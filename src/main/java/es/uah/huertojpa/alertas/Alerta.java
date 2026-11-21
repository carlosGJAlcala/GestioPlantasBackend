package es.uah.huertojpa.alertas;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Episodio de alerta de una maceta (tabla {@code alerta}, V6). Abierta mientras {@code fin} es null; como mucho
 * una abierta por maceta y tipo (índice único parcial).
 */
@Entity
@Table(name = "alerta")
public class Alerta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "maceta_id", nullable = false)
    private Integer macetaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 30, nullable = false)
    private TipoAlerta tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", length = 10, nullable = false)
    private NivelAlerta nivel;

    @Column(name = "mensaje", length = 255, nullable = false)
    private String mensaje;

    /** Primera revisión en la que apareció el problema. */
    @Column(name = "inicio", nullable = false)
    private LocalDateTime inicio;

    /** Última revisión en la que el problema seguía ahí. */
    @Column(name = "vista", nullable = false)
    private LocalDateTime vista;

    @Column(name = "fin")
    private LocalDateTime fin;

    @Column(name = "notificada")
    private LocalDateTime notificada;

    /** A3: el usuario pulsó «Entendido». Sigue abierta pero ya no se avisa por correo. */
    @Column(name = "silenciada")
    private LocalDateTime silenciada;

    protected Alerta() {
    }

    public Alerta(Integer macetaId, Condicion c, LocalDateTime ahora) {
        this.macetaId = macetaId;
        this.tipo = c.tipo();
        this.inicio = ahora;
        actualizar(c, ahora);
    }

    /** El problema sigue presente: se refrescan nivel y mensaje (p. ej. la humedad ha cambiado). */
    public void actualizar(Condicion c, LocalDateTime ahora) {
        this.nivel = c.nivel();
        this.mensaje = c.mensaje().length() > 255 ? c.mensaje().substring(0, 255) : c.mensaje();
        this.vista = ahora;
    }

    public boolean isAbierta() {
        return fin == null;
    }

    public String getTitulo() {
        return tipo.getTitulo();
    }

    public Long getId() { return id; }
    public Integer getMacetaId() { return macetaId; }
    public TipoAlerta getTipo() { return tipo; }
    public NivelAlerta getNivel() { return nivel; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getVista() { return vista; }
    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime fin) { this.fin = fin; }
    public LocalDateTime getNotificada() { return notificada; }
    public void setNotificada(LocalDateTime notificada) { this.notificada = notificada; }
    public LocalDateTime getSilenciada() { return silenciada; }
    public void setSilenciada(LocalDateTime silenciada) { this.silenciada = silenciada; }
}
