package es.uah.huertojpa.alertas;

import es.uah.huertojpa.metricas.MetricasService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Revisión periódica de alertas (por defecto cada 5 minutos; {@code ALERTAS_INTERVALO}, formato ISO-8601 como
 * {@code PT5M}). Se desactiva con {@code ALERTAS_ACTIVAS=false} (los tests lo hacen y llaman al servicio a mano).
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "alertas.activas", havingValue = "true", matchIfMissing = true)
public class AlertasProgramadas {
    private static final Logger log = LoggerFactory.getLogger(AlertasProgramadas.class);

    private final AlertaService servicio;
    private final MetricasService metricas;

    public AlertasProgramadas(AlertaService servicio, MetricasService metricas) {
        this.servicio = servicio;
        this.metricas = metricas;
    }

    @Scheduled(initialDelayString = "PT1M", fixedDelayString = "${alertas.intervalo:PT5M}")
    public void revisar() {
        int avisadas = servicio.revisarTodas(metricas.ahora());
        if (avisadas > 0) log.info("Alertas avisadas por correo en esta revisión: {}", avisadas);
    }
}
