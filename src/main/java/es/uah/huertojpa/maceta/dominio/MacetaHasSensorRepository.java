package es.uah.huertojpa.maceta.dominio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MacetaHasSensorRepository extends JpaRepository<MacetaHasSensor, MacetaHasSensorId> {
    List<MacetaHasSensor> findById_MacetaIdmaceta(Integer macetaIdmaceta)
            ;

    List<MacetaHasSensor> findById_SensorIdsensor(Integer sensorIdsensor);


}