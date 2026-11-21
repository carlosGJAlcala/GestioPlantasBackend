package es.uah.huertojpa.integracion;

import es.uah.huertojpa.alertas.Alerta;
import es.uah.huertojpa.alertas.AlertaRepository;
import es.uah.huertojpa.alertas.AlertaService;
import es.uah.huertojpa.alertas.NotificadorAlertas;
import es.uah.huertojpa.alertas.TipoAlerta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A4 (depósito en litros y serie para la gráfica) y A5 (bajada sin riegos ⇒ posible fuga) contra PostgreSQL real.
 * El depósito de Ana: 5 L, 20 cm de alto, con el ultrasonidos 123.
 */
class DepositoLitrosFugaIntegracionTest extends IntegracionPostgres {
    static final LocalDateTime T = LocalDateTime.of(2026, 11, 18, 12, 0);
    static final int SENSOR_DEPOSITO = 123;

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

    /** Distancia (cm) del sensor al agua: 20 cm de alto ⇒ cada cm son 5 puntos. */
    private void distancia(float cm, LocalDateTime ts) {
        jdbc.update("INSERT INTO lectura (sensor_id, maceta_id, magnitud, valor, ts) VALUES (?, NULL, 'DISTANCIA', ?, ?)",
                SENSOR_DEPOSITO, cm, ts);
    }

    private void humedad(float valor, LocalDateTime ts) {
        jdbc.update("INSERT INTO lectura (sensor_id, maceta_id, magnitud, valor, ts) VALUES (?, ?, 'HUMEDAD', ?, ?)",
                SENSOR_ANA, MACETA_ANA, valor, ts);
    }

    /** Seis horas de lecturas cada 5 min hasta `fin`, bajando de 4 a 8 cm (del 80 % al 60 %: 20 puntos, 1 L). */
    private void bajadaDeSeisHoras(LocalDateTime fin) {
        for (int m = 360; m >= 0; m -= 5) {
            distancia(4 + 4 * (360 - m) / 360f, fin.minusMinutes(m));
            humedad(70, fin.minusMinutes(m));
        }
    }

    private List<Alerta> abiertas() {
        return repo.findByMacetaIdAndFinIsNull(MACETA_ANA);
    }

    @Test
    void bajadaSinRiegosAbreFugaYUnRiegoLaExplica() {
        bajadaDeSeisHoras(T);
        servicio.revisarTodas(T);
        List<Alerta> a = abiertas();
        assertEquals(List.of(TipoAlerta.FUGA_DEPOSITO), a.stream().map(Alerta::getTipo).toList());
        assertTrue(a.get(0).getMensaje().contains(" L)"), a.get(0).getMensaje());
        verifyNoInteractions(notificador); // es un aviso: el correo espera 30 min

        // Mismo caso, pero la maceta regó hace 1 h: la bajada la explica la bomba.
        jdbc.update("DELETE FROM alerta");
        jdbc.update("INSERT INTO riego (maceta_id, ts, pulsos, resultado) VALUES (?, ?, 2, 'OK')", MACETA_ANA, T.minusHours(1));
        servicio.revisarTodas(T);
        assertTrue(abiertas().isEmpty());
    }

    @Test
    void unRiegoSinBombaNoExplicaLaBajada() {
        bajadaDeSeisHoras(T);
        jdbc.update("INSERT INTO riego (maceta_id, ts, pulsos, resultado) VALUES (?, ?, 0, 'HUMEDAD_MAXIMA')", MACETA_ANA, T.minusHours(1));
        servicio.revisarTodas(T);
        assertEquals(TipoAlerta.FUGA_DEPOSITO, abiertas().get(0).getTipo());
    }

    @Test
    void metricasEnLitrosYSerieDelDeposito() throws Exception {
        // 4 cm de 20 ⇒ 80 % de 5 L = 4 L. Con hora del servidor (las métricas usan «ahora»).
        jdbc.update("""
                INSERT INTO lectura (sensor_id, maceta_id, magnitud, valor, ts) VALUES
                  (?, NULL, 'DISTANCIA', 4, (now() AT TIME ZONE 'Europe/Madrid') - interval '3 minute'),
                  (?, NULL, 'DISTANCIA', -1, (now() AT TIME ZONE 'Europe/Madrid') - interval '2 minute'),
                  (?, NULL, 'DISTANCIA', 4, (now() AT TIME ZONE 'Europe/Madrid') - interval '1 minute')
                """, SENSOR_DEPOSITO, SENSOR_DEPOSITO, SENSOR_DEPOSITO);
        String ana = bearer("ana");
        mvc.perform(get("/metricas/maceta/" + MACETA_ANA).header("Authorization", ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deposito.nivel").value(80.0))
                .andExpect(jsonPath("$.deposito.capacidadLitros").value(5.0))
                .andExpect(jsonPath("$.deposito.litros").value(4.0));

        mvc.perform(get("/lectura/deposito/huerto/" + HUERTO_ANA).header("Authorization", ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.magnitud").value("DEPOSITO"))
                .andExpect(jsonPath("$.rangoMinimo").value(20.0))
                .andExpect(jsonPath("$.puntos[0].media").value(80.0))
                .andExpect(jsonPath("$.puntos[0].lecturas").value(2)); // la de -1 (sin eco) no cuenta
        mvc.perform(get("/lectura/deposito/huerto/" + HUERTO_ANA).header("Authorization", bearer("admin")))
                .andExpect(status().isOk());
        mvc.perform(get("/lectura/deposito/huerto/" + HUERTO_ANA).header("Authorization", bearer("luis")))
                .andExpect(status().isForbidden());
        // El huerto de Luis no tiene depósito.
        mvc.perform(get("/lectura/deposito/huerto/" + HUERTO_LUIS).header("Authorization", bearer("luis")))
                .andExpect(status().isNotFound());
    }
}
