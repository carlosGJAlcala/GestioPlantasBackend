package es.uah.huertojpa.riego.dominio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrdenRiegoRepository extends JpaRepository<OrdenRiego, Long> {

    List<OrdenRiego> findByMacetaIdAndEstadoOrderByCreadaAsc(Integer macetaId, EstadoOrden estado);

    Optional<OrdenRiego> findFirstByMacetaIdAndEstadoOrderByEntregadaDesc(Integer macetaId, EstadoOrden estado);

    /** Órdenes creadas en una ventana (métricas de latencia y disponibilidad de la placa). */
    List<OrdenRiego> findByMacetaIdAndCreadaBetween(Integer macetaId, LocalDateTime desde, LocalDateTime hasta);
}
