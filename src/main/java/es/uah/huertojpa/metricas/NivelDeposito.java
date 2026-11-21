package es.uah.huertojpa.metricas;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.uah.huertojpa.registro.dominio.entities.Lectura;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A2 – Nivel del depósito de agua del huerto de una maceta, calculado sobre las lecturas del ultrasonidos que ya
 * se guardan en {@code lectura} (sensor del depósito, {@code maceta_id} NULL). Cálculo puro, sin BD.
 *
 * <p>El ultrasonidos es ruidoso (±1 cm en un depósito de 17 cm son ±6 puntos), así que:</p>
 * <ul>
 *   <li>el nivel actual es la <b>mediana</b> de las últimas lecturas (hasta 5, en 15 min);</li>
 *   <li>el consumo se calcula con <b>medias por hora</b> desde el último rellenado (subida de ≥ 10 puntos entre
 *   dos horas seguidas), por mínimos cuadrados.</li>
 * </ul>
 *
 * @param nivel                % de agua (0–100) ahora; null si no hay lecturas válidas
 * @param ultimaLectura        última lectura válida del depósito
 * @param minutosDesdeLectura  antigüedad de esa lectura
 * @param consumoPorDia        puntos de % que baja el depósito al día desde el último rellenado (null si no se
 *                             puede estimar: pocas horas de datos o no baja)
 * @param diasHastaVacio       días hasta llegar al {@link #NIVEL_MINIMO_RIEGO} % al ritmo actual, que es donde la
 *                             placa deja de regar (0 si ya está por debajo; null sin consumo estimado)
 * @param capacidadLitros      A4: capacidad del depósito ({@code cantidadMaxima}, en litros); null si no está puesta
 * @param litros               A4: agua que queda (nivel × capacidad); null sin capacidad o sin nivel
 * @param consumoLitrosPorDia  A4: {@code consumoPorDia} en litros
 * @param perdidaSinRiego      A5: puntos de % que ha bajado el depósito en las últimas horas <b>sin ningún riego</b>
 *                             en el huerto (null si no hay datos suficientes; ver {@link #perdidaSinRiego})
 * @param horasSinRiego        A5: horas que cubre esa medida
 */
public record NivelDeposito(
        Float nivel,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime ultimaLectura,
        Long minutosDesdeLectura,
        Double consumoPorDia,
        Double diasHastaVacio,
        Float capacidadLitros,
        Double litros,
        Double consumoLitrosPorDia,
        Double perdidaSinRiego,
        Double horasSinRiego) {

    /** Igual que {@code NIVEL_MINIMO_DEPOSITO} del firmware: por debajo, el ESP32 no riega (DEPOSITO_VACIO). */
    public static final float NIVEL_MINIMO_RIEGO = 10f;
    /** Ventana de lecturas que se piden a la BD para estimar el consumo. */
    public static final Duration VENTANA = Duration.ofHours(72);
    /** Subida entre dos medias horarias seguidas que se interpreta como «lo han rellenado». */
    static final double SUBIDA_RELLENADO = 10;
    /** Por debajo de esto (puntos/día) se considera que no baja (ruido o placa sin regar). */
    static final double CONSUMO_MINIMO = 0.5;
    /** Horas mínimas de datos tras el rellenado para dar un consumo (los riegos son escalones, no una rampa). */
    static final double HORAS_MINIMAS = 6;
    static final int LECTURAS_MEDIANA = 5;
    static final Duration VENTANA_MEDIANA = Duration.ofMinutes(15);

    /** A5: ventana en la que se busca una bajada sin riegos (con más horas, una fuga lenta se confunde con la evaporación). */
    static final Duration VENTANA_FUGA = Duration.ofHours(6);
    /** A5: tras un riego se deja este margen antes de volver a vigilar (el agua del tubo vuelve, la superficie se calma). */
    static final Duration MARGEN_TRAS_RIEGO = Duration.ofMinutes(15);
    /**
     * A5: no se miran las lecturas más recientes que esto: el ESP32 registra el riego al <b>terminar</b> el ciclo
     * (como mucho 3 pulsos + absorción ≈ 2,5 min), así que un ciclo en curso todavía no está en la tabla.
     */
    static final Duration MARGEN_FINAL = Duration.ofMinutes(10);
    /** A5: horas mínimas sin riego para poder hablar de fuga. */
    static final double HORAS_MINIMAS_FUGA = 3;
    /**
     * A5: bajada (puntos de %) sin riegos a partir de la que se avisa. En un depósito de 20 cm son 1,6 cm: muy por
     * encima del ruido de las medias horarias del ultrasonidos y de la evaporación (décimas de punto al día).
     */
    public static final double PERDIDA_FUGA = 8;

    public static final NivelDeposito SIN_DATOS =
            new NivelDeposito(null, null, null, null, null, null, null, null, null, null);

    /** Sin litros ni fugas (tests y llamadas anteriores a A4/A5). */
    public NivelDeposito(Float nivel, LocalDateTime ultimaLectura, Long minutosDesdeLectura, Double consumoPorDia,
                         Double diasHastaVacio) {
        this(nivel, ultimaLectura, minutosDesdeLectura, consumoPorDia, diasHastaVacio, null, null, null, null, null);
    }

    /**
     * Distancia del ultrasonidos (cm, del sensor a la superficie del agua) → % de agua. Mismo criterio que
     * {@code nivelDeposito()} del firmware: lectura negativa (sin eco) o mayor que 1,2 veces la altura ⇒ inválida.
     */
    public static Float porcentaje(Float distanciaCm, Float alturaCm) {
        if (distanciaCm == null || alturaCm == null || alturaCm <= 0) return null;
        if (distanciaCm < 0 || distanciaCm > alturaCm * 1.2f) return null;
        float p = 100f * (1f - distanciaCm / alturaCm);
        return Math.max(0f, Math.min(100f, p));
    }

    /** @param niveles lecturas ya convertidas a % y ordenadas por fecha (las de las últimas {@link #VENTANA}) */
    public static NivelDeposito calcular(List<Lectura> niveles, LocalDateTime ahora) {
        return calcular(niveles, ahora, null, null);
    }

    /**
     * @param capacidadLitros capacidad del depósito (para dar litros); null si no se conoce
     * @param ultimoRiego     último riego con bomba de cualquier maceta del huerto (para A5); null si no hay
     */
    public static NivelDeposito calcular(List<Lectura> niveles, LocalDateTime ahora, Float capacidadLitros,
                                         LocalDateTime ultimoRiego) {
        if (niveles == null || niveles.isEmpty()) return SIN_DATOS;
        Lectura ultima = niveles.get(niveles.size() - 1);
        Float nivel = mediana(niveles);
        Long minutos = Duration.between(ultima.getTs(), ahora).toMinutes();

        Double consumo = consumoPorDia(niveles);
        Double dias = null;
        if (consumo != null) {
            dias = nivel <= NIVEL_MINIMO_RIEGO ? 0.0 : redondear((nivel - NIVEL_MINIMO_RIEGO) / consumo);
        }
        Float capacidad = capacidadLitros != null && capacidadLitros > 0 ? capacidadLitros : null;
        Double litros = capacidad == null ? null : redondear(nivel * capacidad / 100.0);
        Double litrosDia = capacidad == null || consumo == null ? null : redondear(consumo * capacidad / 100.0);
        double[] fuga = perdidaSinRiego(niveles, ultimoRiego, ahora);
        return new NivelDeposito(redondear1(nivel), ultima.getTs(), minutos, consumo, dias, capacidad, litros, litrosDia,
                fuga == null ? null : fuga[0], fuga == null ? null : fuga[1]);
    }

    /** A5: ¿la bajada sin riegos llega para sospechar una fuga? */
    public boolean posibleFuga() {
        return perdidaSinRiego != null && perdidaSinRiego >= PERDIDA_FUGA;
    }

    /**
     * A5 – Cuánto ha bajado el depósito sin que ninguna maceta del huerto haya regado: [puntos perdidos, horas].
     * Lo único que saca agua del depósito es la bomba, y cada ciclo queda registrado en {@code riego}; si el nivel
     * baja con la bomba parada es que se pierde agua (fuga, tubo suelto que sifona, alguien la ha cogido).
     *
     * <p>Se mira desde lo más tarde entre «hace {@link #VENTANA_FUGA}» y «último riego + {@link #MARGEN_TRAS_RIEGO}»
     * hasta «ahora − {@link #MARGEN_FINAL}», con medias de cada hora contada desde el inicio (el ultrasonidos da
     * ±1 cm por lectura), y a partir del último rellenado si lo hay. Null si cubre menos de
     * {@link #HORAS_MINIMAS_FUGA} horas entre la primera y la última media: tras cada riego hay que esperar
     * unas 4 h para volver a tener medida.</p>
     */
    static double[] perdidaSinRiego(List<Lectura> niveles, LocalDateTime ultimoRiego, LocalDateTime ahora) {
        LocalDateTime desde = ahora.minus(VENTANA_FUGA);
        if (ultimoRiego != null && ultimoRiego.plus(MARGEN_TRAS_RIEGO).isAfter(desde)) {
            desde = ultimoRiego.plus(MARGEN_TRAS_RIEGO);
        }
        LocalDateTime hasta = ahora.minus(MARGEN_FINAL);
        if (!desde.isBefore(hasta)) return null;

        java.util.TreeMap<Long, double[]> porHora = new java.util.TreeMap<>();
        for (Lectura l : niveles) {
            if (l.getTs().isBefore(desde) || l.getTs().isAfter(hasta)) continue;
            long minuto = Duration.between(desde, l.getTs()).toMinutes();
            double[] acc = porHora.computeIfAbsent(minuto / 60, k -> new double[3]);
            acc[0] += l.getValor();
            acc[1]++;
            acc[2] += minuto;
        }
        // Valor medio y momento medio (en horas desde «desde») de cada hora con datos.
        List<double[]> grupos = new ArrayList<>(porHora.values());
        List<Double> medias = grupos.stream().map(g -> g[0] / g[1]).toList();
        List<Double> horas = grupos.stream().map(g -> g[2] / g[1] / 60.0).toList();
        int inicio = 0;
        for (int i = 1; i < medias.size(); i++) {
            if (medias.get(i) - medias.get(i - 1) >= SUBIDA_RELLENADO) inicio = i;
        }
        if (medias.size() - inicio < 2) return null;
        double span = horas.get(horas.size() - 1) - horas.get(inicio);
        if (span < HORAS_MINIMAS_FUGA) return null;
        double perdida = medias.get(inicio) - medias.get(medias.size() - 1);
        return new double[]{redondear(Math.max(0, perdida)), redondear(span)};
    }

    /** Mediana de las últimas lecturas (como mucho {@link #LECTURAS_MEDIANA}, de los últimos 15 min). */
    static Float mediana(List<Lectura> niveles) {
        LocalDateTime limite = niveles.get(niveles.size() - 1).getTs().minus(VENTANA_MEDIANA);
        List<Float> ultimas = new ArrayList<>();
        for (int i = niveles.size() - 1; i >= 0 && ultimas.size() < LECTURAS_MEDIANA; i--) {
            if (niveles.get(i).getTs().isBefore(limite)) break;
            ultimas.add(niveles.get(i).getValor());
        }
        ultimas.sort(null);
        int n = ultimas.size();
        return n % 2 == 1 ? ultimas.get(n / 2) : (ultimas.get(n / 2 - 1) + ultimas.get(n / 2)) / 2f;
    }

    /** Pendiente (puntos/día, positiva si baja) de las medias horarias desde el último rellenado. */
    static Double consumoPorDia(List<Lectura> niveles) {
        Map<LocalDateTime, double[]> porHora = new LinkedHashMap<>();
        for (Lectura l : niveles) {
            double[] acc = porHora.computeIfAbsent(l.getTs().truncatedTo(ChronoUnit.HOURS), k -> new double[2]);
            acc[0] += l.getValor();
            acc[1]++;
        }
        List<LocalDateTime> horas = new ArrayList<>(porHora.keySet());
        List<Double> medias = horas.stream().map(h -> porHora.get(h)[0] / porHora.get(h)[1]).toList();

        int desde = 0;
        for (int i = 1; i < medias.size(); i++) {
            if (medias.get(i) - medias.get(i - 1) >= SUBIDA_RELLENADO) desde = i;
        }
        if (medias.size() - desde < 3) return null;
        LocalDateTime t0 = horas.get(desde);
        double span = Duration.between(t0, horas.get(horas.size() - 1)).toMinutes() / 60.0;
        if (span < HORAS_MINIMAS) return null;

        double n = 0, sx = 0, sy = 0, sxx = 0, sxy = 0;
        for (int i = desde; i < medias.size(); i++) {
            double x = Duration.between(t0, horas.get(i)).toMinutes() / 60.0;
            double y = medias.get(i);
            n++; sx += x; sy += y; sxx += x * x; sxy += x * y;
        }
        double den = n * sxx - sx * sx;
        if (den == 0) return null;
        double consumo = -24.0 * (n * sxy - sx * sy) / den;
        return consumo >= CONSUMO_MINIMO ? redondear(consumo) : null;
    }

    private static Double redondear(double v) {
        return Math.round(v * 100) / 100.0;
    }

    private static Float redondear1(Float v) {
        return v == null ? null : Math.round(v * 10) / 10f;
    }
}
