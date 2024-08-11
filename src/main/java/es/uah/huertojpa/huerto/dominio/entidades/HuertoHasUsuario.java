package es.uah.huertojpa.huerto.dominio.entidades;

import es.uah.huertojpa.persona.dominio.entidades.Usuario;
import jakarta.persistence.*;

@Entity
@Table(name = "huerto_has_usuario")
public class HuertoHasUsuario {
    @EmbeddedId
    private HuertoHasUsuarioId id;

    @MapsId("huertoIdhuerto")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Huerto_idHuerto", nullable = false)
    private Huerto huertoIdhuerto;

    @MapsId("usuarioPersonaId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Usuario_Persona_ID", nullable = false)
    private Usuario usuarioPersona;

    public HuertoHasUsuarioId getId() {
        return id;
    }

    public void setId(HuertoHasUsuarioId id) {
        this.id = id;
    }

    public Huerto getHuertoIdhuerto() {
        return huertoIdhuerto;
    }

    public void setHuertoIdhuerto(Huerto huertoIdhuerto) {
        this.huertoIdhuerto = huertoIdhuerto;
    }

    public Usuario getUsuarioPersona() {
        return usuarioPersona;
    }

    public void setUsuarioPersona(Usuario usuarioPersona) {
        this.usuarioPersona = usuarioPersona;
    }

}