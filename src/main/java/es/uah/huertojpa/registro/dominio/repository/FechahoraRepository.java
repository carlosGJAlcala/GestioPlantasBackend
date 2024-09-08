package es.uah.huertojpa.registro.dominio.repository;

import es.uah.huertojpa.registro.dominio.entities.Fechahora;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FechahoraRepository extends JpaRepository<Fechahora, Integer> {


    @Override
    Optional<Fechahora> findById(Integer integer);

    Optional<Fechahora> findByAnyioAndMesAndDiaAndHora(String anyio, String mes, String dia, String hora);

    Optional<Fechahora> findByAnyioAndMesAndDiaAndHoraAndMinuto(String anyio, String mes, String dia, String hora, String minuto);


}