package es.uah.huertojpa.riego.dominio;

/** Ciclo de vida de una orden «Regar ahora». */
public enum EstadoOrden {
    /** Pedida desde la web; el ESP32 aún no la ha recogido. */
    PENDIENTE,
    /** El ESP32 la ha recogido; el resultado llegará como un {@link Riego} con motivo MANUAL. */
    ENTREGADA,
    /** El usuario la anuló antes de que el ESP32 la recogiera. */
    CANCELADA,
    /** Nadie la recogió a tiempo (ESP32 sin red): no se riega horas más tarde por sorpresa. */
    CADUCADA
}
