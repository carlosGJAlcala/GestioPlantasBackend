package es.uah.huertojpa.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * W9 – {@code GET /metricas/panel}: macetas del usuario con métricas, estado y depósito, y sus alertas abiertas, en
 * una sola petición. Cada uno ve solo lo suyo; el admin, todo; la placa no tiene panel.
 */
class PanelIntegracionTest extends IntegracionPostgres {
    static final int SENSOR_DEPOSITO = 123, MACETA_ANA_2 = 112;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void cadaUsuarioVeSusMacetasConMetricasYSusAlertas() throws Exception {
        jdbc.update("""
                INSERT INTO lectura (sensor_id, maceta_id, magnitud, valor, ts) VALUES
                  (?, ?, 'HUMEDAD', 40, (now() AT TIME ZONE 'Europe/Madrid') - interval '1 minute')
                """, SENSOR_ANA, MACETA_ANA);
        jdbc.update("INSERT INTO alerta (id, maceta_id, tipo, nivel, mensaje, inicio, vista) VALUES (140, ?, 'TIERRA_SECA', 'AVISO', 'seca', now(), now())",
                MACETA_ANA);

        mvc.perform(get("/metricas/panel").header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horas").value(24))
                .andExpect(jsonPath("$.generado").isString())
                .andExpect(jsonPath("$.macetas", hasSize(1)))
                .andExpect(jsonPath("$.macetas[0].id").value(MACETA_ANA))
                .andExpect(jsonPath("$.macetas[0].huertoIdhuerto").value(HUERTO_ANA))
                .andExpect(jsonPath("$.macetas[0].posicionMacetaenElHuerto").value("balcon"))
                .andExpect(jsonPath("$.macetas[0].plantas[0]").value("Tomate de Ana"))
                .andExpect(jsonPath("$.macetas[0].metricas.humedadActual").value(40.0))
                .andExpect(jsonPath("$.macetas[0].metricas.estado.texto").value("Tierra seca"))
                .andExpect(jsonPath("$.alertas", hasSize(1)))
                .andExpect(jsonPath("$.alertas[0].macetaId").value(MACETA_ANA));

        // Luis: su maceta, sin las alertas de Ana.
        mvc.perform(get("/metricas/panel").header("Authorization", bearer("luis")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.macetas", hasSize(1)))
                .andExpect(jsonPath("$.macetas[0].id").value(MACETA_LUIS))
                .andExpect(jsonPath("$.macetas[0].metricas.ultimaLectura").doesNotExist())
                .andExpect(jsonPath("$.alertas", hasSize(0)));

        // Admin: todas las macetas y todas las alertas; la ventana se limita como en /metricas/maceta.
        mvc.perform(get("/metricas/panel?horas=100000").header("Authorization", bearer("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horas").value(24 * 90))
                .andExpect(jsonPath("$.macetas[*].id", containsInAnyOrder(MACETA_ANA, MACETA_LUIS)))
                .andExpect(jsonPath("$.alertas", hasSize(1)));
    }

    @Test
    void lasMacetasDeUnHuertoCompartenElDeposito() throws Exception {
        jdbc.update("INSERT INTO maceta (idmaceta, posicionmacetaenelhuerto, huerto_idhuerto) VALUES (?, 'ventana', ?)",
                MACETA_ANA_2, HUERTO_ANA);
        jdbc.update("INSERT INTO sensor (idsensor, nombresensor, magnitudamedir, cantidadmedida, unidades) VALUES (?, 'ultrasonidos', 'Distancia', '5', 'cm')",
                SENSOR_DEPOSITO);
        jdbc.update("UPDATE depositoagua SET sensor_idsensor = ? WHERE iddepositoagua = ?", SENSOR_DEPOSITO, DEPOSITO_ANA);
        jdbc.update("""
                INSERT INTO lectura (sensor_id, maceta_id, magnitud, valor, ts) VALUES
                  (?, NULL, 'DISTANCIA', 16, (now() AT TIME ZONE 'Europe/Madrid') - interval '1 minute')
                """, SENSOR_DEPOSITO);

        mvc.perform(get("/metricas/panel").header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.macetas", hasSize(2)))
                .andExpect(jsonPath("$.macetas[0].id").value(MACETA_ANA))   // ordenadas por huerto e id
                .andExpect(jsonPath("$.macetas[1].id").value(MACETA_ANA_2))
                .andExpect(jsonPath("$.macetas[1].plantas", hasSize(0)))
                .andExpect(jsonPath("$.macetas[0].metricas.deposito.nivel").value(20.0))
                .andExpect(jsonPath("$.macetas[1].metricas.deposito.nivel").value(20.0));
    }

    @Test
    void sinPersonaNoHayPanel() throws Exception {
        mvc.perform(get("/metricas/panel")).andExpect(status().isUnauthorized());
        // La clave del ESP32 solo autentica en /auth/**: aquí es como no traer nada.
        mvc.perform(get("/metricas/panel").header("X-Device-Key", CLAVE_DISPOSITIVO)).andExpect(status().isUnauthorized());
    }
}
