package es.uah.huertojpa.persona.dominio.repository;

import es.uah.huertojpa.persona.dominio.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {





    @Override
    Optional<Usuario> findById(Integer integer);

    @Override
    void deleteById(Integer integer);


}