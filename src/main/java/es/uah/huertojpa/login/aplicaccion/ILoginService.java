package es.uah.huertojpa.login.aplicaccion;

import es.uah.huertojpa.login.dominio.LoginRequest;
import es.uah.huertojpa.login.dominio.LoginResponse;

public interface ILoginService {
    /** Sin IP (llamadas internas o antiguas): cuenta los fallos como si vinieran de una IP desconocida. */
    default LoginResponse auth(LoginRequest login) {
        return auth(login, null);
    }

    /**
     * S6: {@code ip} es la del cliente, para el límite de intentos. Si está bloqueado, la respuesta trae
     * {@code reintentarEnSegundos} (el controlador responde 429) y no se comprueba la contraseña.
     */
    LoginResponse auth(LoginRequest login, String ip);
    boolean isAdmind(Integer id);
}
