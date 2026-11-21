package es.uah.huertojpa.login.infrasctuctura;

import es.uah.huertojpa.login.aplicaccion.ILoginService;
import es.uah.huertojpa.login.dominio.LoginRequest;
import es.uah.huertojpa.login.dominio.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

    /**
     * 200 con {@code idUser = "-1"} si el usuario o la contraseña no valen (como siempre). S6: 429 + {@code Retry-After}
     * si hay demasiados fallos seguidos desde esa IP. La IP es la del navegador también detrás de nginx gracias a
     * {@code server.forward-headers-strategy=native}.
     */
    @PostMapping(value="login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest login, HttpServletRequest request) {
        LoginResponse respuesta = service.auth(login, request.getRemoteAddr());
        if (respuesta.bloqueado()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(HttpHeaders.RETRY_AFTER, String.valueOf(respuesta.getReintentarEnSegundos()))
                    .body(respuesta);
        }
        return ResponseEntity.ok(respuesta);
    }
    @PostMapping(value="register")
    public String register(){
        return "Register fom public endpoint";
    }
}
