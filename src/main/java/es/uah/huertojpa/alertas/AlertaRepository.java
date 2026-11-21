package es.uah.huertojpa.alertas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByMacetaIdAndFinIsNull(Integer macetaId);

    /** Abiertas, más las cerradas después de {@code desde}; las abiertas primero y luego las más recientes. */
    @Query("""
            select a from Alerta a
            where a.macetaId = :maceta and (a.fin is null or a.fin >= :desde)
            order by case when a.fin is null then 0 else 1 end, a.inicio desc
            """)
    List<Alerta> historial(@Param("maceta") Integer maceta, @Param("desde") LocalDateTime desde);

    List<Alerta> findByFinIsNullAndMacetaIdInOrderByInicioDesc(Collection<Integer> macetas);

    List<Alerta> findByFinIsNullOrderByInicioDesc();

    /**
     * A2: ¿alguna de estas macetas tiene ya abierta una alerta de este tipo avisada o silenciada? El depósito es
     * del huerto: si tiene varias macetas, todas tienen su alerta «Depósito bajo», pero el correo sale una vez.
     */
    @Query("""
            select count(a) > 0 from Alerta a
            where a.tipo = :tipo and a.fin is null and a.macetaId in :macetas
              and (a.notificada is not null or a.silenciada is not null)
            """)
    boolean yaAtendida(@Param("tipo") TipoAlerta tipo, @Param("macetas") Collection<Integer> macetas);
}
