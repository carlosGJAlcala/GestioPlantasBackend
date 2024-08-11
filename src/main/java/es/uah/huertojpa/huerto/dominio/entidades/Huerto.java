package es.uah.huertojpa.huerto.dominio.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "huerto")
public class Huerto {
    @Id
    @Column(name = "idHuerto", nullable = false)
    private Integer id;

    @Column(name = "NumeroMacetas")
    private Integer numeroMacetas;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumeroMacetas() {
        return numeroMacetas;
    }

    public void setNumeroMacetas(Integer numeroMacetas) {
        this.numeroMacetas = numeroMacetas;
    }

}