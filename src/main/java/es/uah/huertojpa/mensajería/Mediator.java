package es.uah.huertojpa.mensajería;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class Mediator implements IMediador {
    @Autowired
    private JavaMailSender mailSender;
    @Override
    public void enviarMensaje(String correo, String asunto, String mensaje) throws Exception {
        SimpleMailMessage mailmensaje = new SimpleMailMessage();
        mailmensaje.setTo(correo);
        mailmensaje.setSubject(asunto);
        mailmensaje.setText(mensaje);
        mailmensaje.setFrom("huertouah@gmail.com");

        mailSender.send(mailmensaje);

    }
}
