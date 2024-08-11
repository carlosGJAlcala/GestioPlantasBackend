package es.uah.huertojpa.planta.dominio.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "planta")
public class Planta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPlanta", nullable = false)
    private Integer id;

    @Column(name = "NombrePlanta", length = 45)
    private String nombrePlanta;

    @Column(name = "FechaPlantacion")
    private LocalDate fechaPlantacion;

    @Column(name = "Estado", length = 45)
    private String estado;



    @Column(name = "NivelActualHumedad")
    private Float nivelActualHumedad;

    @Column(name = "NivelActualLuminosidad")
    private Float nivelActualLuminosidad;

    @Column(name = "NivelActualTemperatura")
    private Float nivelActualTemperatura;

    @Column(name = "TipoPlanta_idTipoPlanta", nullable = false)
    private Integer tipoplantaIdtipoplanta;

    @Column(name = "Maceta_IdMaceta", nullable = false)
    private Integer macetaIdmaceta;

    public Integer getMacetaIdmaceta() {
        return macetaIdmaceta;
    }

    public void setMacetaIdmaceta(Integer macetaIdmaceta) {
        this.macetaIdmaceta = macetaIdmaceta;
    }

    public Integer getTipoplantaIdtipoplanta() {
        return tipoplantaIdtipoplanta;
    }

    public void setTipoplantaIdtipoplanta(Integer tipoplantaIdtipoplanta) {
        this.tipoplantaIdtipoplanta = tipoplantaIdtipoplanta;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombrePlanta() {
        return nombrePlanta;
    }

    public void setNombrePlanta(String nombrePlanta) {
        this.nombrePlanta = nombrePlanta;
    }

    public LocalDate getFechaPlantacion() {
        return fechaPlantacion;
    }

    public void setFechaPlantacion(LocalDate fechaPlantacion) {
        this.fechaPlantacion = fechaPlantacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }



    public Float getNivelActualHumedad() {
        return nivelActualHumedad;
    }

    public void setNivelActualHumedad(Float nivelActualHumedad) {
        this.nivelActualHumedad = nivelActualHumedad;
    }

    public Float getNivelActualLuminosidad() {
        return nivelActualLuminosidad;
    }

    public void setNivelActualLuminosidad(Float nivelActualLuminosidad) {
        this.nivelActualLuminosidad = nivelActualLuminosidad;
    }

    public Float getNivelActualTemperatura() {
        return nivelActualTemperatura;
    }

    public void setNivelActualTemperatura(Float nivelActualTemperatura) {
        this.nivelActualTemperatura = nivelActualTemperatura;
    }

}