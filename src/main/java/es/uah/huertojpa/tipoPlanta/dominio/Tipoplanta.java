package es.uah.huertojpa.tipoPlanta.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "tipoplanta")
public class Tipoplanta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTipoPlanta", nullable = false)
    private Integer id;

    @Column(name = "NombrePlanta", length = 45)
    private String nombrePlanta;

    @Column(name = "Descripcion", length = 1024)
    private String descripcion;

    @Column(name = "NivelHumedadNecesarioMinimo")
    private Float nivelHumedadNecesarioMinimo;

    @Column(name = "NivelLuxNecesarioMinimo")
    private Float nivelLuxNecesarioMinimo;

    @Column(name = "NivelTemperaturaNecesarioMinimo")
    private Float nivelTemperaturaNecesarioMinimo;

    @Column(name = "NivelHumedadNecesarioMaximo")
    private Float nivelHumedadNecesarioMaximo;

    @Column(name = "NivelLuxNecesarioMaximo")
    private Float nivelLuxNecesarioMaximo;

    @Column(name = "NivelTemperaturaNecesarioMaximo")
    private Float nivelTemperaturaNecesarioMaximo;

    @Column(name = "Estaciones", length = 528)
    private String estaciones;

    @Column(name = "ZonaOrigen", length = 45)
    private String zonaOrigen;

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Float getNivelHumedadNecesarioMinimo() {
        return nivelHumedadNecesarioMinimo;
    }

    public void setNivelHumedadNecesarioMinimo(Float nivelHumedadNecesarioMinimo) {
        this.nivelHumedadNecesarioMinimo = nivelHumedadNecesarioMinimo;
    }

    public Float getNivelLuxNecesarioMinimo() {
        return nivelLuxNecesarioMinimo;
    }

    public void setNivelLuxNecesarioMinimo(Float nivelLuxNecesarioMinimo) {
        this.nivelLuxNecesarioMinimo = nivelLuxNecesarioMinimo;
    }

    public Float getNivelTemperaturaNecesarioMinimo() {
        return nivelTemperaturaNecesarioMinimo;
    }

    public void setNivelTemperaturaNecesarioMinimo(Float nivelTemperaturaNecesarioMinimo) {
        this.nivelTemperaturaNecesarioMinimo = nivelTemperaturaNecesarioMinimo;
    }

    public Float getNivelHumedadNecesarioMaximo() {
        return nivelHumedadNecesarioMaximo;
    }

    public void setNivelHumedadNecesarioMaximo(Float nivelHumedadNecesarioMaximo) {
        this.nivelHumedadNecesarioMaximo = nivelHumedadNecesarioMaximo;
    }

    public Float getNivelLuxNecesarioMaximo() {
        return nivelLuxNecesarioMaximo;
    }

    public void setNivelLuxNecesarioMaximo(Float nivelLuxNecesarioMaximo) {
        this.nivelLuxNecesarioMaximo = nivelLuxNecesarioMaximo;
    }

    public Float getNivelTemperaturaNecesarioMaximo() {
        return nivelTemperaturaNecesarioMaximo;
    }

    public void setNivelTemperaturaNecesarioMaximo(Float nivelTemperaturaNecesarioMaximo) {
        this.nivelTemperaturaNecesarioMaximo = nivelTemperaturaNecesarioMaximo;
    }

    public String getEstaciones() {
        return estaciones;
    }

    public void setEstaciones(String estaciones) {
        this.estaciones = estaciones;
    }

    public String getZonaOrigen() {
        return zonaOrigen;
    }

    public void setZonaOrigen(String zonaOrigen) {
        this.zonaOrigen = zonaOrigen;
    }

}