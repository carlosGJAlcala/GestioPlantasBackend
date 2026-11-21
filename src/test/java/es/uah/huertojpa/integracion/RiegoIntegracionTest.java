package es.uah.huertojpa.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static es.uah.huertojpa.seguridad.DispositivoKeyFilter.CABECERA;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** P8: ciclo completo de «Regar ahora» (web → orden → ESP32 → riego registrado → resumen y métricas). */
class RiegoIntegracionTest extends IntegracionPostgres {

    @Test
    void regarAhoraDeExtremoAExtremo() throws Exception {
        String ana = bearer("ana");
        String orden = "/maceta/" + MACETA_ANA + "/riego";

        mvc.perform(post(orden).header("Authorization", bearer("luis"))).andExpect(status().isForbidden());
        mvc.perform(post(orden).header("Authorization", ana))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
        mvc.perform(post(orden).header("Authorization", ana)).andExpect(status().isOk()); // la misma orden
        mvc.perform(get(orden + "/orden").header("Authorization", ana)).andExpect(status().isOk());

        // El ESP32 la recoge (una sola vez).
        String recoger = "/auth/maceta/" + MACETA_ANA + "/orden";
        mvc.perform(get(recoger).header(CABECERA, CLAVE_DISPOSITIVO))
                .andExpect(status().isOk()).andExpect(jsonPath("$.regar").value(true));
        mvc.perform(get(recoger).header(CABECERA, CLAVE_DISPOSITIVO)).andExpect(status().isNoContent());

        // ...riega y lo registra.
        mvc.perform(post("/auth/riego").header(CABECERA, CLAVE_DISPOSITIVO).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idMaceta\":" + MACETA_ANA + ",\"duracionMs\":4000,\"pulsos\":120,"
                                + "\"humedadAntes\":38,\"humedadDespues\":66,\"nivelDepositoAntes\":80,"
                                + "\"nivelDepositoDespues\":76,\"motivo\":\"MANUAL\",\"resultado\":\"OK\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/auth/riego").contentType(MediaType.APPLICATION_JSON).content("{\"idMaceta\":" + MACETA_ANA + "}"))
                .andExpect(status().isUnauthorized());

        // Inmediatamente después no se puede volver a pedir (429 con Retry-After).
        mvc.perform(post(orden).header("Authorization", ana))
                .andExpect(status().isTooManyRequests()).andExpect(header().exists("Retry-After"));

        mvc.perform(get("/riego/maceta/" + MACETA_ANA + "/resumen").header("Authorization", ana))
                .andExpect(status().isOk());
        mvc.perform(get("/riego/maceta/" + MACETA_ANA).header("Authorization", ana))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/metricas/maceta/" + MACETA_ANA + "?horas=24").header("Authorization", ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.segundosMediosEntregaOrden").exists());
    }
}
