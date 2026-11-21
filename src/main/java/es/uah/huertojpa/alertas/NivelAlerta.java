package es.uah.huertojpa.alertas;

/** Gravedad de una alerta o del estado global de una maceta (de menos a más grave). */
public enum NivelAlerta {
    /** Sin datos todavía: no se puede decir nada. */
    NEUTRO,
    OK,
    AVISO,
    CRITICO
}
