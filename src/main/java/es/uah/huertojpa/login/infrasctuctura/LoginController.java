package es.uah.huertojpa.login.infrasctuctura;

import es.uah.huertojpa.login.aplicaccion.ILoginService;
import es.uah.huertojpa.login.dominio.LoginRequest;
import es.uah.huertojpa.login.dominio.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class LoginController {

    @Autowired
    ILoginService service;
    @PostMapping(value="login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest login) {
        return ResponseEntity.ok( service.auth(login));
    }
    @PostMapping(value="register")
    public String register(){
        return "Register fom public endpoint";
    }
}
