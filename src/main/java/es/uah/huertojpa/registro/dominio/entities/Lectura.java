package es.uah.huertojpa.registro.dominio.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * P-DB1 – Una lectura de un sensor con valor numérico y fecha real (tabla {@code lectura}, migración V3).
 * Sustituye a {@code fechahora}/{@code fechahora_has_sensor} para consultas por rango y métricas.
 */
@Entity
@Table(name = "lectura", indexes = {
        @Index(name = "idx_lectura_maceta_magnitud_ts", columnList = "maceta_id, magnitud, ts"),
        @Index(name = "idx_lectura_sensor_ts", columnList = "sensor_id, ts")})
public class Lectura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "sensor_id", nullable = false)
    private Integer sensorId;

    /** null para sensores que no están en una maceta (p. ej. el del depósito). */
    @Column(name = "maceta_id")
    private Integer macetaId;

    /** HUMEDAD, TEMPERATURA, LUZ, o la magnitud que declare el sensor (en mayúsculas). */
    @Column(name = "magnitud", length = 20, nullable = false)
    private String magnitud;

    @Column(name = "valor", nullable = false)
    private Float valor;

    @Column(name = "ts", nullable = false)
    private LocalDateTime ts;

    public Lectura() {
    }

    public Lectura(Integer sensorId, Integer macetaId, String magnitud, Float valor, LocalDateTime ts) {
        this.sensorId = sensorId;
        this.macetaId = macetaId;
        this.magnitud = magnitud;
        this.valor = valor;
        this.ts = ts;
    }

    public Long getId() { return id; }
    public Integer getSensorId() { return sensorId; }
    public Integer getMacetaId() { return macetaId; }
    public String getMagnitud() { return magnitud; }
    public Float getValor() { return valor; }
    public LocalDateTime getTs() { return ts; }
}
