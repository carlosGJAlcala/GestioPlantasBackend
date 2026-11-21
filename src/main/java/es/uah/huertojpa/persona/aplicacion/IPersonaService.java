package es.uah.huertojpa.persona.aplicacion;

import es.uah.huertojpa.persona.dominio.entidades.Persona;

import java.util.List;


public interface IPersonaService   {
    List<Persona> buscarTodas();
    Persona buscarPorId(Integer id);
    Persona buscarPorUserName(String userName);

    boolean guardarPersona(Persona persona);

    /** Alta de un usuario desde la web por un administrador: persona + fila en {@code usuario}. */
    boolean altaUsuario(Persona persona, Integer idAdministrador);
    boolean actualizarPersona(Persona persona);
    /**
     * S2: cambia la contraseña si {@code actual} es la buena. Las dos llegan como SHA-256 en hex (lo que hace el
     * front antes de mandarlas); {@code nueva} con otro formato se rechaza.
     */
    boolean cambiarContrasena(Integer idPersona, String actual, String nueva);
    /**
     * S8: el administrador pone una contraseña temporal ({@code nueva}, SHA-256 en hex) sin conocer la actual. La
     * persona tendrá que cambiarla al entrar, sus sesiones abiertas dejan de valer y se le quitan los bloqueos de
     * login (S6). {@code false} si no existe o {@code nueva} no es un SHA-256.
     */
    boolean restablecerContrasena(Integer idPersona, String nueva);

    /**
     * S9: cierra todas las sesiones de la persona (los JWT emitidos hasta ahora dejan de valer). Devuelve la persona,
     * para emitirle un token nuevo si es ella quien lo pide, o null si no existe.
     */
    Persona cerrarSesiones(Integer idPersona);
    boolean eliminarPersona(Integer idPersona);

    boolean borrarPorUserName(String userName);

}
