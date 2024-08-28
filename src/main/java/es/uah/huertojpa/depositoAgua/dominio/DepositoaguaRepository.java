package es.uah.huertojpa.depositoAgua.dominio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface DepositoaguaRepository extends JpaRepository<DepositoAgua, Integer> {


    @Override
    Optional<DepositoAgua> findById(Integer integer);

    Optional<DepositoAgua> findBySensorIdsensor_Id(Integer id);

    Optional<DepositoAgua> findByHuertoIdhuerto_Id(Integer id);


    @Transactional
    @Modifying
    @Query("update DepositoAgua d set d.estado = ?1 where d.id = ?2")
    int updateEstadoById(String estado, Integer id);

}