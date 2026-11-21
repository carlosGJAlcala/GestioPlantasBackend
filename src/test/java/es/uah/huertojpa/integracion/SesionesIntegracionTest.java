package es.uah.huertojpa.integracion;

import com.jayway.jsonpath.JsonPath;
import es.uah.huertojpa.seguridad.Contrasenas;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * S8b (la contraseña temporal solo sirve para cambiarla y caduca) y S9 (cerrar sesión en todos los equipos), con la
 * cadena de seguridad real.
 */
class SesionesIntegracionTest extends IntegracionPostgres {

    @Autowired
    JdbcTemplate jdbc;

    private ResultActions login(String usuario, String password) throws Exception {
        return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"" + usuario + "\",\"password\":\"" + password + "\"}"));
    }

    /** El admin pone a Ana una temporal y Ana entra con ella. Devuelve el token de Ana. */
    private String anaConTemporal(String temporal) throws Exception {
        mvc.perform(put("/persona/" + ANA + "/contrasena").header("Authorization", bearer("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nueva\":\"" + temporal + "\"}"))
                .andExpect(status().isOk());
        String json = login("ana", temporal).andExpect(jsonPath("$.debeCambiarContrasena").value(true))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(json, "$.token");
    }

    @Test
    void conLaTemporalSoloSePuedeCambiarla() throws Exception {
        String temporal = Contrasenas.sha256("temporal-123");
        String ana = anaConTemporal(temporal);

        // Antes bastaba con no hacer caso al front: con el token se usaba todo el API.
        mvc.perform(get("/maceta/" + MACETA_ANA).header("Authorization", ana))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.debeCambiarContrasena").value(true))
                .andExpect(jsonPath("$.mensaje", containsString("contraseña temporal")));
        mvc.perform(get("/metricas/panel").header("Authorization", ana)).andExpect(status().isForbidden());
        mvc.perform(put("/persona").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":" + ANA + ",\"nombre\":\"Otra\"}")).andExpect(status().isForbidden());
        mvc.perform(post("/persona/sesiones/cerrar").header("Authorization", ana)).andExpect(status().isForbidden());
        // Sí: leer su ficha (el front la pide) y volver a entrar aunque el navegador mande el token guardado.
        mvc.perform(get("/persona/userName/ana").header("Authorization", ana)).andExpect(status().isOk());
        mvc.perform(get("/persona/userName/luis").header("Authorization", ana)).andExpect(status().isForbidden());
        mvc.perform(post("/auth/login").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"ana\",\"password\":\"" + temporal + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isString());
        // El ESP32 (sin JWT) no se ve afectado (204: no hay orden de riego pendiente).
        mvc.perform(get("/auth/maceta/" + MACETA_ANA + "/orden").header("X-Device-Key", CLAVE_DISPOSITIVO))
                .andExpect(status().is2xxSuccessful());

        // Al cambiarla, el token nuevo ya sirve para todo.
        String cambio = mvc.perform(put("/persona/contrasena").header("Authorization", ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actual\":\"" + temporal + "\",\"nueva\":\"" + Contrasenas.sha256("pepinos") + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        mvc.perform(get("/maceta/" + MACETA_ANA).header("Authorization", "Bearer " + JsonPath.read(cambio, "$.token")))
                .andExpect(status().isOk());
    }

    @Test
    void laTemporalCaduca() throws Exception {
        String temporal = Contrasenas.sha256("temporal-123");
        String ana = anaConTemporal(temporal);

        // Han pasado 4 días (el plazo por defecto es 3) sin cambiarla.
        jdbc.update("UPDATE persona SET contrasena_cambiada = ? WHERE id = ?", LocalDateTime.now().minusDays(4), ANA);
        login("ana", temporal)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUser").value("-1"))
                .andExpect(jsonPath("$.temporalCaducada").value(true))
                .andExpect(jsonPath("$.token").doesNotExist());
        // La sesión que abrió con ella tampoco vale ya (ni para cambiarla).
        mvc.perform(get("/persona/userName/ana").header("Authorization", ana)).andExpect(status().isUnauthorized());
        // Una contraseña mala sigue siendo «incorrecta», sin pistas de que la temporal existía.
        login("ana", Contrasenas.sha256("otra")).andExpect(jsonPath("$.temporalCaducada").doesNotExist());

        // El admin le pone otra y vuelve a poder entrar.
        String otra = Contrasenas.sha256("temporal-456");
        assertNotNull(anaConTemporal(otra));
        // Al día siguiente aún vale.
        jdbc.update("UPDATE persona SET contrasena_cambiada = ? WHERE id = ?", LocalDateTime.now().minusDays(1), ANA);
        login("ana", otra).andExpect(jsonPath("$.token").isString());
    }

    @Test
    void cerrarSesionEnTodosLosEquipos() throws Exception {
        String anaMovil = bearer("ana");
        String luisMovil = bearer("luis");
        String admin = bearer("admin");
        // iat va en segundos: los tokens «de antes» tienen que ser de un segundo anterior al cierre.
        Thread.sleep(1100);

        String json = mvc.perform(post("/persona/sesiones/cerrar").header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cerradas").value(true))
                .andReturn().getResponse().getContentAsString();
        String anaAqui = "Bearer " + JsonPath.read(json, "$.token");
        // La sesión del móvil queda fuera; la de este equipo sigue con el token nuevo. Luis no se entera.
        mvc.perform(get("/maceta/" + MACETA_ANA).header("Authorization", anaMovil)).andExpect(status().isUnauthorized());
        mvc.perform(get("/maceta/" + MACETA_ANA).header("Authorization", anaAqui)).andExpect(status().isOk());
        mvc.perform(get("/maceta/" + MACETA_LUIS).header("Authorization", luisMovil)).andExpect(status().isOk());
        // Puede volver a entrar con su contraseña de siempre.
        login("ana", "ana").andExpect(jsonPath("$.token").isString());

        // Las de otra persona, solo el admin.
        mvc.perform(post("/persona/" + LUIS + "/sesiones/cerrar").header("Authorization", anaAqui))
                .andExpect(status().isForbidden());
        mvc.perform(post("/persona/9999/sesiones/cerrar").header("Authorization", admin))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/persona/" + LUIS + "/sesiones/cerrar").header("Authorization", admin))
                .andExpect(status().isOk()).andExpect(content().string("true"));
        mvc.perform(get("/maceta/" + MACETA_LUIS).header("Authorization", luisMovil)).andExpect(status().isUnauthorized());
        mvc.perform(get("/maceta/" + MACETA_ANA).header("Authorization", anaAqui)).andExpect(status().isOk());
    }

    @Test
    void sinTokenNoHayNadaQueCerrar() throws Exception {
        mvc.perform(post("/persona/sesiones/cerrar")).andExpect(status().isUnauthorized());
        mvc.perform(post("/persona/sesiones/cerrar").header("X-Device-Key", CLAVE_DISPOSITIVO))
                .andExpect(status().isUnauthorized());
    }
}
