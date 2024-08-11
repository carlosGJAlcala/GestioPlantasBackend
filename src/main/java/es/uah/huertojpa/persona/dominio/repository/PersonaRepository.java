package es.uah.huertojpa.persona.dominio.repository;

import es.uah.huertojpa.persona.dominio.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Integer> {

    @Override
    Optional<Persona> findById(Integer integer);

    Optional<Persona> findByUsuario(String usuario);

    Optional<Persona> findByNombreAndPasswordSHA256(String nombre, String passwordSHA256);

    Optional<Persona> findByUsuarioAndPasswordSHA256(String usuario, String passwordSHA256);

    @Transactional
    @Modifying
    @Query("""
            update Persona p set p.nombre = ?1, p.apellido = ?2, p.usuario = ?3, p.passwordSHA256 = ?4, p.dni = ?5
            where p.id = ?6""")
    int updateNombreAndApellidoAndUsuarioAndPasswordSha256AndDniById(String nombre, String apellido, String usuario, String passwordSha256, String dni, Integer id);

}