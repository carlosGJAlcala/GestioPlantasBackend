package es.uah.huertojpa.login.aplicaccion;

import es.uah.huertojpa.jwt.JwtService;
import es.uah.huertojpa.seguridad.ContrasenaTemporal;
import es.uah.huertojpa.seguridad.Contrasenas;
import es.uah.huertojpa.seguridad.LimiteIntentosLogin;
import es.uah.huertojpa.login.dominio.LoginRequest;
import es.uah.huertojpa.login.dominio.LoginResponse;
import es.uah.huertojpa.persona.dominio.entidades.Persona;
import es.uah.huertojpa.persona.infrastructura.database.IAdministradorDAO;
import es.uah.huertojpa.persona.infrastructura.database.IPersonaDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
public class LoginServiceImpl implements ILoginService {
    @Autowired
    IPersonaDAO personaDAO;
    @Autowired
    IAdministradorDAO administradorDAO;
    @Autowired
    JwtService jwtService;
    @Autowired
    Contrasenas contrasenas;
    @Autowired
    LimiteIntentosLogin limite;
    @Autowired
    ContrasenaTemporal temporal;

    @Override
    public LoginResponse auth(LoginRequest login, String ip) {
        LoginResponse respuesta = new LoginResponse();
        String userName = login == null ? null : login.getUserName();
        Instant ahora = Instant.now();

        // S6: bloqueado por demasiados fallos ⇒ ni se mira la contraseña (no se puede seguir probando).
        Duration espera = limite.esperaRestante(userName, ip, ahora);
        if (!espera.isZero()) {
            incorrecta(respuesta);
            respuesta.setResultado("Demasiados intentos fallidos");
            // Redondeo hacia arriba: «0 s» haría que el front reintentara antes de tiempo.
            respuesta.setReintentarEnSegundos(Math.max(1, (espera.toMillis() + 999) / 1000));
            return respuesta;
        }

        // S2: se busca por usuario y se comprueba con BCrypt (antes: WHERE usuario = ? AND passwordsha256 = ?).
        Persona persona = userName == null || userName.isBlank() ? null : personaDAO.buscarPorUserName(userName);
        String guardado = persona == null ? null : persona.getPasswordSHA256();
        if (contrasenas.coincide(login == null ? null : login.getPassword(), guardado)
                && temporal.caducada(persona, LocalDateTime.now())) {
            // S8b: era la temporal del admin, pero ha pasado su plazo. No cuenta como fallo (la contraseña era la
            // buena) ni como acierto: no se entra y el front explica que hay que pedir otra.
            incorrecta(respuesta);
            respuesta.setResultado("Contraseña temporal caducada");
            respuesta.setTemporalCaducada(Boolean.TRUE);
        } else if (contrasenas.coincide(login == null ? null : login.getPassword(), guardado)) {
            limite.exito(userName, ip);
            if (!Contrasenas.esBcrypt(guardado)) {
                // Fila antigua (SHA-256 sin sal): se convierte ya, sin que el usuario note nada.
                personaDAO.actualizarContrasena(persona.getId(), contrasenas.cifrar(login.getPassword()));
            }
            personaDAO.registrarAcceso(persona.getId(), LocalDateTime.now());
            respuesta.setIdUser(persona.getId().toString());
            respuesta.setUserName(persona.getUsuario());
            respuesta.setResultado("Autentificiacion correcta");
            respuesta.setToken(jwtService.getToken(persona));
            respuesta.setPermisoAdmin(isAdmind(persona.getId()) ? "1" : "0");
            respuesta.setDebeCambiarContrasena(persona.tieneQueCambiarContrasena());
        } else {
            // Usuario inexistente o contraseña mala: se cuenta igual (si no, se podría saber qué usuarios existen).
            limite.fallo(userName, ip, ahora);
            incorrecta(respuesta);
        }
        return respuesta;
    }

    private static void incorrecta(LoginResponse respuesta) {
        respuesta.setIdUser("-1");
        respuesta.setPermisoAdmin("-1");
        respuesta.setResultado("Autentificiacion incorrecta");
    }

    @Override
    public boolean isAdmind(Integer id) {
        return administradorDAO.buscarPorId(id) != null;
    }
}
