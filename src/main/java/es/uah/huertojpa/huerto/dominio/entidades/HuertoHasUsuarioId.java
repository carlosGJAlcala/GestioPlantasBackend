package es.uah.huertojpa.huerto.dominio.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class HuertoHasUsuarioId implements Serializable {
    private static final long serialVersionUID = -3700995020962702282L;
    @Column(name = "Huerto_idHuerto", nullable = false)
    private Integer huertoIdhuerto;

    @Column(name = "Usuario_Persona_ID", nullable = false)
    private Integer usuarioPersonaId;

    public Integer getHuertoIdhuerto() {
        return huertoIdhuerto;
    }

    public void setHuertoIdhuerto(Integer huertoIdhuerto) {
        this.huertoIdhuerto = huertoIdhuerto;
    }

    public Integer getUsuarioPersonaId() {
        return usuarioPersonaId;
    }

    public void setUsuarioPersonaId(Integer usuarioPersonaId) {
        this.usuarioPersonaId = usuarioPersonaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        HuertoHasUsuarioId entity = (HuertoHasUsuarioId) o;
        return Objects.equals(this.usuarioPersonaId, entity.usuarioPersonaId) &&
                Objects.equals(this.huertoIdhuerto, entity.huertoIdhuerto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioPersonaId, huertoIdhuerto);
    }

}