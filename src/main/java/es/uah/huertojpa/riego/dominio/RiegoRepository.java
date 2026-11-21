package es.uah.huertojpa.riego.dominio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RiegoRepository extends JpaRepository<Riego, Long> {

    List<Riego> findByMacetaIdAndTsBetweenOrderByTsAsc(Integer macetaId, LocalDateTime desde, LocalDateTime hasta);

    Optional<Riego> findFirstByMacetaIdAndPulsosGreaterThanOrderByTsDesc(Integer macetaId, Integer pulsos);

    /** A5 – Último riego con bomba (pulsos > 0) de cualquier maceta del huerto hasta {@code hasta}; null si ninguno. */
    @Query(value = """
            SELECT max(r.ts) FROM riego r JOIN maceta m ON m.idmaceta = r.maceta_id
            WHERE m.huerto_idhuerto = :huerto AND r.pulsos > 0 AND r.ts <= :hasta
            """, nativeQuery = true)
    LocalDateTime ultimoRiegoConBombaEnHuerto(@Param("huerto") Integer idHuerto, @Param("hasta") LocalDateTime hasta);
}
