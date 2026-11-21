package es.uah.huertojpa.integracion;

import es.uah.huertojpa.alertas.Alerta;
import es.uah.huertojpa.alertas.AlertaRepository;
import es.uah.huertojpa.alertas.AlertaService;
import es.uah.huertojpa.alertas.NivelAlerta;
import es.uah.huertojpa.alertas.NotificadorAlertas;
import es.uah.huertojpa.alertas.TipoAlerta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A2 (depósito bajo como alerta, un correo por huerto) y A3 («Entendido» y preferencia de avisos por correo)
 * contra PostgreSQL real. El depósito de Ana (20 cm de alto) pasa a tener el ultrasonidos 123.
 */
class AvisosDepositoIntegracionTest extends IntegracionPostgres {
    static final LocalDateTime T = LocalDateTime.of(2026, 11, 13, 10, 0);
    static final int SENSOR_DEPOSITO = 123, MACETA_ANA_2 = 112;

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private AlertaService servicio;
    @Autowired
    private AlertaRepository repo;
    @MockBean
    private NotificadorAlertas notificador;

    @BeforeEach
    void preparar() {
        when(notificador.notificar(anyString(), anyList(), anyList())).thenReturn(true);
        jdbc.update("INSERT INTO sensor (idsensor, nombresensor, magnitudamedir, cantidadmedida, unidades) VALUES (?, 'ultrasonidos', 'Distancia', '5', 'cm')",
                SENSOR_DEPOSITO);
        jdbc.update("UPDATE depositoagua SET sensor_idsensor = ? WHERE iddepositoagua = ?", SENSOR_DEPOSITO, DEPOSITO_ANA);
    }

    private void humedad(int maceta, float valor, LocalDateTime ts) {
        jdbc.update("INSERT INTO lectura (sensor_id, maceta_id, magnitud, valor, ts) VALUES (?, ?, 'HUMEDAD', ?, ?)",
                SENSOR_ANA, maceta, valor, ts);
    }

    /** Distancia del ultrasonidos al agua (cm): 18 cm en un depósito de 20 ⇒ 10 %. */
    private void distancia(float cm, LocalDateTime ts) {
        jdbc.update("INSERT INTO lectura (sensor_id, maceta_id, magnitud, valor, ts) VALUES (?, NULL, 'DISTANCIA', ?, ?)",
                SENSOR_DEPOSITO, cm, ts);
    }

    private List<Alerta> abiertas(int maceta) {
        return repo.findByMacetaIdAndFinIsNull(maceta);
    }

    @Test
    @SuppressWarnings("unchecked")
    void depositoVacioEsCriticoYSeAvisaEnseguida() {
        humedad(MACETA_ANA, 70, T.minusMinutes(1));
        distancia(18.2f, T.minusMinutes(1));
        servicio.revisarTodas(T);

        List<Alerta> a = abiertas(MACETA_ANA);
        assertEquals(1, a.size());
        assertEquals(TipoAlerta.DEPOSITO_BAJO, a.get(0).getTipo());
        assertEquals(NivelAlerta.CRITICO, a.get(0).getNivel());
        ArgumentCaptor<List<Alerta>> enviadas = ArgumentCaptor.forClass(List.class);
        verify(notificador).notificar(eq("Tomate de Ana (balcon)"), eq(List.of("ana@example.com")), enviadas.capture());
        assertTrue(enviadas.getValue().get(0).getMensaje().contains("9 %"));

        // Se rellena: la alerta se cierra pasada la espera, sin más correos.
        distancia(2f, T.plusMinutes(5));
        humedad(MACETA_ANA, 70, T.plusMinutes(5));
        servicio.revisarTodas(T.plusMinutes(6));
        distancia(2f, T.plusMinutes(40));
        humedad(MACETA_ANA, 70, T.plusMinutes(40));
        servicio.revisarTodas(T.plusMinutes(41));
        assertTrue(abiertas(MACETA_ANA).isEmpty());
        verify(notificador, times(1)).notificar(anyString(), anyList(), anyList());
    }

    @Test
    void elDepositoSeAvisaUnaVezPorHuerto() {
        // Segunda maceta en el mismo huerto (mismo depósito).
        jdbc.update("INSERT INTO maceta (idmaceta, posicionmacetaenelhuerto, huerto_idhuerto) VALUES (?, 'ventana', ?)",
                MACETA_ANA_2, HUERTO_ANA);
        humedad(MACETA_ANA, 70, T.minusMinutes(1));
        humedad(MACETA_ANA_2, 70, T.minusMinutes(1));
        distancia(19f, T.minusMinutes(1));
        servicio.revisarTodas(T);

        assertEquals(TipoAlerta.DEPOSITO_BAJO, abiertas(MACETA_ANA).get(0).getTipo());
        assertEquals(TipoAlerta.DEPOSITO_BAJO, abiertas(MACETA_ANA_2).get(0).getTipo());
        verify(notificador, times(1)).notificar(anyString(), anyList(), anyList());
        // Las dos quedan marcadas: no se reintenta en la siguiente vuelta.
        assertNotNull(abiertas(MACETA_ANA).get(0).getNotificada());
        assertNotNull(abiertas(MACETA_ANA_2).get(0).getNotificada());
        humedad(MACETA_ANA, 70, T.plusMinutes(5));
        humedad(MACETA_ANA_2, 70, T.plusMinutes(5));
        distancia(19f, T.plusMinutes(5));
        servicio.revisarTodas(T.plusMinutes(6));
        verify(notificador, times(1)).notificar(anyString(), anyList(), anyList());
    }

