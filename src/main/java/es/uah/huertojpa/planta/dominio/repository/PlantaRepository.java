package es.uah.huertojpa.planta.dominio.repository;

import es.uah.huertojpa.planta.dominio.entities.Planta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PlantaRepository extends JpaRepository<Planta, Integer> {


    @Override
    Optional<Planta> findById(Integer integer);

    @Override
    void deleteById(Integer integer);

    List<Planta> findByEstado(String estado);

    List<Planta> findByMacetaIdmaceta(Integer macetaIdmaceta);

/*
    List<Planta> findByMacetaIdmaceta_Id(Integer id);
*/


/*
    @Transactional
    @Modifying
    @Query("""
            update Planta p set p.nombrePlanta = ?1, p.fechaPlantacion = ?2, p.estado = ?3, p.tipoplantaIdtipoplanta = ?4, p.macetaIdmaceta = ?5
            where p.id = ?6""")
    int updateNombrePlantaAndFechaPlantacionAndEstadoAndTipoplantaIdtipoplantaAndMacetaIdmacetaById(String nombrePlanta, LocalDate fechaPlantacion, String estado, TipoPlanta tipoplantaIdtipoplanta, Maceta macetaIdmaceta, Integer id);
*/



    @Transactional
    @Modifying
    @Query("update Planta p set p.nivelActualHumedad = ?1 where p.id = ?2")
    int updateNivelActualHumedadById(Float nivelActualHumedad, Integer id);

    @Transactional
    @Modifying
    @Query("update Planta p set p.nivelActualLuminosidad = ?1 where p.id = ?2")
    int updateNivelActualLuminosidadById(Float nivelActualLuminosidad, Integer id);

    @Transactional
    @Modifying
    @Query("update Planta p set p.nivelActualTemperatura = ?1 where p.id = ?2")
    int updateNivelActualTemperaturaById(Float nivelActualTemperatura, Integer id);

    @Transactional
    @Modifying
    @Query("update Planta p set p.estado = ?1 where p.id = ?2")
    int updateEstadoById(String estado, Integer id);


}