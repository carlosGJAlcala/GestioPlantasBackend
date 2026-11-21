package es.uah.huertojpa.alertas;

/**
 * Problemas que se vigilan en cada maceta. El orden es la prioridad con la que se resume el estado global (si hay
 * varios del mismo nivel, gana el primero).
 */
public enum TipoAlerta {
    PLACA_DESCONECTADA("Placa sin conexión"),
    RIEGO_FALLIDO("Riego fallido"),
    DEPOSITO_BAJO("Depósito bajo"),
    FUGA_DEPOSITO("Posible fuga en el depósito"),
    TIERRA_SECA("Tierra seca"),
    EXCESO_AGUA("Exceso de agua"),
    TEMPERATURA_FUERA_RANGO("Temperatura fuera de rango");

    private final String titulo;

    TipoAlerta(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Problemas del depósito, que es del huerto y no de la maceta: todas las macetas del huerto los ven, pero se
     * avisa una sola vez por correo y el panel los agrupa.
     */
    public boolean esDelHuerto() {
        return this == DEPOSITO_BAJO || this == FUGA_DEPOSITO;
    }

    /** Texto corto para la tarjeta del panel y el asunto del correo. */
    public String getTitulo() {
        return titulo;
    }
}
