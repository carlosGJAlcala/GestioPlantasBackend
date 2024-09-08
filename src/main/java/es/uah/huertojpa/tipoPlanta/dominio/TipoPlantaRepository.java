package es.uah.huertojpa.tipoPlanta.dominio;

import es.uah.huertojpa.tipoPlanta.dominio.Tipoplanta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoPlantaRepository extends JpaRepository<Tipoplanta, Integer> {
    Optional<Tipoplanta> findByNombrePlanta(String nombrePlanta);


    List<Tipoplanta> findByNivelHumedadNecesarioMinimo(Float nivelHumedadNecesarioMinimo);


    List<Tipoplanta> findByNivelTemperaturaNecesarioMinimo(Float nivelTemperaturaNecesarioMinimo);

    @Override
    Optional<Tipoplanta> findById(Integer integer);




    //update



}