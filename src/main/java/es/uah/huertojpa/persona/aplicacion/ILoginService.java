package es.uah.huertojpa.persona.aplicacion;

import es.uah.huertojpa.persona.dominio.entidades.Login;
import es.uah.huertojpa.persona.dominio.entidades.LoginResp;

public interface ILoginService {
    LoginResp auth(Login login);
    boolean isAdmind(Integer id);
}
