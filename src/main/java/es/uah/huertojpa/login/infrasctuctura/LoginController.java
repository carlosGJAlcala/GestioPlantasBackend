package es.uah.huertojpa.login.infrasctuctura;

import es.uah.huertojpa.persona.aplicacion.ILoginService;
import es.uah.huertojpa.persona.dominio.entidades.Administrador;
import es.uah.huertojpa.persona.dominio.entidades.Login;
import es.uah.huertojpa.persona.dominio.entidades.LoginResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @Autowired
    ILoginService service;
    @PostMapping(value="/login")
    public LoginResp login(@RequestBody Login login) {
        return service.auth(login);
    }
}
