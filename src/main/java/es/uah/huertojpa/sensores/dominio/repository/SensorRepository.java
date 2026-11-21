package es.uah.huertojpa.sensores.dominio.repository;

import es.uah.huertojpa.sensores.dominio.entidades.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * W12b – «Mis sensores»: los que miden una maceta de sus huertos, el del depósito de sus huertos y los que dio de
     * alta y aún no están asociados a nada. Antes solo salían los de las macetas (ni el del depósito ni los libres).
     */
    @Query(value = """
            SELECT s.* FROM sensor s
            WHERE s.idsensor IN (SELECT mhs.sensor_idsensor FROM maceta_has_sensor mhs
                                 JOIN maceta m ON m.idmaceta = mhs.maceta_idmaceta
                                 JOIN huerto_has_usuario hu ON hu.huerto_idhuerto = m.huerto_idhuerto
                                 JOIN persona p ON p.id = hu.usuario_persona_id
                                 WHERE p.usuario = :usuario)
               OR s.idsensor IN (SELECT d.sensor_idsensor FROM depositoagua d
                                 JOIN huerto_has_usuario hu ON hu.huerto_idhuerto = d.huerto_idhuerto
                                 JOIN persona p ON p.id = hu.usuario_persona_id
                                 WHERE p.usuario = :usuario)
               OR (s.creado_por = (SELECT p.id FROM persona p WHERE p.usuario = :usuario)
                   AND NOT EXISTS (SELECT 1 FROM maceta_has_sensor x WHERE x.sensor_idsensor = s.idsensor)
                   AND NOT EXISTS (SELECT 1 FROM depositoagua x WHERE x.sensor_idsensor = s.idsensor))
            ORDER BY s.idsensor""", nativeQuery = true)
    List<Sensor> visiblesPara(@Param("usuario") String usuario);

    /** W12b – Edición desde la web: nombre, magnitud y unidades. La medida solo la cambia el ESP32 (PUT /auth/sensor). */
    @Transactional
    @Modifying
    @Query("update Sensor s set s.nombreSensor = ?1, s.magnitudAMedir = ?2, s.unidades = ?3 where s.id = ?4")
    int updateDatosById(String nombreSensor, String magnitudAMedir, String unidades, Integer id);
}