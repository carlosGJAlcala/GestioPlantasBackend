package es.uah.huertojpa.mensajería;

public interface IMediador {
    void enviarMensaje(String correo,String asunto,String mensaje) throws Exception;

}
