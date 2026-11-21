package es.uah.huertojpa.metricas;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import es.uah.huertojpa.alertas.EstadoMaceta;
import es.uah.huertojpa.registro.dominio.entities.Lectura;
import es.uah.huertojpa.riego.dominio.EstadoOrden;
import es.uah.huertojpa.riego.dominio.OrdenRiego;
import es.uah.huertojpa.riego.dominio.ResultadoRiego;
import es.uah.huertojpa.riego.dominio.Riego;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Métricas de estado de una maceta en las últimas {@code horas} horas, calculadas sobre la tabla {@code lectura}
 * (P-DB1) y los rangos del tipo de planta. Cálculo puro (sin BD) para poder probarlo con tests unitarios.
 *
 * <p>Las horas se ponderan por tiempo: cada lectura «vale» hasta la siguiente, con un máximo de
 * {@link #MAX_HUECO} (si la placa se apaga, ese hueco no cuenta como tiempo en rango ni fuera de él).</p>
 *
 * @param horas                       ventana analizada
 * @param humedadActual               última lectura de humedad del sustrato (%)
 * @param humedadMinima               mínimo del tipo de planta (umbral de riego del ESP32)
 * @param humedadMaxima               máximo del tipo de planta
 * @param horasConDatos               horas cubiertas por lecturas de humedad (cobertura de la placa)
 * @param porcentajeHumedadEnRango    % del tiempo con datos en que la humedad estuvo dentro de [mín, máx]
 * @param horasEstresHidrico          horas por debajo del mínimo (la planta pasó sed)
 * @param horasExcesoAgua             horas por encima del máximo (riesgo de pudrición de raíz)
 * @param tasaSecadoPorHora           puntos de humedad que pierde el sustrato por hora desde el último riego
 *                                    (pendiente por mínimos cuadrados; null si no hay datos suficientes)
 * @param proximoRiegoEstimado        cuándo llegará la humedad al mínimo a ese ritmo (= ahora si ya está por debajo)
 * @param horasHastaProximoRiego      lo mismo, en horas desde ahora
 * @param temperaturaMedia            media ponderada por tiempo (°C)
 * @param horasTemperaturaFueraRango  horas fuera del rango de temperatura del tipo de planta
 * @param horasLuzSuficientePorDia    horas al día (media) con luz ≥ mínimo del tipo de planta
 * @param ultimaLectura               última lectura de cualquier sensor de la maceta
 * @param minutosDesdeUltimaLectura   antigüedad de esa lectura
 * @param placaConectada              hay lecturas en los últimos {@link #MINUTOS_PLACA_DESCONECTADA} minutos
 * @param ordenesRiego                órdenes «Regar ahora» creadas en la ventana
 * @param segundosMediosEntregaOrden  media de (entregada − creada): latencia de la placa en recoger la orden
 * @param porcentajeOrdenesCaducadas  % de órdenes que la placa no recogió a tiempo (disponibilidad)
 * @param temperaturaActual           última lectura de temperatura (°C)
 * @param temperaturaMinima           mínimo de temperatura del tipo de planta
 * @param temperaturaMaxima           máximo de temperatura del tipo de planta
 * @param ultimoRiego                 último ciclo de riego registrado por la placa (con o sin bomba)
 * @param ultimoRiegoResultado        resultado que informó el ESP32 en ese ciclo
 * @param ultimoRiegoFallido          la bomba se activó y la humedad apenas subió (&lt; 2 puntos)
 * @param deposito                    A2: nivel y consumo del depósito del huerto de la maceta (nunca null)
 */
public record MetricasMaceta(
        int horas,
        Float humedadActual,
        Float humedadMinima,
        Float humedadMaxima,
        double horasConDatos,
        Double porcentajeHumedadEnRango,
        double horasEstresHidrico,
        double horasExcesoAgua,
        Double tasaSecadoPorHora,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime proximoRiegoEstimado,
        Double horasHastaProximoRiego,
        Double temperaturaMedia,
        double horasTemperaturaFueraRango,
        Double horasLuzSuficientePorDia,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime ultimaLectura,
        Long minutosDesdeUltimaLectura,
        boolean placaConectada,
        int ordenesRiego,
        Double segundosMediosEntregaOrden,
        Double porcentajeOrdenesCaducadas,
        Float temperaturaActual,
        Float temperaturaMinima,
        Float temperaturaMaxima,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime ultimoRiego,
        ResultadoRiego ultimoRiegoResultado,
        boolean ultimoRiegoFallido,
        NivelDeposito deposito) {

    /** Un hueco mayor entre lecturas se considera «sin datos» (el ESP32 publica cada pocos minutos). */
    static final Duration MAX_HUECO = Duration.ofMinutes(30);
    static final long MINUTOS_PLACA_DESCONECTADA = 30;
    /** Por debajo de esta pérdida (pts/h) se considera que la tierra no se está secando (ruido del sensor). */
    static final double TASA_SECADO_MINIMA = 0.05;
    /** Ventana máxima para estimar la tasa de secado: tras dos días sin regar la curva deja de ser lineal. */
    static final Duration VENTANA_SECADO = Duration.ofHours(48);

    /** Rango [min, max] de una magnitud; cualquiera de los extremos puede faltar. */
    public record Rango(Float min, Float max) {
        static final Rango VACIO = new Rango(null, null);

        int comparar(float v) {
            if (min != null && v < min) return -1;
            if (max != null && v > max) return 1;
            return 0;
        }
    }

    public static MetricasMaceta calcular(int horas, LocalDateTime ahora,
                                          List<Lectura> humedad, List<Lectura> temperatura, List<Lectura> luz,
                                          Rango rangoHumedad, Rango rangoTemperatura, Rango rangoLuz,
                                          List<Riego> riegos, List<OrdenRiego> ordenes, LocalDateTime ultimaLectura) {
        return calcular(horas, ahora, humedad, temperatura, luz, rangoHumedad, rangoTemperatura, rangoLuz,
                riegos, ordenes, ultimaLectura, NivelDeposito.SIN_DATOS);
    }

    public static MetricasMaceta calcular(int horas, LocalDateTime ahora,
                                          List<Lectura> humedad, List<Lectura> temperatura, List<Lectura> luz,
                                          Rango rangoHumedad, Rango rangoTemperatura, Rango rangoLuz,
                                          List<Riego> riegos, List<OrdenRiego> ordenes, LocalDateTime ultimaLectura,
                                          NivelDeposito deposito) {
        rangoHumedad = rangoHumedad == null ? Rango.VACIO : rangoHumedad;
        rangoTemperatura = rangoTemperatura == null ? Rango.VACIO : rangoTemperatura;
        rangoLuz = rangoLuz == null ? Rango.VACIO : rangoLuz;

        // --- Humedad ---
        double[] h = horasPorZona(humedad, rangoHumedad, ahora);
        double conDatos = h[0] + h[1] + h[2];
        Double enRango = conDatos > 0 ? redondear(100.0 * h[1] / conDatos) : null;
        Float humedadActual = humedad.isEmpty() ? null : humedad.get(humedad.size() - 1).getValor();

        LocalDateTime ultimoRiego = riegos.stream().filter(Riego::bombaActivada).map(Riego::getTs)
                .max(Comparator.naturalOrder()).orElse(null);
        Double tasa = tasaSecado(humedad, ultimoRiego, ahora);

        LocalDateTime proximo = null;
        Double horasHasta = null;
        if (humedadActual != null && rangoHumedad.min() != null) {
            if (humedadActual <= rangoHumedad.min()) {
                proximo = ahora;
                horasHasta = 0.0;
            } else if (tasa != null) {
                double hh = (humedadActual - rangoHumedad.min()) / tasa;
                horasHasta = redondear(hh);
                proximo = ahora.plusSeconds(Math.round(hh * 3600));
            }
        }

        // --- Temperatura ---
        double[] t = horasPorZona(temperatura, rangoTemperatura, ahora);
        Double tempMedia = mediaPonderada(temperatura, ahora);

        // --- Luz ---
        Double luzDia = null;
        if (rangoLuz.min() != null && !luz.isEmpty()) {
            double[] l = horasPorZona(luz, new Rango(rangoLuz.min(), null), ahora);
            double cobertura = l[0] + l[1];
            // Se normaliza por los días realmente cubiertos de datos, no por la ventana pedida.
            luzDia = cobertura > 0 ? redondear(l[1] / (cobertura / 24.0)) : null;
            if (luzDia != null && luzDia > 24) luzDia = 24.0;
        }

        Float temperaturaActual = temperatura.isEmpty() ? null : temperatura.get(temperatura.size() - 1).getValor();

        // --- Último ciclo de riego (con o sin bomba): para la alerta de riego fallido ---
        Riego ultimo = riegos.stream().filter(r -> r.getTs() != null)
                .max(Comparator.comparing(Riego::getTs)).orElse(null);

        // --- Placa ---
        Long minutos = ultimaLectura == null ? null : Duration.between(ultimaLectura, ahora).toMinutes();
        boolean conectada = minutos != null && minutos <= MINUTOS_PLACA_DESCONECTADA;

        // --- Órdenes «Regar ahora» ---
        Double latencia = ordenes.stream()
                .filter(o -> o.getEstado() == EstadoOrden.ENTREGADA && o.getEntregada() != null && o.getCreada() != null)
                .mapToLong(o -> Duration.between(o.getCreada(), o.getEntregada()).toSeconds())
                .average().stream().boxed().map(MetricasMaceta::redondear).findFirst().orElse(null);
        long caducadas = ordenes.stream().filter(o -> o.getEstado() == EstadoOrden.CADUCADA).count();
        long cerradas = ordenes.stream()
                .filter(o -> o.getEstado() == EstadoOrden.ENTREGADA || o.getEstado() == EstadoOrden.CADUCADA).count();
        Double pctCaducadas = cerradas == 0 ? null : redondear(100.0 * caducadas / cerradas);

        return new MetricasMaceta(horas, humedadActual, rangoHumedad.min(), rangoHumedad.max(),
                redondear(conDatos), enRango, redondear(h[0]), redondear(h[2]),
                tasa == null ? null : redondear(tasa), proximo, horasHasta,
                tempMedia, redondear(t[0] + t[2]), luzDia,
                ultimaLectura, minutos, conectada,
                ordenes.size(), latencia, pctCaducadas,
                temperaturaActual, rangoTemperatura.min(), rangoTemperatura.max(),
                ultimo == null ? null : ultimo.getTs(), ultimo == null ? null : ultimo.getResultado(),
                ultimo != null && ultimo.esFallido(),
                deposito == null ? NivelDeposito.SIN_DATOS : deposito);
    }

    /**
     * Estado global de la maceta (lo que pinta el panel y lo que vigilan las alertas). Se calcula aquí para que
     * la web y los correos digan exactamente lo mismo.
     */
    @JsonProperty("estado")
    public EstadoMaceta estado() {
        return EstadoMaceta.de(this);
    }

    /** Horas [por debajo, en rango, por encima] ponderadas por tiempo. Las lecturas deben venir ordenadas. */
    static double[] horasPorZona(List<Lectura> lecturas, Rango rango, LocalDateTime ahora) {
        double[] zonas = new double[3];
        for (int i = 0; i < lecturas.size(); i++) {
            Lectura l = lecturas.get(i);
            zonas[rango.comparar(l.getValor()) + 1] += duracionHoras(lecturas, i, ahora);
        }
        return zonas;
    }

    static Double mediaPonderada(List<Lectura> lecturas, LocalDateTime ahora) {
        double suma = 0, peso = 0;
        for (int i = 0; i < lecturas.size(); i++) {
            double d = duracionHoras(lecturas, i, ahora);
            suma += lecturas.get(i).getValor() * d;
            peso += d;
        }
        if (peso > 0) return redondear(suma / peso);
        // Lecturas sin duración (p. ej. una sola, recién llegada): media simple.
        return lecturas.isEmpty() ? null
                : redondear(lecturas.stream().mapToDouble(Lectura::getValor).average().orElse(0));
    }

    /** Tiempo que «vale» la lectura i: hasta la siguiente (o hasta ahora), con un máximo de MAX_HUECO. */
    private static double duracionHoras(List<Lectura> lecturas, int i, LocalDateTime ahora) {
        LocalDateTime desde = lecturas.get(i).getTs();
        LocalDateTime hasta = i + 1 < lecturas.size() ? lecturas.get(i + 1).getTs() : ahora;
        Duration d = Duration.between(desde, hasta);
        if (d.isNegative()) return 0;
        if (d.compareTo(MAX_HUECO) > 0) d = MAX_HUECO;
        return d.toSeconds() / 3600.0;
    }

    /**
     * Pérdida de humedad por hora (positiva) desde el último riego, por mínimos cuadrados sobre las lecturas de
     * las últimas {@link #VENTANA_SECADO}. Null si hay menos de 3 lecturas, abarcan menos de 1 hora o la
     * humedad no baja (p. ej. justo tras regar o con lluvia).
     */
    static Double tasaSecado(List<Lectura> humedad, LocalDateTime ultimoRiego, LocalDateTime ahora) {
        LocalDateTime desde = ahora.minus(VENTANA_SECADO);
        // Tras un riego el agua tarda unos minutos en repartirse: se ignoran los 15 primeros.
        if (ultimoRiego != null && ultimoRiego.plusMinutes(15).isAfter(desde)) desde = ultimoRiego.plusMinutes(15);
        final LocalDateTime inicio = desde;
        List<Lectura> tramo = humedad.stream().filter(l -> !l.getTs().isBefore(inicio)).toList();
        if (tramo.size() < 3) return null;
        LocalDateTime t0 = tramo.get(0).getTs();
        double span = Duration.between(t0, tramo.get(tramo.size() - 1).getTs()).toSeconds() / 3600.0;
        if (span < 1.0) return null;

        double n = tramo.size(), sx = 0, sy = 0, sxx = 0, sxy = 0;
        for (Lectura l : tramo) {
            double x = Duration.between(t0, l.getTs()).toSeconds() / 3600.0;
            double y = l.getValor();
            sx += x; sy += y; sxx += x * x; sxy += x * y;
        }
        double den = n * sxx - sx * sx;
        if (den == 0) return null;
        double pendiente = (n * sxy - sx * sy) / den;
        return -pendiente >= TASA_SECADO_MINIMA ? -pendiente : null;
    }

    private static Double redondear(double v) {
        return Math.round(v * 100) / 100.0;
    }
}
