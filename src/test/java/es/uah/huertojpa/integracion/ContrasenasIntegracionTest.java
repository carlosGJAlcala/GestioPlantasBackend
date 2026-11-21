package es.uah.huertojpa.integracion;

import es.uah.huertojpa.seguridad.Contrasenas;
import es.uah.huertojpa.seguridad.MigracionContrasenas;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** S2: contraseñas con BCrypt, conversión de las antiguas, cambio de contraseña y hash fuera del API. */
class ContrasenasIntegracionTest extends IntegracionPostgres {

    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    MigracionContrasenas migracion;

    private String guardada(int id) {
        return jdbc.queryForObject("SELECT passwordsha256 FROM persona WHERE id = ?", String.class, id);
    }

    private void login(String usuario, String password, boolean correcto) throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"" + usuario + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUser").value(correcto ? String.valueOf(ANA) : "-1"));
    }

    @Test
    void elPrimerLoginConvierteLaContrasenaAntigua() throws Exception {
        assertEquals("ana", guardada(ANA));
        login("ana", "mala", false);
        assertEquals("ana", guardada(ANA), "un intento fallido no toca nada");

        login("ana", "ana", true);
        String bcrypt = guardada(ANA);
        assertTrue(Contrasenas.esBcrypt(bcrypt), bcrypt);
        login("ana", "ana", true);
        login("ana", "mala", false);
        // Lo guardado ya no vale como contraseña (antes sí: el hash de la BD era la credencial).
        login("ana", bcrypt, false);
        login("nadie", "ana", false);
    }

    @Test
    void migracionAlArrancar() throws Exception {
        assertEquals(3, migracion.migrar());
        assertTrue(Contrasenas.esBcrypt(guardada(ADMIN)));
        assertTrue(Contrasenas.esBcrypt(guardada(ANA)));
        assertEquals(0, migracion.migrar(), "idempotente");
        login("ana", "ana", true);
    }

    @Test
    void elApiNoDevuelveElHash() throws Exception {
        mvc.perform(get("/persona/userName/ana").header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("ana"))
                .andExpect(jsonPath("$.passwordSHA256").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/persona").header("Authorization", bearer("admin")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("$2"))))
                .andExpect(content().string(not(containsString("password"))));
    }

    @Test
    void altaDesdeLaWebGuardaBcrypt() throws Exception {
        mvc.perform(post("/persona").header("Authorization", bearer("admin")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Eva\",\"usuario\":\"eva\",\"passwordSHA256\":\"tomates\"}"))
                .andExpect(status().isOk()).andExpect(content().string("true"));
        String guardado = jdbc.queryForObject("SELECT passwordsha256 FROM persona WHERE usuario = 'eva'", String.class);
        assertTrue(Contrasenas.esBcrypt(guardado));
        // El front entra mandando el SHA-256 de lo que se escribió en el alta.
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"eva\",\"password\":\"" + Contrasenas.sha256("tomates") + "\"}"))
                .andExpect(jsonPath("$.userName").value("eva"))
                .andExpect(jsonPath("$.permisoAdmin").value("0"));
    }

    @Test
    void cambiarContrasena() throws Exception {
        String ana = bearer("ana");
        String nueva = Contrasenas.sha256("pepinos");
        mvc.perform(put("/persona/contrasena").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actual\":\"mala\",\"nueva\":\"" + nueva + "\"}"))
                .andExpect(status().isBadRequest());
        // La nueva tiene que llegar como SHA-256 (el front la cifra); en claro se rechaza.
        mvc.perform(put("/persona/contrasena").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actual\":\"ana\",\"nueva\":\"pepinos\"}"))
                .andExpect(status().isBadRequest());
        login("ana", "ana", true);

        mvc.perform(put("/persona/contrasena").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actual\":\"ana\",\"nueva\":\"" + nueva + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cambiada").value(true))
                .andExpect(jsonPath("$.token").isString());
        login("ana", "ana", false);
        login("ana", nueva, true);

        // Sin token no se puede.
        mvc.perform(put("/persona/contrasena").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actual\":\"" + nueva + "\",\"nueva\":\"" + nueva + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}
