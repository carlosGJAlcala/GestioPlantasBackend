package es.uah.huertojpa.integracion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * W12b – Sensores: el sensor libre es de quien lo da de alta, «Mis sensores» incluye el del depósito y los libres
 * propios, {@code GET /sensor/uso} dice dónde se usa cada uno, {@code PUT /sensor} edita los datos sin apuntar una
 * lectura y un sensor en uso no se puede borrar.
 */
class SensoresIntegracionTest extends IntegracionPostgres {

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private ObjectMapper json;

    private int altaSensor(String usuario, String nombre) throws Exception {
        mvc.perform(post("/sensor").header("Authorization", bearer(usuario)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreSensor\":\"" + nombre + "\",\"magnitudAMedir\":\"Distancia\",\"unidades\":\"cm\"}"))
                .andExpect(status().isOk()).andExpect(content().string("true"));
        return jdbc.queryForObject("SELECT idsensor FROM sensor WHERE nombresensor = ?", Integer.class, nombre);
    }

    private JsonNode uso(String usuario) throws Exception {
        String cuerpo = mvc.perform(get("/sensor/uso").header("Authorization", bearer(usuario)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(cuerpo);
    }

    private static List<Integer> ids(JsonNode lista) {
        List<Integer> ids = new ArrayList<>();
        lista.forEach(n -> ids.add(n.get("id").asInt()));
        return ids;
    }

    @Test
    void elSensorLibreEsDeQuienLoCrea() throws Exception {
        int nuevo = altaSensor("ana", "distancia-ana");
        assertEquals(ANA, jdbc.queryForObject("SELECT creado_por FROM sensor WHERE idsensor = ?", Integer.class, nuevo));

        // Ana lo ve en «Mis sensores» y en /sensor/uso, como libre; Luis no lo ve ni lo puede usar.
        mvc.perform(get("/sensor/userName/ana").header("Authorization", bearer("ana")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("distancia-ana")));
        JsonNode deAna = uso("ana");
        assertEquals(List.of(nuevo, SENSOR_ANA), ids(deAna), "ordenados por id");
        assertTrue(deAna.get(0).get("libre").asBoolean());
        assertFalse(ids(uso("luis")).contains(nuevo));
        mvc.perform(get("/sensor/" + nuevo).header("Authorization", bearer("luis"))).andExpect(status().isForbidden());
        mvc.perform(put("/depositoagua").header("Authorization", bearer("luis")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + DEPOSITO_ANA + ",\"sensorIdsensor\":{\"id\":" + nuevo + "}}"))
                .andExpect(status().isForbidden());

        // Ana lo pone en su depósito: sigue en «Mis sensores», ahora como sensor del depósito.
        mvc.perform(put("/depositoagua").header("Authorization", bearer("ana")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + DEPOSITO_ANA + ",\"sensorIdsensor\":{\"id\":" + nuevo + "}}"))
                .andExpect(status().isOk()).andExpect(content().string("true"));
        JsonNode usado = uso("ana").get(0);
        assertEquals(HUERTO_ANA, usado.get("huertoDeposito").asInt());
        assertFalse(usado.get("libre").asBoolean());
        assertEquals(MACETA_ANA, uso("ana").get(1).get("macetas").get(0).asInt());
    }

    @Test
    void misSensoresSinMacetasNoDaError() throws Exception {
        // Antes: persona sin macetas ⇒ NullPointerException ⇒ 500.
        jdbc.update("DELETE FROM maceta WHERE huerto_idhuerto = ?", HUERTO_LUIS);
        mvc.perform(get("/sensor/userName/luis").header("Authorization", bearer("luis")))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void elAdminVeTodosConSuUso() throws Exception {
        JsonNode todos = uso("admin");
        assertEquals(List.of(SENSOR_ANA, SENSOR_LUIS, SENSOR_LIBRE), ids(todos));
        assertTrue(todos.get(2).get("libre").asBoolean());
        // El usuario no ve los sensores libres antiguos (sin dueño) en su lista, aunque pueda usarlos.
        assertEquals(List.of(SENSOR_ANA), ids(uso("ana")));
    }

    @Test
    void editarDatosNoApuntaUnaLectura() throws Exception {
        int lecturas = jdbc.queryForObject("SELECT count(*) FROM lectura", Integer.class);
        mvc.perform(put("/sensor").header("Authorization", bearer("ana")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + SENSOR_ANA + ",\"nombreSensor\":\"humedad balcón\",\"magnitudAMedir\":\"Humedad\","
                                + "\"unidades\":\"%\",\"cantidadMedida\":\"3\"}"))
                .andExpect(status().isOk()).andExpect(content().string("true"));
        var fila = jdbc.queryForMap("SELECT nombresensor, cantidadmedida FROM sensor WHERE idsensor = ?", SENSOR_ANA);
        assertEquals("humedad balcón", fila.get("nombresensor"));
        assertEquals("65", fila.get("cantidadmedida"), "la medida solo la cambia el ESP32");
        assertEquals(lecturas, jdbc.queryForObject("SELECT count(*) FROM lectura", Integer.class));

        mvc.perform(put("/sensor").header("Authorization", bearer("luis")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + SENSOR_ANA + ",\"nombreSensor\":\"mío\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/sensor").header("Authorization", bearer("ana")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + SENSOR_ANA + ",\"nombreSensor\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unSensorEnUsoNoSeBorra() throws Exception {
        mvc.perform(delete("/sensor/" + SENSOR_ANA).header("Authorization", bearer("ana")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("maceta " + MACETA_ANA)));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM maceta_has_sensor WHERE sensor_idsensor = ?",
                Integer.class, SENSOR_ANA));

        int nuevo = altaSensor("ana", "para-borrar");
        mvc.perform(delete("/sensor/" + nuevo).header("Authorization", bearer("luis"))).andExpect(status().isForbidden());
        mvc.perform(delete("/sensor/" + nuevo).header("Authorization", bearer("ana")))
                .andExpect(status().isOk()).andExpect(content().string("true"));
    }

    @Test
    void asociarYQuitarDeUnaMaceta() throws Exception {
        int nuevo = altaSensor("ana", "humedad-2");
        String enlace = "{\"id\":{\"macetaIdmaceta\":" + MACETA_ANA + ",\"sensorIdsensor\":" + nuevo + "}}";
        mvc.perform(post("/macetahassensor").header("Authorization", bearer("ana"))
                .contentType(MediaType.APPLICATION_JSON).content(enlace)).andExpect(status().isOk());
        assertEquals(List.of(MACETA_ANA), jdbc.queryForList(
                "SELECT maceta_idmaceta FROM maceta_has_sensor WHERE sensor_idsensor = ?", Integer.class, nuevo));

        // El sensor de un depósito no puede medir además una maceta.
        mvc.perform(put("/depositoagua").header("Authorization", bearer("ana")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + DEPOSITO_ANA + ",\"sensorIdsensor\":{\"id\":" + SENSOR_LIBRE + "}}"))
                .andExpect(status().isOk());
        mvc.perform(post("/macetahassensor").header("Authorization", bearer("ana")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":{\"macetaIdmaceta\":" + MACETA_ANA + ",\"sensorIdsensor\":" + SENSOR_LIBRE + "}}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("depósito")));

        mvc.perform(delete("/macetahassensor").header("Authorization", bearer("ana"))
                .contentType(MediaType.APPLICATION_JSON).content(enlace)).andExpect(status().isOk());
        assertEquals(0, jdbc.queryForObject(
                "SELECT count(*) FROM maceta_has_sensor WHERE sensor_idsensor = ?", Integer.class, nuevo));
    }
}
