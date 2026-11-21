package es.uah.huertojpa.registro.dominio.repository;

import es.uah.huertojpa.registro.dominio.entities.Lectura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LecturaRepository extends JpaRepository<Lectura, Long> {

    List<Lectura> findByMacetaIdAndMagnitudAndTsBetweenOrderByTsAsc(Integer macetaId, String magnitud,
                                                                    LocalDateTime desde, LocalDateTime hasta);

    Optional<Lectura> findFirstByMacetaIdOrderByTsDesc(Integer macetaId);

    /** A2: lecturas de un sensor concreto (el ultrasonidos del depósito, que no está en ninguna maceta). */
    List<Lectura> findBySensorIdAndTsBetweenOrderByTsAsc(Integer sensorId, LocalDateTime desde, LocalDateTime hasta);

    /**
     * W7 – Serie agregada en intervalos fijos de {@code segundos} (PostgreSQL ≥ 14, {@code date_bin}): una fila por
     * intervalo con datos: [inicio, media, mínimo, máximo, nº de lecturas]. El origen fijo (2000-01-01) hace que los
     * intervalos no se muevan entre consultas (las 10:00, 10:15, 10:30…). Usa el índice (maceta_id, magnitud, ts).
     */
    @Query(value = """
            SELECT date_bin(make_interval(secs => :segundos), ts, TIMESTAMP '2000-01-01') AS inicio,
                   avg(valor) AS media, min(valor) AS minimo, max(valor) AS maximo, count(*) AS lecturas
            FROM lectura
            WHERE maceta_id = :maceta AND magnitud = :magnitud AND ts >= :desde AND ts < :hasta
            GROUP BY 1 ORDER BY 1
            """, nativeQuery = true)
    List<Object[]> serieAgregada(@Param("maceta") Integer macetaId, @Param("magnitud") String magnitud,
                                 @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                                 @Param("segundos") double segundos);

    /**
     * A4 – Serie agregada del depósito, ya en % de agua: las lecturas del ultrasonidos son la distancia (cm) del
     * sensor al agua y aquí se pasan a % con la altura del depósito, igual que {@code NivelDeposito.porcentaje}
     * (sin eco o más de 1,2 × la altura ⇒ se descarta; se recorta a 0–100). Usa el índice (sensor_id, ts).
     */
    @Query(value = """
            SELECT date_bin(make_interval(secs => :segundos), ts, TIMESTAMP '2000-01-01') AS inicio,
                   avg(p) AS media, min(p) AS minimo, max(p) AS maximo, count(*) AS lecturas
            FROM (SELECT ts, greatest(0, least(100, 100 * (1 - valor / :altura))) AS p
                  FROM lectura
                  WHERE sensor_id = :sensor AND maceta_id IS NULL AND ts >= :desde AND ts < :hasta
                    AND valor >= 0 AND valor <= :altura * 1.2) d
            GROUP BY 1 ORDER BY 1
            """, nativeQuery = true)
    List<Object[]> serieDeposito(@Param("sensor") Integer sensorId, @Param("altura") double alturaCm,
                                 @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                                 @Param("segundos") double segundos);
}
