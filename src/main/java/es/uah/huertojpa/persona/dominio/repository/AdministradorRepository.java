package es.uah.huertojpa.persona.dominio.repository;

import es.uah.huertojpa.persona.dominio.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {


    @Override
    Optional<Administrador> findById(Integer integer);

    @Override
    void deleteById(Integer integer);

}