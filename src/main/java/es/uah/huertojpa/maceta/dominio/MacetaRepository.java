package es.uah.huertojpa.maceta.dominio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MacetaRepository extends JpaRepository<Maceta, Integer> {

    @Override
    Optional<Maceta> findById(Integer integer);

    List<Maceta> findByHuertoIdHuerto(Integer huertoIdHuerto);

/*
    List<Maceta> findByHuertoIdhuerto_Id(Integer id);
*/

    @Transactional
    @Modifying
    @Query("update Maceta m set m.posicionMacetaenElHuerto = ?1 where m.id = ?2")
    int updatePosicionMacetaenElHuertoById(String posicionMacetaenElHuerto, Integer id);
}