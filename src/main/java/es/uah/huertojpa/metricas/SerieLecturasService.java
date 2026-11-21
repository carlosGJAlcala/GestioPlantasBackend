package es.uah.huertojpa.metricas;

import es.uah.huertojpa.alertas.EstadoMaceta;
import es.uah.huertojpa.depositoAgua.dominio.DepositoAgua;
import es.uah.huertojpa.depositoAgua.dominio.DepositoaguaRepository;
import es.uah.huertojpa.metricas.MetricasMaceta.Rango;
import es.uah.huertojpa.registro.dominio.repository.LecturaRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/** W7 – Serie agregada de lecturas para la gráfica del panel. */
@Service
public class SerieLecturasService {
    /** Pasos «redondos» (minutos) entre los que se elige el más fino que no supere {@link #PUNTOS_OBJETIVO}. */
    static final int[] PASOS_MINUTOS = {1, 5, 10, 15, 30, 60, 120, 180, 360, 720, 1440};
    static final int PUNTOS_OBJETIVO = 200;
    static final int PUNTOS_MAXIMOS = 2000;
    static final Duration RANGO_MAXIMO = Duration.ofDays(MetricasService.HORAS_MAXIMAS / 24);
    private static final Pattern MAGNITUD = Pattern.compile("[A-Z_]{1,20}");

    private final LecturaRepository lecturas;
    private final MetricasService metricas;
    private final DepositoaguaRepository depositos;
    private final Clock clock;

    public SerieLecturasService(LecturaRepository lecturas, MetricasService metricas, DepositoaguaRepository depositos) {
        this.lecturas = lecturas;
        this.metricas = metricas;
        this.depositos = depositos;
        this.clock = Clock.system(ZoneId.of("Europe/Madrid"));
    }

    /**
     * @param desde       por defecto {@code hasta - 7 días}
     * @param hasta       por defecto ahora
     * @param pasoMinutos opcional; si no se da (o daría más de {@link #PUNTOS_MAXIMOS} puntos) se elige automáticamente
     * @throws IllegalArgumentException magnitud no válida o {@code desde >= hasta}
     */
    public SerieLecturas serie(Integer idMaceta, String magnitud, LocalDateTime desde, LocalDateTime hasta,
                               Integer pasoMinutos) {
        String mag = magnitud == null ? "HUMEDAD" : magnitud.trim().toUpperCase(Locale.ROOT);
        if (!MAGNITUD.matcher(mag).matches()) {
            throw new IllegalArgumentException("magnitud no válida: " + magnitud);
        }
        LocalDateTime[] r = rango(desde, hasta);
        desde = r[0];
        hasta = r[1];
        int paso = elegirPaso(Duration.between(desde, hasta), pasoMinutos);

        List<SerieLecturas.Punto> puntos = lecturas.serieAgregada(idMaceta, mag, desde, hasta, paso * 60.0).stream()
                .map(SerieLecturasService::punto).toList();
        Rango rango = metricas.rango(idMaceta, mag);
        return new SerieLecturas(idMaceta, mag, desde, hasta, paso, rango.min(), rango.max(), puntos);
    }

    /**
     * A4 – Serie del nivel del depósito (%) de un huerto, para la gráfica del panel. La banda sombreada va del
     * aviso de «Depósito bajo» al 100 %. Vacío si el huerto no tiene depósito, ultrasonidos o altura configurada.
     */
    public Optional<SerieLecturas> serieDeposito(Integer idHuerto, LocalDateTime desde, LocalDateTime hasta,
                                                 Integer pasoMinutos) {
        Optional<DepositoAgua> d = depositos.findByHuertoIdhuerto(idHuerto);
        if (d.isEmpty() || d.get().getSensorIdsensor() == null || d.get().getAlturaDeposito() == null
                || d.get().getAlturaDeposito() <= 0) {
            return Optional.empty();
        }
        LocalDateTime[] rango = rango(desde, hasta);
        int paso = elegirPaso(Duration.between(rango[0], rango[1]), pasoMinutos);
        List<SerieLecturas.Punto> puntos = lecturas.serieDeposito(d.get().getSensorIdsensor().getId(),
                        d.get().getAlturaDeposito(), rango[0], rango[1], paso * 60.0).stream()
                .map(SerieLecturasService::punto).toList();
        return Optional.of(new SerieLecturas(null, "DEPOSITO", rango[0], rango[1], paso,
                EstadoMaceta.NIVEL_DEPOSITO_AVISO, 100f, puntos));
    }

    /** [desde, hasta] con los valores por defecto (7 días hasta ahora) y acotado a {@link #RANGO_MAXIMO}. */
    private LocalDateTime[] rango(LocalDateTime desde, LocalDateTime hasta) {
        if (hasta == null) hasta = LocalDateTime.now(clock);
        if (desde == null) desde = hasta.minusDays(7);
        if (!desde.isBefore(hasta)) {
            throw new IllegalArgumentException("'desde' tiene que ser anterior a 'hasta'");
        }
        if (Duration.between(desde, hasta).compareTo(RANGO_MAXIMO) > 0) {
            desde = hasta.minus(RANGO_MAXIMO);
        }
        return new LocalDateTime[]{desde, hasta};
    }

    /** Paso pedido (acotado para no superar {@link #PUNTOS_MAXIMOS}) o el paso redondo más fino que da ≤ 200 puntos. */
    static int elegirPaso(Duration rango, Integer pedido) {
        long minutos = Math.max(1, rango.toMinutes());
        int minimo = (int) Math.ceil(minutos / (double) PUNTOS_MAXIMOS);
        if (pedido != null && pedido > 0) {
            return Math.max(pedido, minimo);
        }
        for (int p : PASOS_MINUTOS) {
            if (minutos / p <= PUNTOS_OBJETIVO) return p;
        }
        return Math.max(PASOS_MINUTOS[PASOS_MINUTOS.length - 1], minimo);
    }

    private static SerieLecturas.Punto punto(Object[] fila) {
        Object ts = fila[0];
        LocalDateTime inicio = ts instanceof Timestamp t ? t.toLocalDateTime() : (LocalDateTime) ts;
        return new SerieLecturas.Punto(inicio, redondear(fila[1]), redondear(fila[2]), redondear(fila[3]),
                ((Number) fila[4]).longValue());
    }

    private static double redondear(Object n) {
        return Math.round(((Number) n).doubleValue() * 100) / 100.0;
    }
}
