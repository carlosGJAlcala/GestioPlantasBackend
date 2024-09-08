package es.uah.huertojpa.registro.dominio.repository;

import es.uah.huertojpa.registro.dominio.entities.FechahoraHasSensor;
import es.uah.huertojpa.registro.dominio.entities.FechahoraHasSensorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FechahoraHasSensorRepository extends JpaRepository<FechahoraHasSensor, FechahoraHasSensorId> {

    @Override
    Optional<FechahoraHasSensor> findById(FechahoraHasSensorId fechahoraHasSensorId);

    @Override
    void deleteById(FechahoraHasSensorId fechahoraHasSensorId);

    List<FechahoraHasSensor> findById_SensorIdsensor(Integer sensorIdsensor);

    List<FechahoraHasSensor> findById_FechahoraIdfecha(Integer fechahoraIdfecha);

    @Transactional
    @Modifying
    @Query("update FechahoraHasSensor f set f.cantidadRegistrada = ?1 where f.id = ?2")
    int updateCantidadRegistradaById(String cantidadRegistrada, FechahoraHasSensorId id);

}