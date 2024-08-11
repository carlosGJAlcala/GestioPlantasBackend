package es.uah.huertojpa.sensores.dominio.repository;

import es.uah.huertojpa.sensores.dominio.entidades.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Integer> {

    @Override
    Optional<Sensor> findById(Integer integer);

    List<Sensor> findByNombreSensor(String nombreSensor);

    @Transactional
    @Modifying
    @Query("update Sensor s set s.cantidadMedida = ?1 where s.id = ?2")
    int updateCantidadMedidaById(String cantidadMedida, Integer id);
}