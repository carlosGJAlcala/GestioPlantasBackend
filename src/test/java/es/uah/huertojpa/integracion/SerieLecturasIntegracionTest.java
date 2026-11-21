package es.uah.huertojpa.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** W7: serie agregada con {@code date_bin} contra PostgreSQL real (la consulta es SQL nativo de Postgres). */
class SerieLecturasIntegracionTest extends IntegracionPostgres {

    @Autowired
    private JdbcTemplate jdbc;

    private static final String RANGO = "&desde=2026-11-08T10:00:00&hasta=2026-11-08T11:00:00";

    private void lecturas() {
        // 10:00-10:14 → 40 y 50; 10:15-10:29 → 60; 10:45-10:59 → 70 (10:30-10:44 sin datos). Una de Luis y una de
        // temperatura que no deben salir. Una a las 11:00 exactas, fuera (el intervalo es [desde, hasta)).
        jdbc.update("""
                INSERT INTO lectura (sensor_id, maceta_id, magnitud, valor, ts) VALUES
                  (?, ?, 'HUMEDAD', 40, '2026-11-08 10:01'), (?, ?, 'HUMEDAD', 50, '2026-11-08 10:14'),
                  (?, ?, 'HUMEDAD', 60, '2026-11-08 10:20'), (?, ?, 'HUMEDAD', 70, '2026-11-08 10:50'),
                  (?, ?, 'HUMEDAD', 99, '2026-11-08 11:00'), (?, ?, 'TEMPERATURA', 21, '2026-11-08 10:05'),
                  (?, ?, 'HUMEDAD', 10, '2026-11-08 10:05')
                """, SENSOR_ANA, MACETA_ANA, SENSOR_ANA, MACETA_ANA, SENSOR_ANA, MACETA_ANA, SENSOR_ANA, MACETA_ANA,
                SENSOR_ANA, MACETA_ANA, SENSOR_ANA, MACETA_ANA, SENSOR_LUIS, MACETA_LUIS);
    }

    @Test
    void agregaPorIntervalosYDevuelveElRangoDeLaPlanta() throws Exception {
        lecturas();
        mvc.perform(get("/lectura/maceta/" + MACETA_ANA + "?magnitud=humedad&paso=15" + RANGO)
                        .header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.magnitud").value("HUMEDAD"))
                .andExpect(jsonPath("$.pasoMinutos").value(15))
                .andExpect(jsonPath("$.rangoMinimo").value(60.0))   // tomate (V4): 60-80 %
                .andExpect(jsonPath("$.rangoMaximo").value(80.0))
                .andExpect(jsonPath("$.puntos", hasSize(3)))
                .andExpect(jsonPath("$.puntos[0].ts").value("2026-11-08T10:00:00"))
                .andExpect(jsonPath("$.puntos[0].media").value(45.0))
                .andExpect(jsonPath("$.puntos[0].minimo").value(40.0))
                .andExpect(jsonPath("$.puntos[0].maximo").value(50.0))
                .andExpect(jsonPath("$.puntos[0].lecturas").value(2))
                .andExpect(jsonPath("$.puntos[2].ts").value("2026-11-08T10:45:00"));

        // Sin paso: 1 h de rango ⇒ paso automático de 1 min (60 puntos posibles, 5 con datos).
        mvc.perform(get("/lectura/maceta/" + MACETA_ANA + "?magnitud=TEMPERATURA" + RANGO)
                        .header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pasoMinutos").value(1))
                .andExpect(jsonPath("$.puntos", hasSize(1)))
                .andExpect(jsonPath("$.rangoMinimo").value(15.0));
    }

    @Test
    void soloLaDuenaYElAdminVenLaSerie() throws Exception {
        String url = "/lectura/maceta/" + MACETA_ANA + "?paso=15" + RANGO;
        mvc.perform(get(url).header("Authorization", bearer("luis"))).andExpect(status().isForbidden());
        mvc.perform(get(url).header("Authorization", bearer("admin"))).andExpect(status().isOk());
        mvc.perform(get(url)).andExpect(status().isUnauthorized());
    }

    @Test
    void parametrosIncorrectosDan400() throws Exception {
        String ana = bearer("ana");
        String base = "/lectura/maceta/" + MACETA_ANA;
        mvc.perform(get(base + "?magnitud=hum;drop").header("Authorization", ana)).andExpect(status().isBadRequest());
        mvc.perform(get(base + "?desde=2026-11-08T11:00:00&hasta=2026-11-08T10:00:00").header("Authorization", ana))
                .andExpect(status().isBadRequest());
    }
}
