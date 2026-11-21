package es.uah.huertojpa.metricas;

import java.time.LocalDateTime;
import java.util.List;

/**
 * W7 – Serie temporal de una magnitud de una maceta, agregada en intervalos de {@code pasoMinutos} para pintar la
 * gráfica del panel sin mandar al navegador miles de lecturas (el ESP32 manda una por minuto: 10 080 por semana).
 *
 * @param rangoMinimo/rangoMaximo rango ideal de la planta para esa magnitud (banda sombreada en la gráfica); null
 *                                si la maceta no tiene planta con tipo
 */
public record SerieLecturas(Integer macetaId, String magnitud, LocalDateTime desde, LocalDateTime hasta,
                            int pasoMinutos, Float rangoMinimo, Float rangoMaximo, List<Punto> puntos) {

    /** Un intervalo con datos (los intervalos sin lecturas no aparecen: la gráfica corta la línea). */
    public record Punto(LocalDateTime ts, double media, double minimo, double maximo, long lecturas) {
    }
}
