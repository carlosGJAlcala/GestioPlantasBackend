package es.uah.huertojpa.persona.dominio.repository;

import es.uah.huertojpa.persona.dominio.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Integer> {

    @Override
    Optional<Persona> findById(Integer integer);

    Optional<Persona> findByUsuario(String usuario);

    /** S2: solo la contraseña (BCrypt). Las búsquedas por usuario + hash se han quitado: con sal no sirven. */
    @Transactional
    @Modifying
    @Query("update Persona p set p.passwordSHA256 = ?2 where p.id = ?1")
    int actualizarContrasena(Integer id, String hash);

    /**
     * S2/S8: nueva contraseña (BCrypt), si hay que cambiarla en el próximo login (restablecida por el admin) y desde
     * cuándo vale (los JWT anteriores dejan de servir).
     */
    @Transactional
    @Modifying
    @Query("""
            update Persona p set p.passwordSHA256 = ?2, p.debeCambiarContrasena = ?3, p.contrasenaCambiada = ?4
            where p.id = ?1""")
    int ponerContrasena(Integer id, String hash, boolean debeCambiar, LocalDateTime desde);

    /** S9: cierra todas las sesiones (los JWT emitidos antes de {@code cuando} dejan de valer). */
    @Transactional
    @Modifying
    @Query("update Persona p set p.sesionesCerradas = ?2 where p.id = ?1")
    int cerrarSesiones(Integer id, LocalDateTime cuando);

    /** Último login correcto. */
    @Transactional
    @Modifying
    @Query("update Persona p set p.ultimoAcceso = ?2 where p.id = ?1")
    int registrarAcceso(Integer id, LocalDateTime cuando);

    @Transactional
    @Modifying
    // 2026-11-13: antes no actualizaba el correo (editarlo en la web no hacía nada); ahora también los avisos (A3).
    @Query("""
            update Persona p set p.nombre = ?1, p.apellido = ?2, p.usuario = ?3, p.passwordSHA256 = ?4, p.dni = ?5,
                p.correoElectronico = ?6, p.avisosCorreo = ?7
            where p.id = ?8""")
    int actualizarFicha(String nombre, String apellido, String usuario, String passwordSha256, String dni,
                        String correo, Boolean avisosCorreo, Integer id);

}