    @Test
    void lasMetricasIncluyenElDeposito() throws Exception {
        jdbc.update("""
                INSERT INTO lectura (sensor_id, maceta_id, magnitud, valor, ts) VALUES
                  (?, NULL, 'DISTANCIA', 16, (now() AT TIME ZONE 'Europe/Madrid') - interval '1 minute'),
                  (?, ?, 'HUMEDAD', 70, (now() AT TIME ZONE 'Europe/Madrid') - interval '1 minute')
                """, SENSOR_DEPOSITO, SENSOR_ANA, MACETA_ANA);
        mvc.perform(get("/metricas/maceta/" + MACETA_ANA).header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deposito.nivel").value(20.0))
                .andExpect(jsonPath("$.deposito.minutosDesdeLectura").value(1))
                .andExpect(jsonPath("$.estado.texto").value("Todo bien")); // 20 % justo: todavía no avisa
        // La maceta de Luis no tiene depósito.
        mvc.perform(get("/metricas/maceta/" + MACETA_LUIS).header("Authorization", bearer("luis")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deposito.nivel").doesNotExist());
    }

    @Test
    void entendidoSilenciaElCorreoPeroLaAlertaSigueAbierta() throws Exception {
        humedad(MACETA_ANA, 40, T.minusMinutes(1));   // tierra seca: aviso, se espera 30 min para el correo
        servicio.revisarTodas(T);
        Long id = abiertas(MACETA_ANA).get(0).getId();
        String url = "/alertas/" + id + "/silenciar";

        mvc.perform(post(url)).andExpect(status().isUnauthorized());
        mvc.perform(post(url).header("Authorization", bearer("luis"))).andExpect(status().isForbidden());
        // La clave de la placa solo vale en /auth/**: aquí no autentica.
        mvc.perform(post(url).header("X-Device-Key", CLAVE_DISPOSITIVO)).andExpect(status().isUnauthorized());
        mvc.perform(post("/alertas/999999/silenciar").header("Authorization", bearer("ana")))
                .andExpect(status().isForbidden());
        mvc.perform(post(url).header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.silenciada").exists())
                .andExpect(jsonPath("$.abierta").value(true));

        humedad(MACETA_ANA, 41, T.plusMinutes(30));
        servicio.revisarTodas(T.plusMinutes(31));
        verifyNoInteractions(notificador);
        mvc.perform(get("/alertas").header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].silenciada").exists());
    }

    @Test
    void quienDesactivaLosAvisosNoRecibeCorreo() throws Exception {
        String ana = bearer("ana");
        mvc.perform(get("/persona/userName/ana").header("Authorization", ana))
                .andExpect(status().isOk()).andExpect(jsonPath("$.avisosCorreo").value(true));
        mvc.perform(put("/persona").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":" + ANA + ",\"nombre\":\"Ana\",\"avisosCorreo\":false}")).andExpect(status().isOk());
        // Un formulario que no manda la casilla no la vuelve a activar.
        mvc.perform(put("/persona").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":" + ANA + ",\"nombre\":\"Ana M.\"}")).andExpect(status().isOk());
        assertEquals(Boolean.FALSE, jdbc.queryForObject("SELECT avisos_correo FROM persona WHERE id = ?", Boolean.class, ANA));
        assertEquals("ana@example.com", jdbc.queryForObject("SELECT correoelectronico FROM persona WHERE id = ?", String.class, ANA));
        // El correo ya se puede cambiar desde la ficha (antes la consulta lo ignoraba).
        mvc.perform(put("/persona").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":" + ANA + ",\"nombre\":\"Ana M.\",\"correoElectronico\":\"ana2@example.com\"}"))
                .andExpect(status().isOk());
        assertEquals("ana2@example.com", jdbc.queryForObject("SELECT correoelectronico FROM persona WHERE id = ?", String.class, ANA));

        humedad(MACETA_ANA, 70, T.minusMinutes(1));
        distancia(19f, T.minusMinutes(1));
        servicio.revisarTodas(T);
        verify(notificador).notificar(anyString(), eq(List.of()), anyList()); // sin destinatarios
    }

    @Test
    void laCasillaDeMisDatosDevuelveLaFichaTalCual() throws Exception {
        // El front («Mis datos») manda de vuelta el JSON que recibió (con authorities, enabled… de UserDetails).
        String ana = bearer("ana");
        String ficha = mvc.perform(get("/persona/userName/ana").header("Authorization", ana))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        mvc.perform(put("/persona").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                .content(ficha.replace("\"avisosCorreo\":true", "\"avisosCorreo\":false")))
                .andExpect(status().isOk());
        assertEquals(Boolean.FALSE, jdbc.queryForObject("SELECT avisos_correo FROM persona WHERE id = ?", Boolean.class, ANA));
        token("ana"); // la contraseña sigue valiendo
    }
}
