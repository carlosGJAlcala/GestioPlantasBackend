package es.uah.huertojpa.riego.aplicacion;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.uah.huertojpa.riego.dominio.MotivoRiego;
import es.uah.huertojpa.riego.dominio.ResultadoRiego;
import es.uah.huertojpa.riego.dominio.Riego;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Métricas de riego de una maceta en una ventana de {@code dias} días (backlog §3.2).
 * Cálculo puro (sin BD) para poder probarlo con tests unitarios.
 *
 * @param riegos                   ciclos con la bomba activada
 * @param riegosPorDia             riegos / días de la ventana
 * @param pulsosTotales            pulsos de bomba
 * @param segundosBomba            tiempo total de bomba encendida
 * @param incrementoHumedadMedio   media de (humedadDespues − humedadAntes) en riegos con bomba: "eficacia"
 * @param riegosFallidos           bomba activada y la humedad subió &lt; 2 puntos
 * @param riegosLimitePulsos       se agotaron los pulsos sin alcanzar la humedad mínima
 * @param bloqueadosDepositoVacio  ciclos en los que no se regó por falta de agua
 * @param erroresSensor            ciclos abortados por lectura inválida
 * @param consumoDepositoPuntos    suma de bajadas de nivel del depósito (puntos de %) durante los riegos
 * @param litrosEstimados          consumoDepositoPuntos × capacidad / 100 (null si no se conoce la capacidad)
 * @param ultimoRiego              inicio del último riego con bomba (fuera o dentro de la ventana)
 * @param horasDesdeUltimoRiego    horas desde ese riego (null si nunca se ha regado)
 * @param riegosManuales           riegos con bomba pedidos desde la web («Regar ahora»)
 * @param manualesDescartados      órdenes «Regar ahora» que el ESP32 no ejecutó porque la tierra ya estaba en la
 *                                 humedad máxima del tipo de planta (indicador de que se riega «por si acaso»)
 */
public record ResumenRiego(
        int dias,
        int riegos,
        double riegosPorDia,
        int pulsosTotales,
        double segundosBomba,
        Double incrementoHumedadMedio,
        int riegosFallidos,
        int riegosLimitePulsos,
        int bloqueadosDepositoVacio,
        int erroresSensor,
        double consumoDepositoPuntos,
        Double litrosEstimados,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime ultimoRiego,
        Double horasDesdeUltimoRiego,
        int riegosManuales,
        int manualesDescartados) {

    public static ResumenRiego calcular(List<Riego> ventana, Riego ultimo, LocalDateTime ahora, int dias,
                                        Float capacidadDepositoLitros) {
        int riegos = 0, pulsos = 0, fallidos = 0, limite = 0, vacio = 0, errores = 0, conDelta = 0;
        int manuales = 0, descartados = 0;
        long msBomba = 0;
        double sumaDelta = 0, consumo = 0;

        for (Riego r : ventana) {
            if (r.getResultado() == ResultadoRiego.DEPOSITO_VACIO) vacio++;
            if (r.getResultado() == ResultadoRiego.SENSOR_ERROR) errores++;
            if (r.getResultado() == ResultadoRiego.LIMITE_PULSOS) limite++;
            if (r.getResultado() == ResultadoRiego.HUMEDAD_MAXIMA) descartados++;
            if (!r.bombaActivada()) continue;

            riegos++;
            if (r.getMotivo() == MotivoRiego.MANUAL) manuales++;
            pulsos += r.getPulsos();
            msBomba += r.getDuracionMs() == null ? 0 : r.getDuracionMs();
            if (r.esFallido()) fallidos++;
            Float delta = r.deltaHumedad();
            if (delta != null) {
                sumaDelta += delta;
                conDelta++;
            }
            if (r.getNivelDepositoAntes() != null && r.getNivelDepositoDespues() != null) {
                // Solo bajadas: una subida durante el riego es ruido del ultrasonidos o un rellenado.
                consumo += Math.max(0, r.getNivelDepositoAntes() - r.getNivelDepositoDespues());
            }
        }

        Double litros = capacidadDepositoLitros == null ? null : redondear(consumo * capacidadDepositoLitros / 100.0);
        LocalDateTime tsUltimo = ultimo == null ? null : ultimo.getTs();
        Double horas = tsUltimo == null ? null : redondear(Duration.between(tsUltimo, ahora).toMinutes() / 60.0);

        return new ResumenRiego(dias, riegos, redondear((double) riegos / Math.max(1, dias)), pulsos,
                msBomba / 1000.0, conDelta == 0 ? null : redondear(sumaDelta / conDelta), fallidos, limite, vacio,
                errores, redondear(consumo), litros, tsUltimo, horas, manuales, descartados);
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
