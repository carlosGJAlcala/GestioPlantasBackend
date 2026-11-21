package es.uah.huertojpa.persona.aplicacion;

import es.uah.huertojpa.persona.dominio.entidades.Persona;
import es.uah.huertojpa.persona.dominio.entidades.Usuario;
import es.uah.huertojpa.persona.infrastructura.database.IAdministradorDAO;
import es.uah.huertojpa.persona.infrastructura.database.IPersonaDAO;
import es.uah.huertojpa.persona.infrastructura.database.IUsuarioDAO;
import es.uah.huertojpa.seguridad.Contrasenas;
import es.uah.huertojpa.seguridad.LimiteIntentosLogin;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class PersonaServiceImpl implements IPersonaService{
    @Autowired
    IPersonaDAO personaDAO;
    @Autowired
    IUsuarioDAO usuarioDAO;
    @Autowired
    IAdministradorDAO administradorDAO;
    @Autowired
    Contrasenas contrasenas;
    @Autowired
    LimiteIntentosLogin limiteIntentos;

    @Override
    public List<Persona> buscarTodas() {
        return personaDAO.buscarTodas();
    }

    @Override
    public Persona buscarPorId(Integer id) {
        return personaDAO.buscarPorId(id);
    }

    @Override
    public Persona buscarPorUserName(String userName) {
        return personaDAO.buscarPorUserName(userName);
    }

    @Override
    public boolean guardarPersona(Persona persona) {
        // Alta sin id (los formularios ya no lo piden): lo genera la BD. Antes findById(null) daba un 500.
        if (persona.getId() == null || personaDAO.buscarPorId(persona.getId())==null) {
            String passSinHash =persona.getPasswordSHA256();
            // Sin usuario o contraseña no se puede entrar nunca (y antes daba un 500 al hacer el hash de null).
            if (vacio(persona.getUsuario()) || vacio(passSinHash) || personaDAO.buscarPorUserName(persona.getUsuario()) != null) {
                return false;
            }
            // El formulario de alta manda la contraseña tal cual: se hace el mismo SHA-256 que el front en el login
            // y se guarda con BCrypt (S2).
            persona.setPasswordSHA256(contrasenas.cifrar(Contrasenas.sha256(passSinHash)));
            if (persona.getAvisosCorreo() == null) persona.setAvisosCorreo(Boolean.TRUE);
            personaDAO.guardarPersona(persona);
            return true;
        }
        return false;
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    @Override
    @Transactional
    public boolean altaUsuario(Persona persona, Integer idAdministrador) {
        var admin = idAdministrador == null ? null : administradorDAO.buscarPorId(idAdministrador);
        if (admin == null || !guardarPersona(persona)) {
            return false;
        }
        // Sin fila en «usuario» la persona podía entrar pero no crear huertos (huerto_has_usuario apunta a usuario).
        Usuario usuario = new Usuario();
        usuario.setId(persona.getId());
        usuario.setAdministradorPersona(admin);
        usuario.setFechaDeAlta(Instant.now());
        usuarioDAO.guardar(usuario);
        return true;
    }

    @Override
    public boolean actualizarPersona(Persona persona) {
        Persona actual = persona.getId() == null ? null : personaDAO.buscarPorId(persona.getId());
        if (actual != null) {
            // Un formulario sin usuario no lo borra (antes dejaba la cuenta sin poder entrar).
            if (vacio(persona.getUsuario())) persona.setUsuario(actual.getUsuario());
            // S2: la contraseña NO se cambia desde aquí (antes se guardaba en claro lo que llegara y ya no se podía
            // entrar); hay que usar cambiarContrasena, que pide la actual.
            persona.setPasswordSHA256(actual.getPasswordSHA256());
            // A3: un formulario antiguo (sin la casilla) no desactiva los avisos.
            if (persona.getAvisosCorreo() == null) persona.setAvisosCorreo(actual.getAvisosCorreo());
            // Un formulario sin correo no lo borra (hasta hoy el correo no se actualizaba nunca por aquí).
            if (vacio(persona.getCorreoElectronico())) persona.setCorreoElectronico(actual.getCorreoElectronico());
            personaDAO.actualizarPersona(persona);
            return true;
        }
        return false;

    }

    @Override
    public boolean cambiarContrasena(Integer idPersona, String actual, String nueva) {
        Persona persona = idPersona == null ? null : personaDAO.buscarPorId(idPersona);
        if (persona == null || !Contrasenas.pareceSha256(nueva)
                || !contrasenas.coincide(actual, persona.getPasswordSHA256())) {
            return false;
        }
        // S8: también quita la marca de «cambiarla al entrar» y deja sin valor los JWT anteriores.
        personaDAO.ponerContrasena(idPersona, contrasenas.cifrar(nueva), false, LocalDateTime.now());
        return true;
    }

    @Override
    public boolean restablecerContrasena(Integer idPersona, String nueva) {
        Persona persona = idPersona == null ? null : personaDAO.buscarPorId(idPersona);
        if (persona == null || !Contrasenas.pareceSha256(nueva)) {
            return false;
        }
        personaDAO.ponerContrasena(idPersona, contrasenas.cifrar(nueva), true, LocalDateTime.now());
        limiteIntentos.desbloquearUsuario(persona.getUsuario());
        return true;
    }

    @Override
    public Persona cerrarSesiones(Integer idPersona) {
        Persona persona = idPersona == null ? null : personaDAO.buscarPorId(idPersona);
        if (persona == null) {
            return null;
        }
        personaDAO.cerrarSesiones(idPersona, LocalDateTime.now());
        return persona;
    }

    @Override
    public boolean eliminarPersona(Integer idPersona) {
        if(personaDAO.buscarPorId(idPersona)!=null){
            personaDAO.eliminarPersona(idPersona);
            return true;
        }
        return false;
    }

    @Override
    public boolean borrarPorUserName(String userName) {
        if(this.buscarPorUserName(userName)!=null){
            personaDAO.eliminarPersona(this.buscarPorUserName(userName).getId());
            return true;
        }
        return false;
    }
}
