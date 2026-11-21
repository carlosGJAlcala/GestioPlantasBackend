package es.uah.huertojpa.integracion;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** S5/P7: tras quitar {@code @EnableWebMvc} siguen funcionando CORS y las fechas salen en ISO-8601. */
class ConfiguracionWebIntegracionTest extends IntegracionPostgres {

    @Test
    void preflightCorsDelFront() throws Exception {
        mvc.perform(options("/maceta/" + MACETA_ANA)
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void fechasEnIso() throws Exception {
        String ana = bearer("ana");
        mvc.perform(post("/maceta/" + MACETA_ANA + "/riego").header("Authorization", ana))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.creada").value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}.*")));
        mvc.perform(get("/planta/" + PLANTA_ANA).header("Authorization", ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaPlantacion").value("2026-10-01"));
    }
}
