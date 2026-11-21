package es.uah.huertojpa.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * W12 – Edición del depósito desde la web: el PUT guarda capacidad, altura y sensor (antes solo el estado), un
 * huerto tiene un único depósito y el sensor del depósito no puede ser el de una maceta ni el de otro depósito.
 */
class DepositoEdicionIntegracionTest extends IntegracionPostgres {

    @Autowired
    private JdbcTemplate jdbc;

    private ResultActions enviar(String usuario, boolean alta, String json) throws Exception {
        var peticion = alta ? post("/depositoagua") : put("/depositoagua");
        return mvc.perform(peticion.header("Authorization", bearer(usuario))
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private Map<String, Object> deposito(int id) {
        return jdbc.queryForMap("SELECT cantidadmaxima, alturadeposito, estado, sensor_idsensor, huerto_idhuerto "
                + "FROM depositoagua WHERE iddepositoagua = ?", id);
    }

    @Test
    void elPutGuardaCapacidadAlturaYSensor() throws Exception {
        enviar("ana", false, "{\"id\":" + DEPOSITO_ANA + ",\"cantidadMaxima\":8,\"alturaDeposito\":30,"
                + "\"sensorIdsensor\":{\"id\":" + SENSOR_LIBRE + "}}")
                .andExpect(status().isOk()).andExpect(content().string("true"));
        Map<String, Object> d = deposito(DEPOSITO_ANA);
        assertEquals(8f, ((Number) d.get("cantidadmaxima")).floatValue());
        assertEquals(30f, ((Number) d.get("alturadeposito")).floatValue());
        assertEquals(SENSOR_LIBRE, d.get("sensor_idsensor"));
        assertEquals("LLENO", d.get("estado"), "lo que no llega no se toca");
        assertEquals(HUERTO_ANA, d.get("huerto_idhuerto"));

        // Sin sensorIdsensor el sensor se queda; con {"id": null} se quita.
        enviar("ana", false, "{\"id\":" + DEPOSITO_ANA + ",\"cantidadMaxima\":9}").andExpect(status().isOk());
        assertEquals(SENSOR_LIBRE, deposito(DEPOSITO_ANA).get("sensor_idsensor"));
        enviar("ana", false, "{\"id\":" + DEPOSITO_ANA + ",\"sensorIdsensor\":{\"id\":null}}").andExpect(status().isOk());
        assertNull(deposito(DEPOSITO_ANA).get("sensor_idsensor"));
    }

    @Test
    void unHuertoTieneUnSoloDeposito() throws Exception {
        int antes = jdbc.queryForObject("SELECT count(*) FROM depositoagua", Integer.class);
        enviar("ana", true, "{\"cantidadMaxima\":5,\"alturaDeposito\":20,\"huertoIdhuerto\":" + HUERTO_ANA + "}")
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("ya tiene un depósito")));
        assertEquals(antes, jdbc.queryForObject("SELECT count(*) FROM depositoagua", Integer.class));
    }

    @Test
    void elSensorDeUnaMacetaNoSirveParaElDeposito() throws Exception {
        enviar("ana", false, "{\"id\":" + DEPOSITO_ANA + ",\"sensorIdsensor\":{\"id\":" + SENSOR_ANA + "}}")
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("ya mide una maceta")));
        assertNull(deposito(DEPOSITO_ANA).get("sensor_idsensor"));
    }

    @Test
    void valoresImposiblesDanUn400ConMotivo() throws Exception {
        enviar("ana", false, "{\"id\":" + DEPOSITO_ANA + ",\"cantidadMaxima\":0}")
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("capacidad")));
        // W12b: para un usuario, un sensor inexistente es 403 (no se revela qué ids existen); el admin ve el motivo.
        enviar("ana", false, "{\"id\":" + DEPOSITO_ANA + ",\"sensorIdsensor\":{\"id\":9999}}")
                .andExpect(status().isForbidden());
        enviar("admin", false, "{\"id\":" + DEPOSITO_ANA + ",\"sensorIdsensor\":{\"id\":9999}}")
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("No existe el sensor")));
    }

    @Test
    void altaConSensorLibreYSinSensorAjeno() throws Exception {
        // Luis no puede usar el sensor de la maceta de Ana (403, antes de mirar nada más).
        enviar("luis", true, "{\"cantidadMaxima\":10,\"alturaDeposito\":25,\"huertoIdhuerto\":" + HUERTO_LUIS
                + ",\"sensorIdsensor\":{\"id\":" + SENSOR_ANA + "}}")
                .andExpect(status().isForbidden());

        enviar("luis", true, "{\"cantidadMaxima\":10,\"alturaDeposito\":25,\"huertoIdhuerto\":" + HUERTO_LUIS
                + ",\"sensorIdsensor\":{\"id\":" + SENSOR_LIBRE + "}}")
                .andExpect(status().isOk()).andExpect(content().string("true"));
        assertEquals(SENSOR_LIBRE, jdbc.queryForObject(
                "SELECT sensor_idsensor FROM depositoagua WHERE huerto_idhuerto = ?", Integer.class, HUERTO_LUIS));

        // El mismo sensor ya no se puede poner en el depósito de Ana.
        enviar("ana", false, "{\"id\":" + DEPOSITO_ANA + ",\"sensorIdsensor\":{\"id\":" + SENSOR_LIBRE + "}}")
                .andExpect(status().isForbidden()); // ya es de un huerto de Luis: para Ana es ajeno
    }
}
