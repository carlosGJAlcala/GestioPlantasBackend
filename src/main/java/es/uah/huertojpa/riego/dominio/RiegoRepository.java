package es.uah.huertojpa.riego.dominio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RiegoRepository extends JpaRepository<Riego, Long> {

    List<Riego> findByMacetaIdAndTsBetweenOrderByTsAsc(Integer macetaId, LocalDateTime desde, LocalDateTime hasta);

    Optional<Riego> findFirstByMacetaIdAndPulsosGreaterThanOrderByTsDesc(Integer macetaId, Integer pulsos);
}
