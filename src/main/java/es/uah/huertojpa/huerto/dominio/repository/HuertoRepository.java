package es.uah.huertojpa.huerto.dominio.repository;

import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface HuertoRepository extends JpaRepository<Huerto, Integer> {


    @Override
    Optional<Huerto> findById(Integer integer);


    @Transactional
    @Modifying
    @Query("update Huerto h set h.numeroMacetas = ?1 where h.id = ?2")
    int updateNumeroMacetasById(Integer numeroMacetas, Integer id);
}