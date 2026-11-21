package es.uah.huertojpa.persona.infrastructura.restcontrolador;

import es.uah.huertojpa.jwt.JwtService;
import es.uah.huertojpa.persona.aplicacion.IPersonaService;
import es.uah.huertojpa.persona.aplicacion.IUsuarioService;
import es.uah.huertojpa.persona.dominio.entidades.Persona;
import es.uah.huertojpa.persona.dominio.entidades.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import es.uah.huertojpa.seguridad.AccesoService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
public class PersonaController {

    @Autowired
    IPersonaService personaService;
    @Autowired
    AccesoService acceso;
    @Autowired
    JwtService jwtService;
    @GetMapping("/persona")
    public List<Persona> buscarTodos() {
        return personaService.buscarTodas();
    }
    @GetMapping("/persona/{id}")
    public Persona buscarPorId(@PathVariable("id") Integer id) {
        return personaService.buscarPorId(id);
    }
    @GetMapping("/persona/userName/{userName}")
    public Persona buscarPorUserName(@PathVariable("userName") String userName) {
        return personaService.buscarPorUserName(userName);
    }


    @PostMapping(value="/persona")
    public String guardarPersona(@RequestBody Persona persona, Authentication auth) {
        // Solo admin (SecurityConfig). Crea la persona y su fila de usuario, con este admin como responsable.
        var admin = AccesoService.persona(auth);
        return String.valueOf(personaService.altaUsuario(persona, admin == null ? null : admin.getId()));
    }
    @PutMapping(value="/persona")
    public String actualizarTipoPersona(@RequestBody Persona persona, Authentication auth) {
        // S4b: cada uno solo modifica su propia ficha; el admin, cualquiera. La contraseña ya no (S2): /persona/contrasena.
        AccesoService.exigir(acceso.puedeAccederPersona(auth, persona.getId()));
        return String.valueOf( personaService.actualizarPersona(persona));
    }

    /** S2: cuerpo de {@code PUT /persona/contrasena}; las dos como SHA-256 en hex, igual que en el login. */
    public record CambioContrasena(String actual, String nueva) {}

    /** S8: respuesta de un cambio correcto. El token anterior deja de valer (es de antes del cambio): va uno nuevo. */
    public record ContrasenaCambiada(boolean cambiada, String token) {}

    /**
     * S2: el usuario del token cambia SU contraseña (pide la actual). 200 {@code {"cambiada":true,"token":…}} si se
     * cambia; 400 «false» si la actual no es correcta o la nueva no viene como SHA-256. S8: desde este cambio los JWT
     * anteriores ya no valen (sesiones abiertas en otros equipos), así que se devuelve uno nuevo para seguir en esta.
     */
    @PutMapping(value = "/persona/contrasena")
    public ResponseEntity<?> cambiarContrasena(@RequestBody CambioContrasena cambio, Authentication auth) {
        var yo = AccesoService.persona(auth);
        boolean ok = yo != null && cambio != null
                && personaService.cambiarContrasena(yo.getId(), cambio.actual(), cambio.nueva());
        if (!ok) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body("false");
        }
        return ResponseEntity.ok(new ContrasenaCambiada(true, jwtService.getToken(yo)));
    }

    /** S8: cuerpo de {@code PUT /persona/{id}/contrasena}: la contraseña temporal como SHA-256 en hex. */
    public record RestablecerContrasena(String nueva) {}

    /**
     * S8: el administrador (regla en SecurityConfig) restablece la contraseña de otra persona, p. ej. si la ha
     * olvidado o se ha quedado bloqueada (S6). 200 «true»; 400 «false» si no existe o la nueva no es un SHA-256.
     */
    @PutMapping(value = "/persona/{id}/contrasena", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> restablecerContrasena(@PathVariable("id") Integer id,
                                                        @RequestBody RestablecerContrasena cuerpo) {
        boolean ok = cuerpo != null && personaService.restablecerContrasena(id, cuerpo.nueva());
        return ok ? ResponseEntity.ok("true") : ResponseEntity.badRequest().body("false");
    }

    /** S9: respuesta de {@code POST /persona/sesiones/cerrar}: token nuevo para seguir en este equipo. */
    public record SesionesCerradas(boolean cerradas, String token) {}

    /**
     * S9: «Cerrar sesión en todos los equipos». Los JWT de la persona del token emitidos hasta ahora dejan de valer
     * (otros navegadores, un móvil perdido…). Se devuelve uno nuevo para no echar también a quien lo pide.
     */
    @PostMapping(value = "/persona/sesiones/cerrar")
    public ResponseEntity<?> cerrarMisSesiones(Authentication auth) {
        var yo = AccesoService.persona(auth);
        Persona persona = yo == null ? null : personaService.cerrarSesiones(yo.getId());
        if (persona == null) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body("false");
        }
        return ResponseEntity.ok(new SesionesCerradas(true, jwtService.getToken(persona)));
    }

    /** S9: el admin (regla en SecurityConfig) cierra todas las sesiones de otra persona. 200 «true»; 400 si no existe. */
    @PostMapping(value = "/persona/{id}/sesiones/cerrar", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> cerrarSesionesDe(@PathVariable("id") Integer id) {
        return personaService.cerrarSesiones(id) != null ? ResponseEntity.ok("true") : ResponseEntity.badRequest().body("false");
    }

    @DeleteMapping(value = "/persona/{id}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String eliminarTipoPersona(@PathVariable("id") Integer id) {
        return String.valueOf(personaService.eliminarPersona(id));
    }
    @DeleteMapping(value = "/persona/userName/{userName}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String borrarPorUserName(@PathVariable("userName") String userName) {
        return String.valueOf(personaService.borrarPorUserName(userName));
    }
}
