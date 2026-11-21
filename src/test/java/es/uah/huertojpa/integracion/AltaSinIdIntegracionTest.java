package es.uah.huertojpa.integracion;

import com.google.common.hash.Hashing;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los formularios de alta del front ya no piden el id (lo genera la BD). Antes, un alta sin id llegaba a
 * {@code findById(null)} y el backend respondía 500. Mismos cuerpos JSON que manda el front.
 */
class AltaSinIdIntegracionTest extends IntegracionPostgres {

    @Autowired
    JdbcTemplate jdbc;

    private void alta(String usuario, String ruta, String json) throws Exception {
        mvc.perform(post(ruta).header("Authorization", bearer(usuario)).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andExpect(content().string("true"));
    }

    private int filas(String tabla) {
        return jdbc.queryForObject("SELECT count(*) FROM " + tabla, Integer.class);
    }

    @Test
    void macetaSinIdEnUnHuertoPropio() throws Exception {
        int antes = filas("maceta");
        alta("ana", "/maceta", "{\"posicionMacetaenElHuerto\":\"Balcón\",\"huertoIdhuerto\":" + HUERTO_ANA + "}");
        assertEquals(antes + 1, filas("maceta"));
        assertEquals(HUERTO_ANA, jdbc.queryForObject(
                "SELECT huerto_idhuerto FROM maceta WHERE posicionmacetaenelhuerto = 'Balcón'", Integer.class));
    }

    @Test
    void depositoSensorYPlantaSinId() throws Exception {
        int depositos = filas("depositoagua"), sensores = filas("sensor"), plantas = filas("planta");

        alta("admin", "/depositoagua", "{\"cantidadMaxima\":5,\"huertoIdhuerto\":" + HUERTO_LUIS
                + ",\"alturaDeposito\":20,\"estado\":\"LLENO\"}");
        alta("admin", "/sensor", "{\"nombreSensor\":\"luz-nueva\",\"magnitudAMedir\":\"Luz\",\"cantidadMedida\":\"0\",\"unidades\":\"lx\"}");
        alta("admin", "/planta", "{\"nombrePlanta\":\"Albahaca\",\"fechaPlantacion\":\"2026-11-09\",\"estado\":\"FELIZ\","
                + "\"tipoplantaIdtipoplanta\":1,\"macetaIdmaceta\":" + MACETA_ANA
                + ",\"nivelActualHumedad\":50,\"nivelActualLuminosidad\":10,\"nivelActualTemperatura\":20}");

        assertEquals(depositos + 1, filas("depositoagua"));
        assertEquals(sensores + 1, filas("sensor"));
        assertEquals(plantas + 1, filas("planta"));
    }

    @Test
    void elAdminDaDeAltaUnUsuarioQuePuedeEntrarYCrearSuHuerto() throws Exception {
        int personas = filas("persona"), usuarios = filas("usuario");
        String eva = "{\"nombre\":\"Eva\",\"apellido\":\"Ruiz\",\"usuario\":\"eva\","
                + "\"correoElectronico\":\"eva@example.com\",\"passwordSHA256\":\"clave-de-eva\"}";
        alta("admin", "/persona", eva);
        assertEquals(personas + 1, filas("persona"));
        assertEquals(usuarios + 1, filas("usuario"));

        // Usuario repetido o sin contraseña: «false», no un 500.
        mvc.perform(post("/persona").header("Authorization", bearer("admin")).contentType(MediaType.APPLICATION_JSON).content(eva))
                .andExpect(status().isOk()).andExpect(content().string("false"));
        mvc.perform(post("/persona").header("Authorization", bearer("admin")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Sin\",\"usuario\":\"sinclave\"}")).andExpect(content().string("false"));

        // Entra como lo hace el front (SHA-256 de la contraseña) y crea su huerto.
        String hash = Hashing.sha256().hashString("clave-de-eva", StandardCharsets.UTF_8).toString();
        String json = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"eva\",\"password\":\"" + hash + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(json, "$.token");
        mvc.perform(post("/huerto").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"numeroMacetas\":2}")).andExpect(content().string("true"));
    }
}
