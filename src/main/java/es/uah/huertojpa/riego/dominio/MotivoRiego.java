package es.uah.huertojpa.riego.dominio;

/** Origen del riego: decidido por el ESP32 (AUTO) o pedido por un usuario (MANUAL, ver backlog 3.3). */
public enum MotivoRiego {
    AUTO,
    MANUAL
}
