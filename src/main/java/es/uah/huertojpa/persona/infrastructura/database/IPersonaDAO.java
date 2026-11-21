package es.uah.huertojpa.persona.infrastructura.database;

import es.uah.huertojpa.persona.dominio.entidades.Persona;

import java.time.LocalDateTime;
import java.util.List;

public interface IPersonaDAO {
    List<Persona> buscarTodas();
    Persona buscarPorId(Integer id);
    Persona buscarPorUserName(String userName);

    void guardarPersona(Persona persona);

    void actualizarPersona(Persona persona);

    void eliminarPersona(Integer id);
    /** S2: guarda solo la contraseña (ya cifrada con BCrypt). */
    void actualizarContrasena(Integer id, String hash);
    /** S8: contraseña nueva (ya cifrada), con la marca de «cambiarla al entrar» y la fecha del cambio. */
    void ponerContrasena(Integer id, String hash, boolean debeCambiar, LocalDateTime desde);
    /** S9: los JWT de la persona emitidos antes de {@code desde} dejan de valer. */
    void cerrarSesiones(Integer id, LocalDateTime desde);
    void registrarAcceso(Integer id, LocalDateTime cuando);
}
