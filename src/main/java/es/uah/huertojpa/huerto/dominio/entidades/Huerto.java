package es.uah.huertojpa.huerto.dominio.entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Estas entidades llegan a veces como proxy LAZY de Hibernate (p. ej. desde huerto_has_usuario o maceta_has_sensor):
// sin esto Jackson falla al serializar el campo interno «hibernateLazyInitializer» (500 en «Mis huertos»).
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "huerto")
public class Huerto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // V5: lo genera la BD
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