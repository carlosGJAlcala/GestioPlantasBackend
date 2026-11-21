package es.uah.huertojpa.integracion;

import com.jayway.jsonpath.JsonPath;
import es.uah.huertojpa.seguridad.Contrasenas;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** S6 (límite de intentos de login) y S8 (el admin restablece la contraseña), con la cadena de seguridad real. */
class LoginIntentosIntegracionTest extends IntegracionPostgres {
    private static final String IP = "192.168.1.50", OTRA_IP = "192.168.1.77";

    @Autowired
    JdbcTemplate jdbc;

    private ResultActions login(String usuario, String password, String ip) throws Exception {
        MockHttpServletRequestBuilder peticion = post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"" + usuario + "\",\"password\":\"" + password + "\"}")
                .with(r -> { r.setRemoteAddr(ip); return r; });
        return mvc.perform(peticion);
    }

    private ResultActions restablecer(int id, String autorizacion, String nueva) throws Exception {
        return mvc.perform(put("/persona/" + id + "/contrasena").header("Authorization", autorizacion)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nueva\":\"" + nueva + "\"}"));
    }

    @Test
    void cincoFallosBloqueanElLoginDesdeEsaIp() throws Exception {
        for (int i = 0; i < 5; i++) {
            login("ana", "mala", IP).andExpect(status().isOk()).andExpect(jsonPath("$.idUser").value("-1"));
        }
        // Bloqueado: 429 aunque la contraseña sea la buena, y sin token.
        login("ana", "ana", IP)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "900"))
                .andExpect(jsonPath("$.reintentarEnSegundos").value(900))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.idUser").value("-1"));
        // Desde otro equipo, la dueña entra; y desde la misma IP otro usuario también.
        login("ana", "ana", OTRA_IP).andExpect(status().isOk()).andExpect(jsonPath("$.idUser").value(String.valueOf(ANA)));
        login("luis", "luis", IP).andExpect(status().isOk()).andExpect(jsonPath("$.idUser").value(String.valueOf(LUIS)));
        // Un usuario que no existe cuenta igual (no se distingue de una contraseña mala).
        for (int i = 0; i < 5; i++) login("nadie", "x", OTRA_IP).andExpect(status().isOk());
        login("nadie", "x", OTRA_IP).andExpect(status().isTooManyRequests());
    }

    @Test
    void unLoginCorrectoReiniciaLaCuenta() throws Exception {
        for (int i = 0; i < 4; i++) login("ana", "mala", IP);
        login("ana", "ana", IP).andExpect(status().isOk()).andExpect(jsonPath("$.token").isString());
        for (int i = 0; i < 4; i++) login("ana", "mala", IP);
        login("ana", "ana", IP).andExpect(status().isOk()).andExpect(jsonPath("$.debeCambiarContrasena").value(false));
    }

    @Test
    void elAdminRestableceLaContrasena() throws Exception {
        String admin = bearer("admin");
        String anaAntes = bearer("ana");
        // Ana se ha quedado bloqueada.
        for (int i = 0; i < 5; i++) login("ana", "mala", IP);
        login("ana", "ana", IP).andExpect(status().isTooManyRequests());

        // Solo el admin, solo con un SHA-256 y solo personas que existen.
        String temporal = Contrasenas.sha256("temporal-123");
        restablecer(ANA, bearer("luis"), temporal).andExpect(status().isForbidden());
        restablecer(ANA, admin, "temporal-123").andExpect(status().isBadRequest());
        restablecer(9999, admin, temporal).andExpect(status().isBadRequest());
        restablecer(ANA, admin, temporal).andExpect(status().isOk()).andExpect(content().string("true"));
        assertEquals(Boolean.TRUE, jdbc.queryForObject(
                "SELECT debe_cambiar_contrasena FROM persona WHERE id = ?", Boolean.class, ANA));

        // Desbloqueada, la contraseña antigua ya no vale y la temporal pide cambiarla.
        login("ana", "ana", IP).andExpect(status().isOk()).andExpect(jsonPath("$.idUser").value("-1"));
        String json = login("ana", temporal, IP).andExpect(status().isOk())
                .andExpect(jsonPath("$.debeCambiarContrasena").value(true))
                .andReturn().getResponse().getContentAsString();
        String anaNueva = "Bearer " + JsonPath.read(json, "$.token");

        // La sesión que tenía abierta con la contraseña antigua deja de valer (401 ⇒ el front vuelve al login).
        // Se adelanta el cambio un minuto porque el token antiguo y el restablecimiento pueden caer en el mismo segundo.
        jdbc.update("UPDATE persona SET contrasena_cambiada = ? WHERE id = ?", LocalDateTime.now().plusMinutes(1), ANA);
        mvc.perform(get("/persona/userName/ana").header("Authorization", anaAntes)).andExpect(status().isUnauthorized());
        mvc.perform(get("/persona/userName/ana").header("Authorization", anaNueva)).andExpect(status().isUnauthorized());
        jdbc.update("UPDATE persona SET contrasena_cambiada = ? WHERE id = ?", LocalDateTime.now().minusMinutes(1), ANA);
        mvc.perform(get("/persona/userName/ana").header("Authorization", anaNueva))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debeCambiarContrasena").value(true))
                .andExpect(jsonPath("$.contrasenaCambiada").doesNotExist());

        // Un PUT /persona no quita la marca: solo cambiando la contraseña. S8b: con la temporal ni siquiera se acepta.
        mvc.perform(put("/persona").header("Authorization", anaNueva).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + ANA + ",\"nombre\":\"Ana\",\"debeCambiarContrasena\":false}"))
                .andExpect(status().isForbidden());
        assertEquals(Boolean.TRUE, jdbc.queryForObject(
                "SELECT debe_cambiar_contrasena FROM persona WHERE id = ?", Boolean.class, ANA));

        String definitiva = Contrasenas.sha256("pepinos");
        String cambio = mvc.perform(put("/persona/contrasena").header("Authorization", anaNueva)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actual\":\"" + temporal + "\",\"nueva\":\"" + definitiva + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // El cambio devuelve un token nuevo para seguir en la sesión aunque el anterior ya no valga.
        mvc.perform(get("/persona/userName/ana").header("Authorization", "Bearer " + JsonPath.read(cambio, "$.token")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.debeCambiarContrasena").value(false));
        login("ana", definitiva, IP).andExpect(jsonPath("$.debeCambiarContrasena").value(false));
    }

    @Test
    void ultimoAccesoParaElAdmin() throws Exception {
        assertNull(jdbc.queryForObject("SELECT ultimo_acceso FROM persona WHERE id = ?", LocalDateTime.class, LUIS));
        login("luis", "mala", IP);
        assertNull(jdbc.queryForObject("SELECT ultimo_acceso FROM persona WHERE id = ?", LocalDateTime.class, LUIS),
                "un fallo no cuenta como acceso");
        login("luis", "luis", IP);
        mvc.perform(get("/persona").header("Authorization", bearer("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.usuario == 'luis')].ultimoAcceso", contains(startsWith("20"))))
                .andExpect(content().string(not(containsString("contrasenaCambiada"))));
    }
}
