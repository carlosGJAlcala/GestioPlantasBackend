package es.uah.huertojpa.riego.dominio;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Un ciclo de riego de una maceta (uno o varios pulsos de bomba). Tabla creada por {@code db/riego.sql}. */
@Entity
@Table(name = "riego", indexes = @Index(name = "idx_riego_maceta_ts", columnList = "maceta_id, ts"))
public class Riego {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "maceta_id", nullable = false)
    private Integer macetaId;

    /** Momento de inicio del riego (hora del servidor: el ESP32 no tiene reloj). */
    @Column(name = "ts", nullable = false)
    private LocalDateTime ts;

    @Column(name = "duracion_ms", nullable = false)
    private Integer duracionMs;

    @Column(name = "pulsos", nullable = false)
    private Integer pulsos;

    @Column(name = "humedad_antes")
    private Float humedadAntes;

    @Column(name = "humedad_despues")
    private Float humedadDespues;

    @Column(name = "nivel_antes")
    private Float nivelDepositoAntes;

    @Column(name = "nivel_despues")
    private Float nivelDepositoDespues;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", length = 10, nullable = false)
    private MotivoRiego motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", length = 20, nullable = false)
    private ResultadoRiego resultado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getMacetaId() { return macetaId; }
    public void setMacetaId(Integer macetaId) { this.macetaId = macetaId; }
    public LocalDateTime getTs() { return ts; }
    public void setTs(LocalDateTime ts) { this.ts = ts; }
    public Integer getDuracionMs() { return duracionMs; }
    public void setDuracionMs(Integer duracionMs) { this.duracionMs = duracionMs; }
    public Integer getPulsos() { return pulsos; }
    public void setPulsos(Integer pulsos) { this.pulsos = pulsos; }
    public Float getHumedadAntes() { return humedadAntes; }
    public void setHumedadAntes(Float humedadAntes) { this.humedadAntes = humedadAntes; }
    public Float getHumedadDespues() { return humedadDespues; }
    public void setHumedadDespues(Float humedadDespues) { this.humedadDespues = humedadDespues; }
    public Float getNivelDepositoAntes() { return nivelDepositoAntes; }
    public void setNivelDepositoAntes(Float nivelDepositoAntes) { this.nivelDepositoAntes = nivelDepositoAntes; }
    public Float getNivelDepositoDespues() { return nivelDepositoDespues; }
    public void setNivelDepositoDespues(Float nivelDepositoDespues) { this.nivelDepositoDespues = nivelDepositoDespues; }
    public MotivoRiego getMotivo() { return motivo; }
    public void setMotivo(MotivoRiego motivo) { this.motivo = motivo; }
    public ResultadoRiego getResultado() { return resultado; }
    public void setResultado(ResultadoRiego resultado) { this.resultado = resultado; }

    /** Hubo bomba encendida. */
    public boolean bombaActivada() {
        return pulsos != null && pulsos > 0;
    }

    /** Incremento de humedad conseguido (puntos de %), o null si falta algún dato. */
    public Float deltaHumedad() {
        if (humedadAntes == null || humedadDespues == null) {
            return null;
        }
        return humedadDespues - humedadAntes;
    }

    /**
     * Riego fallido: la bomba se activó y la humedad apenas subió (&lt; 2 puntos).
     * Indica depósito vacío, tubo obstruido, bomba averiada o sensor fuera de la tierra.
     */
    public boolean esFallido() {
        Float delta = deltaHumedad();
        return bombaActivada() && delta != null && delta < 2.0f;
    }
}
