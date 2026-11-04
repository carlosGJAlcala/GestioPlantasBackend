package es.uah.huertojpa.riego.aplicacion;

import es.uah.huertojpa.riego.dominio.MotivoRiego;
import es.uah.huertojpa.riego.dominio.ResultadoRiego;

/**
 * Cuerpo que envía el ESP32 en {@code POST /auth/riego} al terminar cada ciclo de riego.
 * Los niveles de depósito van en % (0-100), la humedad en % de suelo.
 */
public record RiegoRequest(
        Integer idMaceta,
        Integer duracionMs,
        Integer pulsos,
        Float humedadAntes,
        Float humedadDespues,
        Float nivelDepositoAntes,
        Float nivelDepositoDespues,
        MotivoRiego motivo,
        ResultadoRiego resultado) {
}
