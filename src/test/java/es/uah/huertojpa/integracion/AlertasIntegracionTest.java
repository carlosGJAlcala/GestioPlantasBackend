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
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Alertas contra PostgreSQL real: apertura, aviso único por correo (con espera para los avisos y sin ella para lo
 * crítico), cierre con histéresis, índice único de alertas abiertas y permisos de los endpoints.
 * El correo se sustituye por un mock; el planificador está desactivado y se llama a {@code revisarTodas(ahora)}.
 */
class AlertasIntegracionTest extends IntegracionPostgres {
    static final LocalDateTime T = LocalDateTime.of(2026, 11, 10, 10, 0);

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private AlertaService servicio;
    @Autowired
    private AlertaRepository repo;
    @MockBean
    private NotificadorAlertas notificador;

    @BeforeEach
    void correoFunciona() {
        when(notificador.notificar(anyString(), anyList(), anyList())).thenReturn(true);
    }

    /** Lectura de humedad y temperatura de la maceta de Ana en el instante dado. */
    private void lectura(float humedad, LocalDateTime ts) {
        jdbc.update("INSERT INTO lectura (sensor_id, maceta_id, magnitud, valor, ts) VALUES (?, ?, 'HUMEDAD', ?, ?), (?, ?, 'TEMPERATURA', 20, ?)",
                SENSOR_ANA, MACETA_ANA, humedad, ts, SENSOR_ANA, MACETA_ANA, ts);
    }

    private List<Alerta> abiertasAna() {
        return repo.findByMacetaIdAndFinIsNull(MACETA_ANA);
    }

    @Test
    @SuppressWarnings("unchecked")
    void tierraSecaSeAvisaUnaVezYSeCierraConHisteresis() {
        lectura(40, T.minusMinutes(1));               // tomate: 60-80 %
        servicio.revisarTodas(T);
        assertEquals(1, abiertasAna().size());
        assertEquals(TipoAlerta.TIERRA_SECA, abiertasAna().get(0).getTipo());
        assertTrue(repo.findByMacetaIdAndFinIsNull(MACETA_LUIS).isEmpty()); // Luis no tiene lecturas
        verifyNoInteractions(notificador);            // aviso: espera 30 min antes de mandar correo

        lectura(41, T.plusMinutes(30));
        servicio.revisarTodas(T.plusMinutes(31));
        ArgumentCaptor<List<Alerta>> enviadas = ArgumentCaptor.forClass(List.class);
        verify(notificador).notificar(eq("Tomate de Ana (balcon)"), eq(List.of("ana@example.com")), enviadas.capture());
        assertEquals(TipoAlerta.TIERRA_SECA, enviadas.getValue().get(0).getTipo());
        assertTrue(enviadas.getValue().get(0).getMensaje().contains("41 %"));

        lectura(42, T.plusMinutes(35));
        servicio.revisarTodas(T.plusMinutes(36));
        verify(notificador, times(1)).notificar(anyString(), anyList(), anyList()); // no se repite
        assertEquals(1, repo.count());

        // Riega: la humedad vuelve al rango, pero la alerta no se cierra hasta pasados 30 min sin verla.
        lectura(70, T.plusMinutes(40));
        servicio.revisarTodas(T.plusMinutes(41));
        assertEquals(1, abiertasAna().size());
        lectura(70, T.plusMinutes(65));
        servicio.revisarTodas(T.plusMinutes(66));
        assertTrue(abiertasAna().isEmpty());
        Alerta cerrada = repo.findAll().get(0);
        assertEquals(T.plusMinutes(66), cerrada.getFin());
        assertEquals(T.plusMinutes(31), cerrada.getNotificada());
    }

    @Test
    void lasCriticasSeAvisanEnseguidaYUnaBreveNoSeAvisa() {
        lectura(70, T.minusMinutes(1));
        jdbc.update("INSERT INTO riego (maceta_id, ts, pulsos, resultado) VALUES (?, ?, 0, 'DEPOSITO_VACIO')",
                MACETA_ANA, T.minusMinutes(5));
        servicio.revisarTodas(T);
        List<Alerta> abiertas = abiertasAna();
        assertEquals(1, abiertas.size());
        assertEquals(TipoAlerta.RIEGO_FALLIDO, abiertas.get(0).getTipo());
        assertEquals(NivelAlerta.CRITICO, abiertas.get(0).getNivel());
        assertEquals(T, abiertas.get(0).getNotificada());
        verify(notificador).notificar(anyString(), anyList(), anyList());

        // Tierra seca 10 min y vuelve: se abre y se cierra sin correo.
        jdbc.update("INSERT INTO riego (maceta_id, ts, pulsos, resultado, humedad_antes, humedad_despues) VALUES (?, ?, 3, 'OK', 55, 68)",
                MACETA_ANA, T.plusMinutes(1));
        lectura(50, T.plusMinutes(2));
        servicio.revisarTodas(T.plusMinutes(3));
        lectura(70, T.plusMinutes(12));
        servicio.revisarTodas(T.plusMinutes(13));
        lectura(70, T.plusMinutes(50));
        servicio.revisarTodas(T.plusMinutes(51));
        assertTrue(abiertasAna().isEmpty());
        verify(notificador, times(1)).notificar(anyString(), anyList(), anyList());
    }

    @Test
    void placaSinConexion() {
        lectura(70, T.minusMinutes(45));
        servicio.revisarTodas(T);
        assertEquals(TipoAlerta.PLACA_DESCONECTADA, abiertasAna().get(0).getTipo());
        assertTrue(abiertasAna().get(0).getMensaje().contains("45 min"));
    }

    @Test
    void endpointsSoloMuestranLasMacetasPropias() throws Exception {
        lectura(40, T.minusMinutes(1));
        servicio.revisarTodas(T);

        mvc.perform(get("/alertas").header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo").value("TIERRA_SECA"))
                .andExpect(jsonPath("$[0].titulo").value("Tierra seca"))
                .andExpect(jsonPath("$[0].macetaId").value(MACETA_ANA))
                .andExpect(jsonPath("$[0].abierta").value(true))
                .andExpect(jsonPath("$[0].inicio").value("2026-11-10T10:00:00"));
        mvc.perform(get("/alertas").header("Authorization", bearer("luis")))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/alertas").header("Authorization", bearer("admin")))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/alertas")).andExpect(status().isUnauthorized());

        String url = "/alertas/maceta/" + MACETA_ANA + "?dias=3650";
        mvc.perform(get(url).header("Authorization", bearer("ana"))).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get(url).header("Authorization", bearer("luis"))).andExpect(status().isForbidden());
        mvc.perform(get(url).header("Authorization", bearer("admin"))).andExpect(status().isOk());
    }

    @Test
    void lasMetricasIncluyenElEstadoGlobal() throws Exception {
        // Lectura «de ahora» en la hora del huerto (el backend usa Europe/Madrid).
        jdbc.update("""
                INSERT INTO lectura (sensor_id, maceta_id, magnitud, valor, ts) VALUES
                  (?, ?, 'HUMEDAD', 90, (now() AT TIME ZONE 'Europe/Madrid') - interval '1 minute')
                """, SENSOR_ANA, MACETA_ANA);
        mvc.perform(get("/metricas/maceta/" + MACETA_ANA + "?horas=24").header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.nivel").value("AVISO"))
                .andExpect(jsonPath("$.estado.texto").value("Exceso de agua"))
                .andExpect(jsonPath("$.estado.condiciones[0].tipo").value("EXCESO_AGUA"))
                .andExpect(jsonPath("$.estado.condiciones[0].titulo").value("Exceso de agua"));
    }
}
