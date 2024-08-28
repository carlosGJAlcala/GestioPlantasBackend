package es.uah.huertojpa.login.aplicaccion;

import es.uah.huertojpa.login.dominio.LoginRequest;
import es.uah.huertojpa.login.dominio.LoginResponse;

public interface ILoginService {
    LoginResponse auth(LoginRequest login);
    boolean isAdmind(Integer id);
}
