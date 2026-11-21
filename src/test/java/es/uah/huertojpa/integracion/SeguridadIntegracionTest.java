package es.uah.huertojpa.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import static es.uah.huertojpa.seguridad.DispositivoKeyFilter.CABECERA;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** P8 + S4b: cadena de filtros real (JWT, clave del ESP32) y control de propiedad de cada recurso. */
class SeguridadIntegracionTest extends IntegracionPostgres {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void loginYTokenObligatorio() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"ana\",\"password\":\"mala\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUser").value("-1"));
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"admin\",\"password\":\"admin\"}"))
                .andExpect(jsonPath("$.permisoAdmin").value("1"));

        mvc.perform(get("/maceta/userName/ana")).andExpect(status().isUnauthorized());
        mvc.perform(get("/maceta/userName/ana").header("Authorization", "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/maceta/userName/ana").header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", contains(MACETA_ANA)));
        mvc.perform(get("/maceta/userName/ana").header("Authorization", bearer("luis")))
                .andExpect(status().isForbidden());
    }

    @Test
    void listadosGlobalesSoloAdmin() throws Exception {
        String ana = bearer("ana"), admin = bearer("admin");
        for (String ruta : new String[]{"/maceta", "/huerto", "/planta", "/sensor", "/depositoagua", "/persona",
                "/macetahassensor", "/huertoHasUsuario", "/planta/estado/FELIZ"}) {
            mvc.perform(get(ruta).header("Authorization", ana)).andExpect(status().isForbidden());
            mvc.perform(get(ruta).header("Authorization", admin)).andExpect(status().isOk());
        }
        // El catálogo de tipos de planta lo lee cualquiera, pero solo lo cambia el admin.
        // S8: restablecer la contraseña de otro es solo del admin (ni siquiera la propia por esta vía).
        String sha = "\"" + "a".repeat(64) + "\"";
        mvc.perform(put("/persona/" + LUIS + "/contrasena").header("Authorization", ana)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nueva\":" + sha + "}")).andExpect(status().isForbidden());
        mvc.perform(put("/persona/" + ANA + "/contrasena").header("Authorization", ana)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nueva\":" + sha + "}")).andExpect(status().isForbidden());
        mvc.perform(put("/persona/" + LUIS + "/contrasena").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nueva\":" + sha + "}")).andExpect(status().isOk());
        mvc.perform(get("/tipoPlanta").header("Authorization", ana)).andExpect(status().isOk());
        mvc.perform(post("/tipoPlanta").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombrePlanta\":\"cactus\"}")).andExpect(status().isForbidden());
        mvc.perform(delete("/tipoPlanta/1").header("Authorization", ana)).andExpect(status().isForbidden());
    }

    @Test
    void recursosPorIdSoloDelDueno() throws Exception {
        String ana = bearer("ana"), luis = bearer("luis"), admin = bearer("admin");
        String[] rutas = {"/maceta/" + MACETA_ANA, "/maceta/huerto/" + HUERTO_ANA, "/huerto/" + HUERTO_ANA,
                "/planta/" + PLANTA_ANA, "/planta/macetaid/" + MACETA_ANA, "/sensor/" + SENSOR_ANA,
                "/depositoagua/" + DEPOSITO_ANA, "/depositoagua/huerto/" + HUERTO_ANA,
                "/macetahassensor/maceta/" + MACETA_ANA, "/metricas/maceta/" + MACETA_ANA,
                "/riego/maceta/" + MACETA_ANA + "/resumen", "/alertas/maceta/" + MACETA_ANA};
        for (String ruta : rutas) {
            mvc.perform(get(ruta).header("Authorization", ana)).andExpect(status().isOk());
            mvc.perform(get(ruta).header("Authorization", luis)).andExpect(status().isForbidden());
            mvc.perform(get(ruta).header("Authorization", admin)).andExpect(status().isOk());
        }
        mvc.perform(get("/maceta/abc").header("Authorization", ana)).andExpect(status().isForbidden());
        // A4: la serie del depósito va por huerto (el de Ana no tiene ultrasonidos en los datos de partida ⇒ 404).
        mvc.perform(get("/lectura/deposito/huerto/" + HUERTO_ANA).header("Authorization", luis)).andExpect(status().isForbidden());
        mvc.perform(get("/lectura/deposito/huerto/" + HUERTO_ANA).header("Authorization", ana)).andExpect(status().isNotFound());
        mvc.perform(get("/sensor/" + SENSOR_LIBRE).header("Authorization", luis)).andExpect(status().isOk());

        // A3: «Entendido» sobre una alerta de la maceta de Ana.
        jdbc.update("INSERT INTO alerta (id, maceta_id, tipo, nivel, mensaje, inicio, vista) VALUES (140, ?, 'TIERRA_SECA', 'AVISO', 'x', now(), now())",
                MACETA_ANA);
        mvc.perform(post("/alertas/140/silenciar").header("Authorization", luis)).andExpect(status().isForbidden());
        mvc.perform(post("/alertas/abc/silenciar").header("Authorization", ana)).andExpect(status().isForbidden());
        mvc.perform(post("/alertas/140/silenciar").header("Authorization", admin)).andExpect(status().isOk());
        mvc.perform(post("/alertas/140/silenciar").header("Authorization", ana)).andExpect(status().isOk());
    }

    @Test
    void borradosAjenosRechazados() throws Exception {
        String luis = bearer("luis");
        mvc.perform(delete("/planta/" + PLANTA_ANA).header("Authorization", luis)).andExpect(status().isForbidden());
        mvc.perform(delete("/maceta/" + MACETA_ANA).header("Authorization", luis)).andExpect(status().isForbidden());
        mvc.perform(delete("/huerto/" + HUERTO_ANA).header("Authorization", luis)).andExpect(status().isForbidden());
        mvc.perform(delete("/persona/userName/ana").header("Authorization", luis)).andExpect(status().isForbidden());
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM planta WHERE idplanta = ?", Integer.class, PLANTA_ANA));

        mvc.perform(delete("/planta/" + PLANTA_ANA).header("Authorization", bearer("ana")))
                .andExpect(status().isOk()).andExpect(content().string("true"));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM planta WHERE idplanta = ?", Integer.class, PLANTA_ANA));
    }

    @Test
    void escriturasConElIdEnElCuerpo() throws Exception {
        String ana = bearer("ana"), luis = bearer("luis");
        // Luis intenta quedarse la maceta de Ana moviéndola a su huerto, o crear una en el huerto de Ana.
        mvc.perform(put("/maceta").header("Authorization", luis).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + MACETA_ANA + ",\"posicionMacetaenElHuerto\":\"x\",\"huertoIdhuerto\":" + HUERTO_LUIS + "}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/maceta").header("Authorization", luis).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"posicionMacetaenElHuerto\":\"x\",\"huertoIdhuerto\":" + HUERTO_ANA + "}"))
                .andExpect(status().isForbidden());
        // Ana mueve la suya dentro de su huerto: permitido.
        mvc.perform(put("/maceta").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + MACETA_ANA + ",\"posicionMacetaenElHuerto\":\"ventana\",\"huertoIdhuerto\":" + HUERTO_ANA + "}"))
                .andExpect(status().isOk());
        assertEquals("ventana", jdbc.queryForObject(
                "SELECT posicionmacetaenelhuerto FROM maceta WHERE idmaceta = ?", String.class, MACETA_ANA));

        mvc.perform(post("/planta").header("Authorization", luis).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombrePlanta\":\"intrusa\",\"tipoplantaIdtipoplanta\":1,\"macetaIdmaceta\":" + MACETA_ANA + "}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/depositoagua").header("Authorization", luis).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + DEPOSITO_ANA + ",\"cantidadMaxima\":1,\"huertoIdhuerto\":" + HUERTO_LUIS + "}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/macetahassensor").header("Authorization", luis).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":{\"macetaIdmaceta\":" + MACETA_LUIS + ",\"sensorIdsensor\":" + SENSOR_ANA + "}}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cadaUnoModificaSoloSuFicha() throws Exception {
        String ana = bearer("ana"), luis = bearer("luis");
        String claveAna = jdbc.queryForObject("SELECT passwordsha256 FROM persona WHERE id = ?", String.class, ANA);
        String cuerpoAna = "{\"id\":" + ANA + ",\"nombre\":\"Ana M.\",\"usuario\":\"ana\",\"passwordSHA256\":\"ana\"}";
        mvc.perform(put("/persona").header("Authorization", luis).contentType(MediaType.APPLICATION_JSON)
                .content(cuerpoAna.replace("ana\"}", "hackeada\"}"))).andExpect(status().isForbidden());
        assertEquals(claveAna, jdbc.queryForObject("SELECT passwordsha256 FROM persona WHERE id = ?", String.class, ANA));

        mvc.perform(put("/persona").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                .content(cuerpoAna.replace("ana\"}", "otra\"}"))).andExpect(status().isOk());
        assertEquals("Ana M.", jdbc.queryForObject("SELECT nombre FROM persona WHERE id = ?", String.class, ANA));
        // S2: la contraseña ya no se cambia por aquí (antes se guardaba en claro y la cuenta quedaba inservible).
        assertEquals(claveAna, jdbc.queryForObject("SELECT passwordsha256 FROM persona WHERE id = ?", String.class, ANA));

        // Editar la ficha sin mandar usuario ni contraseña (formulario de la web) no los borra.
        mvc.perform(put("/persona").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":" + ANA + ",\"nombre\":\"Ana María\"}")).andExpect(status().isOk());
        assertEquals(claveAna, jdbc.queryForObject("SELECT passwordsha256 FROM persona WHERE id = ?", String.class, ANA));
        assertEquals("ana", jdbc.queryForObject("SELECT usuario FROM persona WHERE id = ?", String.class, ANA));
        token("ana");
    }

    @Test
    void elHuertoNuevoQuedaAsociadoAQuienLoCrea() throws Exception {
        String ana = bearer("ana");
        // V5: el id lo genera la BD; el formulario ya no lo pide.
        mvc.perform(post("/huerto").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroMacetas\":3}"))
                .andExpect(status().isOk()).andExpect(content().string("true"));
        Integer nuevo = jdbc.queryForObject("SELECT idhuerto FROM huerto WHERE idhuerto NOT IN (?, ?)", Integer.class,
                HUERTO_ANA, HUERTO_LUIS);
        assertEquals(3, jdbc.queryForObject("SELECT numeromacetas FROM huerto WHERE idhuerto = ?", Integer.class, nuevo));
        mvc.perform(get("/huerto/userName/ana").header("Authorization", ana))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(HUERTO_ANA, nuevo)));
        mvc.perform(get("/huerto/" + nuevo).header("Authorization", bearer("luis"))).andExpect(status().isForbidden());
        // Mandar el id de un huerto ajeno no lo pisa ni lo asocia: se crea otro huerto nuevo para quien lo pide.
        mvc.perform(post("/huerto").header("Authorization", bearer("luis")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":" + HUERTO_ANA + ",\"numeroMacetas\":9}")).andExpect(content().string("true"));
        assertEquals(1, jdbc.queryForObject("SELECT numeromacetas FROM huerto WHERE idhuerto = ?", Integer.class, HUERTO_ANA));
        mvc.perform(get("/huerto/" + HUERTO_ANA).header("Authorization", bearer("luis"))).andExpect(status().isForbidden());
    }

    @Test
    void sensorDesdeElEsp32YDesdeLaWeb() throws Exception {
        String lectura = "{\"id\":" + SENSOR_ANA + ",\"nombreSensor\":\"humedad-ana\",\"magnitudAMedir\":\"Humedad\","
                + "\"cantidadMedida\":\"61.5\",\"unidades\":\"%\"}";
        mvc.perform(put("/auth/sensor").contentType(MediaType.APPLICATION_JSON).content(lectura))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/auth/sensor").header(CABECERA, "clave-mala").contentType(MediaType.APPLICATION_JSON)
                .content(lectura)).andExpect(status().isUnauthorized());
        mvc.perform(put("/auth/sensor").header(CABECERA, CLAVE_DISPOSITIVO).contentType(MediaType.APPLICATION_JSON)
                .content(lectura)).andExpect(status().isOk()).andExpect(content().string("true"));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM lectura WHERE maceta_id = ? AND magnitud = 'HUMEDAD'",
                Integer.class, MACETA_ANA));
        assertEquals(61.5f, jdbc.queryForObject("SELECT nivelactualhumedad FROM planta WHERE idplanta = ?",
                Float.class, PLANTA_ANA), 0.01f);

        // Desde la web (JWT) solo sus propios sensores.
        String deLuis = lectura.replace(String.valueOf(SENSOR_ANA), String.valueOf(SENSOR_LUIS));
        mvc.perform(put("/auth/sensor").header("Authorization", bearer("ana")).contentType(MediaType.APPLICATION_JSON)
                .content(deLuis)).andExpect(status().isForbidden());
    }
}
