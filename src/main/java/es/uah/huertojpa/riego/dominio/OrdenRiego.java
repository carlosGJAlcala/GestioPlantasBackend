package es.uah.huertojpa.riego.dominio;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Orden «Regar ahora» pedida desde la web. El ESP32 está detrás de NAT, así que no se le puede llamar: la consulta
 * él en cada vuelta del loop ({@code GET /auth/maceta/{id}/orden}). Tabla creada por {@code db/orden_riego.sql}.
 */
@Entity
@Table(name = "orden_riego", indexes = @Index(name = "idx_orden_maceta_estado", columnList = "maceta_id, estado"))
public class OrdenRiego {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "maceta_id", nullable = false)
    private Integer macetaId;

    @Column(name = "solicitante", length = 45)
    private String solicitante;

    @Column(name = "creada", nullable = false)
    private LocalDateTime creada;

    @Column(name = "entregada")
    private LocalDateTime entregada;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 10, nullable = false)
    private EstadoOrden estado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getMacetaId() { return macetaId; }
    public void setMacetaId(Integer macetaId) { this.macetaId = macetaId; }
    public String getSolicitante() { return solicitante; }
    public void setSolicitante(String solicitante) { this.solicitante = solicitante; }
    public LocalDateTime getCreada() { return creada; }
    public void setCreada(LocalDateTime creada) { this.creada = creada; }
    public LocalDateTime getEntregada() { return entregada; }
    public void setEntregada(LocalDateTime entregada) { this.entregada = entregada; }
    public EstadoOrden getEstado() { return estado; }
    public void setEstado(EstadoOrden estado) { this.estado = estado; }
}
