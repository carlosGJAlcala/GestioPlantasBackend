package es.uah.huertojpa.persona.dominio.entidades;

import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @Column(name = "Persona_ID", nullable = false)
    private Integer id;



    @ManyToOne
    @JoinColumn(name = "Administrador_Persona_ID", nullable = false)
    private Administrador administradorPersona;

    @Column(name = "fechaDeAlta")
    private Instant fechaDeAlta;

    @Column(name = "fechaBaja")
    private Instant fechaBaja;

    @ManyToMany
    @JoinTable(name = "huerto_has_usuario",
            inverseJoinColumns = @JoinColumn(name = "Huerto_idHuerto"))
    private Set<Huerto> huertos = new LinkedHashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }



    public Administrador getAdministradorPersona() {
        return administradorPersona;
    }

    public void setAdministradorPersona(Administrador administradorPersona) {
        this.administradorPersona = administradorPersona;
    }

    public Instant getFechaDeAlta() {
        return fechaDeAlta;
    }

    public void setFechaDeAlta(Instant fechaDeAlta) {
        this.fechaDeAlta = fechaDeAlta;
    }

    public Instant getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(Instant fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public Set<Huerto> getHuertos() {
        return huertos;
    }

    public void setHuertos(Set<Huerto> huertos) {
        this.huertos = huertos;
    }

}