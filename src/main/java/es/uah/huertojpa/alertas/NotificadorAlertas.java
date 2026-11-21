package es.uah.huertojpa.alertas;

import java.util.List;

/** Canal por el que se avisa de las alertas nuevas (hoy, correo; mañana podría ser Telegram o push). */
public interface NotificadorAlertas {

    /**
     * Avisa a los dueños de una maceta de sus alertas nuevas (un único mensaje con todas).
     *
     * @return {@code true} si el aviso salió al menos hacia un destinatario
     */
    boolean notificar(String maceta, List<String> correos, List<Alerta> alertas);
}
