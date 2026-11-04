package es.uah.huertojpa.riego.dominio;

/**
 * Resultado del ciclo de riego tal y como lo informa el ESP32.
 * <ul>
 *   <li>OK: se alcanzó la humedad mínima.</li>
 *   <li>LIMITE_PULSOS: se agotaron los pulsos sin alcanzar el objetivo (sensor desplazado, sustrato muy seco...).</li>
 *   <li>DEPOSITO_VACIO: no se riega porque el depósito está por debajo del mínimo.</li>
 *   <li>SENSOR_ERROR: lectura inválida del ultrasonidos o de humedad; no se riega por seguridad.</li>
 * </ul>
 */
public enum ResultadoRiego {
    OK,
    LIMITE_PULSOS,
    DEPOSITO_VACIO,
    SENSOR_ERROR
}
