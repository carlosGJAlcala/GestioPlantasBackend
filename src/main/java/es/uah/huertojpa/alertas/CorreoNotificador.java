package es.uah.huertojpa.alertas;

import es.uah.huertojpa.mensajería.IMediador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Envía las alertas por correo con {@code spring-mail} (también la del depósito, A2). Si no hay contraseña
 * de correo configurada ({@code MAIL_PASSWORD} vacía) no intenta enviar: las alertas se ven igual en el panel.
 */
@Component
public class CorreoNotificador implements NotificadorAlertas {
    private static final Logger log = LoggerFactory.getLogger(CorreoNotificador.class);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final IMediador mediador;
    private final boolean configurado;

    public CorreoNotificador(IMediador mediador, @Value("${spring.mail.password:}") String clave) {
        this.mediador = mediador;
        this.configurado = clave != null && !clave.isBlank();
        if (!configurado) {
            log.warn("MAIL_PASSWORD vacía: las alertas solo se verán en el panel, no se enviarán por correo");
        }
    }

    @Override
    public boolean notificar(String maceta, List<String> correos, List<Alerta> alertas) {
        if (!configurado || correos.isEmpty() || alertas.isEmpty()) {
            log.info("Alertas de {} sin enviar por correo ({}): {}", maceta,
                    configurado ? "sin destinatarios con correo" : "correo no configurado",
                    alertas.stream().map(Alerta::getTitulo).toList());
            return false;
        }
        String asunto = "Huerto · " + maceta + ": " + alertas.get(0).getTitulo()
                + (alertas.size() > 1 ? " y " + (alertas.size() - 1) + " más" : "");
        String cuerpo = cuerpo(maceta, alertas);
        boolean alguno = false;
        for (String correo : correos) {
            try {
                mediador.enviarMensaje(correo, asunto, cuerpo);
                alguno = true;
            } catch (Exception e) {
                log.error("No se pudo enviar la alerta de {} a {}: {}", maceta, correo, e.getMessage());
            }
        }
        return alguno;
    }

    static String cuerpo(String maceta, List<Alerta> alertas) {
        StringBuilder sb = new StringBuilder("Hay novedades en ").append(maceta).append(":\n\n");
        for (Alerta a : alertas) {
            sb.append(a.getNivel() == NivelAlerta.CRITICO ? "[URGENTE] " : "[AVISO] ")
                    .append(a.getTitulo()).append(" (desde ").append(HORA.format(a.getInicio())).append(")\n  ")
                    .append(a.getMensaje()).append("\n\n");
        }
        sb.append("Puedes ver la maceta y regarla desde el panel del huerto.\n")
                .append("No se repetirá este aviso mientras el problema siga abierto.");
        return sb.toString();
    }
}
