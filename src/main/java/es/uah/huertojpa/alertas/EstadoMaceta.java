package es.uah.huertojpa.alertas;

import es.uah.huertojpa.metricas.MetricasMaceta;
import es.uah.huertojpa.metricas.NivelDeposito;
import es.uah.huertojpa.riego.dominio.ResultadoRiego;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Estado global de una maceta a partir de sus métricas: el nivel y el texto del problema más grave, y la lista de
 * todos los problemas activos. Lo usan el panel (dentro de {@code GET /metricas/maceta/{id}}) y el servicio de
 * alertas, así que la web y los correos aplican <b>los mismos criterios</b>. Cálculo puro, sin BD.
 *
 * <p>Criterios:</p>
 * <ul>
 *   <li>{@code PLACA_DESCONECTADA} (crítico): hay lecturas antiguas pero ninguna en los últimos 30 min. Mientras
 *   tanto no se evalúan humedad ni temperatura: serían datos viejos.</li>
 *   <li>{@code RIEGO_FALLIDO}: el último ciclo de riego acabó en {@code DEPOSITO_VACIO} (crítico: la planta no se
 *   regará hasta rellenarlo), {@code LIMITE_PULSOS} o {@code SENSOR_ERROR}, o la bomba funcionó y la humedad
 *   apenas subió. Se resuelve con el siguiente riego correcto.</li>
 *   <li>{@code DEPOSITO_BAJO} (A2): el depósito del huerto está por debajo del {@value #NIVEL_DEPOSITO_AVISO} %
 *   (aviso) o ya en el mínimo con el que la placa deja de regar (crítico). Solo con una lectura del depósito de
 *   los últimos 30 min. Sustituye al antiguo correo «El deposito de agua esta vacio».</li>
 *   <li>{@code FUGA_DEPOSITO} (A5, aviso): el depósito ha bajado al menos {@code NivelDeposito.PERDIDA_FUGA}
 *   puntos en 3–6 h sin ningún riego en el huerto. Solo la bomba saca agua: si baja con ella parada, se pierde.</li>
 *   <li>{@code TIERRA_SECA} / {@code EXCESO_AGUA}: última humedad fuera del rango del tipo de planta.</li>
 *   <li>{@code TEMPERATURA_FUERA_RANGO}: última temperatura fuera del rango del tipo de planta.</li>
 * </ul>
 */
public record EstadoMaceta(NivelAlerta nivel, String texto, List<Condicion> condiciones) {
    /** % de agua por debajo del cual se avisa de rellenar el depósito (el correo antiguo usaba 25). */
    public static final float NIVEL_DEPOSITO_AVISO = 20f;
    /** Una lectura del depósito más antigua que esto no sirve para avisar (placa apagada o ultrasonidos roto). */
    static final long MINUTOS_DEPOSITO_VALIDO = 30;

    public static EstadoMaceta de(MetricasMaceta m) {
        if (m == null || m.ultimaLectura() == null) {
            return new EstadoMaceta(NivelAlerta.NEUTRO, "Sin lecturas todavía", List.of());
        }
        List<Condicion> c = condiciones(m);
        if (c.isEmpty()) {
            return new EstadoMaceta(NivelAlerta.OK, "Todo bien", c);
        }
        Condicion peor = c.stream()
                .max(Comparator.comparing(Condicion::nivel)
                        .thenComparing(Condicion::tipo, Comparator.reverseOrder()))
                .orElseThrow();
        return new EstadoMaceta(peor.nivel(), peor.titulo(), c);
    }

    static List<Condicion> condiciones(MetricasMaceta m) {
        List<Condicion> c = new ArrayList<>();
        if (!m.placaConectada()) {
            c.add(new Condicion(TipoAlerta.PLACA_DESCONECTADA, NivelAlerta.CRITICO,
                    "La placa no manda lecturas desde hace " + duracion(m.minutosDesdeUltimaLectura())
                            + ". Revisa la alimentación del ESP32 y la WiFi."));
        }

        Condicion riego = riegoFallido(m);
        if (riego != null) c.add(riego);

        Condicion deposito = depositoBajo(m.deposito());
        if (deposito != null) c.add(deposito);

        Condicion fuga = fugaDeposito(m.deposito());
        if (fuga != null) c.add(fuga);

        if (m.placaConectada()) {
            Float h = m.humedadActual();
            if (h != null && m.humedadMinima() != null && h < m.humedadMinima()) {
                c.add(new Condicion(TipoAlerta.TIERRA_SECA, NivelAlerta.AVISO,
                        "Humedad " + num(h, 0) + " %, por debajo del mínimo de la planta (" + num(m.humedadMinima(), 0) + " %)."));
            } else if (h != null && m.humedadMaxima() != null && h > m.humedadMaxima()) {
                c.add(new Condicion(TipoAlerta.EXCESO_AGUA, NivelAlerta.AVISO,
                        "Humedad " + num(h, 0) + " %, por encima del máximo de la planta (" + num(m.humedadMaxima(), 0)
                                + " %). Riesgo de pudrición de raíz: revisa el drenaje."));
            }
            Float t = m.temperaturaActual();
            boolean fria = t != null && m.temperaturaMinima() != null && t < m.temperaturaMinima();
            boolean caliente = t != null && m.temperaturaMaxima() != null && t > m.temperaturaMaxima();
            if (fria || caliente) {
                c.add(new Condicion(TipoAlerta.TEMPERATURA_FUERA_RANGO, NivelAlerta.AVISO,
                        "Temperatura " + num(t, 1) + " °C, fuera del rango de la planta ("
                                + num(m.temperaturaMinima(), 0) + "–" + num(m.temperaturaMaxima(), 0) + " °C)."));
            }
        }
        return c;
    }

    private static Condicion riegoFallido(MetricasMaceta m) {
        ResultadoRiego r = m.ultimoRiegoResultado();
        if (r == null) return null;
        return switch (r) {
            case DEPOSITO_VACIO -> new Condicion(TipoAlerta.RIEGO_FALLIDO, NivelAlerta.CRITICO,
                    "No se ha podido regar: el depósito de agua está vacío. Rellénalo.");
            case LIMITE_PULSOS -> new Condicion(TipoAlerta.RIEGO_FALLIDO, NivelAlerta.AVISO,
                    "El último riego agotó los pulsos sin llegar a la humedad mínima (¿sensor fuera de la tierra o tubo obstruido?).");
            case SENSOR_ERROR -> new Condicion(TipoAlerta.RIEGO_FALLIDO, NivelAlerta.AVISO,
                    "La placa no regó porque un sensor dio una lectura inválida.");
            default -> m.ultimoRiegoFallido()
                    ? new Condicion(TipoAlerta.RIEGO_FALLIDO, NivelAlerta.AVISO,
                    "La bomba funcionó pero la humedad apenas subió (¿depósito vacío, tubo suelto o bomba averiada?).")
                    : null;
        };
    }

    static Condicion depositoBajo(NivelDeposito d) {
        if (d == null || d.nivel() == null || d.minutosDesdeLectura() == null
                || d.minutosDesdeLectura() > MINUTOS_DEPOSITO_VALIDO) {
            return null;
        }
        if (d.nivel() <= NivelDeposito.NIVEL_MINIMO_RIEGO) {
            return new Condicion(TipoAlerta.DEPOSITO_BAJO, NivelAlerta.CRITICO,
                    "El depósito está al " + num(d.nivel(), 0) + " %: por debajo del "
                            + num(NivelDeposito.NIVEL_MINIMO_RIEGO, 0) + " % la placa ya no riega. Rellénalo.");
        }
        if (d.nivel() < NIVEL_DEPOSITO_AVISO) {
            String quedan = d.diasHastaVacio() == null ? ""
                    : d.diasHastaVacio() < 1 ? " Al ritmo de consumo actual se acabará hoy."
                    : " Al ritmo de consumo actual quedan unos " + Math.round(d.diasHastaVacio()) + " días de riego.";
            return new Condicion(TipoAlerta.DEPOSITO_BAJO, NivelAlerta.AVISO,
                    "El depósito está al " + num(d.nivel(), 0) + " %: rellénalo pronto." + quedan);
        }
        return null;
    }

    /** A5 – El depósito baja sin que la bomba haya funcionado. */
    static Condicion fugaDeposito(NivelDeposito d) {
        if (d == null || !d.posibleFuga() || d.minutosDesdeLectura() == null
                || d.minutosDesdeLectura() > MINUTOS_DEPOSITO_VALIDO) {
            return null;
        }
        String litros = d.capacidadLitros() == null ? ""
                : " (unos " + String.format(Locale.forLanguageTag("es"), "%.1f", d.perdidaSinRiego() * d.capacidadLitros() / 100.0)
                + " L)"; // coma decimal: el correo y el panel están en español
        return new Condicion(TipoAlerta.FUGA_DEPOSITO, NivelAlerta.AVISO,
                "El depósito ha bajado " + num(d.perdidaSinRiego().floatValue(), 0) + " puntos" + litros
                        + " en " + Math.round(d.horasSinRiego()) + " h sin ningún riego. Revisa si hay una fuga o un"
                        + " tubo suelto (o si alguien ha cogido agua).");
    }

    static String duracion(Long minutos) {
        if (minutos == null) return "mucho tiempo";
        if (minutos < 120) return minutos + " min";
        if (minutos < 48 * 60) return (minutos / 60) + " h";
        return (minutos / 1440) + " días";
    }

    private static String num(Float v, int decimales) {
        return String.format(Locale.ROOT, "%." + decimales + "f", v);
    }
}
