package es.uah.huertojpa.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * W13 (2026-11-21) – Plantas: {@code PUT /planta} guarda de verdad (antes respondía 200 sin tocar la BD), no pisa
 * las lecturas ni el estado que pone el sensor, y las altas/ediciones con datos imposibles dan 400 con el motivo en
 * vez de un 500 por la FK del tipo de planta.
 */
class PlantasIntegracionTest extends IntegracionPostgres {

    @Autowired
    private JdbcTemplate jdbc;

    private static String planta(Integer id, String nombre, String fecha, Integer tipo, int maceta) {
        return "{" + (id == null ? "" : "\"id\":" + id + ",")
                + "\"nombrePlanta\":" + (nombre == null ? "null" : "\"" + nombre + "\"")
                + ",\"fechaPlantacion\":" + (fecha == null ? "null" : "\"" + fecha + "\"")
                + ",\"tipoplantaIdtipoplanta\":" + tipo + ",\"macetaIdmaceta\":" + maceta
                // Lo que mandaba el formulario antiguo: no debe llegar a la BD al editar.
                + ",\"estado\":\"ALEGRE\",\"nivelActualHumedad\":1}";
    }

    @Test
    void editarPlantaGuardaLosDatosYRespetaLasLecturas() throws Exception {
        jdbc.update("UPDATE planta SET nivelactualhumedad = 61.5 WHERE idplanta = ?", PLANTA_ANA);

        mvc.perform(put("/planta").header("Authorization", bearer("ana")).contentType(MediaType.APPLICATION_JSON)
                        .content(planta(PLANTA_ANA, "Tomate cherry", "2026-10-15", 1, MACETA_ANA)))
                .andExpect(status().isOk()).andExpect(content().string("true"));

        Map<String, Object> fila = jdbc.queryForMap(
                "SELECT nombreplanta, fechaplantacion, estado, nivelactualhumedad FROM planta WHERE idplanta = ?", PLANTA_ANA);
        assertEquals("Tomate cherry", fila.get("nombreplanta"));
        assertEquals(java.sql.Date.valueOf("2026-10-15"), fila.get("fechaplantacion"));
        assertEquals("FELIZ", fila.get("estado"), "el estado lo pone el sensor, no el formulario");
        assertEquals(61.5f, ((Number) fila.get("nivelactualhumedad")).floatValue(), 0.01f);
    }

    @Test
    void datosImposiblesDan400ConMotivo() throws Exception {
        String ana = bearer("ana");
        int antes = jdbc.queryForObject("SELECT count(*) FROM planta", Integer.class);

        // Tipo inexistente: antes violaba la FK ⇒ 500.
        mvc.perform(post("/planta").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                        .content(planta(null, "Rara", null, 9999, MACETA_ANA)))
                .andExpect(status().isBadRequest()).andExpect(content().string("Elige un tipo de planta de la biblioteca."));
        mvc.perform(post("/planta").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                        .content(planta(null, "  ", null, 1, MACETA_ANA)))
                .andExpect(status().isBadRequest()).andExpect(content().string("La planta necesita un nombre."));
        mvc.perform(post("/planta").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                        .content(planta(null, "Futura", LocalDate.now().plusDays(3).toString(), 1, MACETA_ANA)))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/planta").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                        .content(planta(PLANTA_ANA, "Tomate", null, null, MACETA_ANA)))
                .andExpect(status().isBadRequest());
        assertEquals(antes, jdbc.queryForObject("SELECT count(*) FROM planta", Integer.class));
        assertEquals("Tomate de Ana", jdbc.queryForObject("SELECT nombreplanta FROM planta WHERE idplanta = ?",
                String.class, PLANTA_ANA));

        // Un alta correcta sigue funcionando, sin id.
        mvc.perform(post("/planta").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                        .content(planta(null, "Albahaca", LocalDate.now().toString(), 1, MACETA_ANA)))
                .andExpect(status().isOk()).andExpect(content().string("true"));
        assertEquals(antes + 1, jdbc.queryForObject("SELECT count(*) FROM planta", Integer.class));
    }

    @Test
    void noSePuedeMoverLaPlantaAUnaMacetaAjena() throws Exception {
        mvc.perform(put("/planta").header("Authorization", bearer("ana")).contentType(MediaType.APPLICATION_JSON)
                        .content(planta(PLANTA_ANA, "Tomate de Ana", null, 1, MACETA_LUIS)))
                .andExpect(status().isForbidden());
        assertEquals(MACETA_ANA, jdbc.queryForObject("SELECT maceta_idmaceta FROM planta WHERE idplanta = ?",
                Integer.class, PLANTA_ANA));
    }
}
