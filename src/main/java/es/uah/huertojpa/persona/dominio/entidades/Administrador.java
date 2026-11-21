package es.uah.huertojpa.persona.dominio.entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

// Estas entidades llegan a veces como proxy LAZY de Hibernate (p. ej. desde huerto_has_usuario o maceta_has_sensor):
// sin esto Jackson falla al serializar el campo interno «hibernateLazyInitializer» (500 en «Mis huertos»).
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "administrador")
public class Administrador {
    @Id
    @Column(name = "Persona_ID", nullable = false)
    private Integer id;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }



}