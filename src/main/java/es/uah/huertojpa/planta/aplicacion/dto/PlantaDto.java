package es.uah.huertojpa.planta.aplicacion.dto;

import es.uah.huertojpa.planta.aplicacion.state.Estado;
import es.uah.huertojpa.planta.aplicacion.state.EstadoFeliz;
import es.uah.huertojpa.tipoPlanta.dominio.Tipoplanta;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class PlantaDto implements Serializable   {
    Estado estadoActual;
    private final Integer id;
    private final String nombrePlanta;
    private final LocalDate fechaPlantacion;
    private  String estado;
    private  Integer tipoplantaIdtipoplanta;
    private  Integer macetaIdmaceta;
    private  Float nivelActualHumedad;
    private  Float nivelActualLuminosidad;
    private  Float nivelActualTemperatura;

    Float NivelNitrogenoMIN,NivelHumedadMIN;
    Float NivelNitrogenoMAX,NivelHumedadMAX;
    Float NivelTemperaturaMAX,NivelTemperaturaMIN;
    Tipoplanta tipoplanta;

    public void setNivelNitrogenoMIN(Float nivelNitrogenoMIN) {
        NivelNitrogenoMIN = nivelNitrogenoMIN;
    }

    public void setNivelHumedadMIN(Float nivelHumedadMIN) {
        NivelHumedadMIN = nivelHumedadMIN;
    }

    public void setNivelNitrogenoMAX(Float nivelNitrogenoMAX) {
        NivelNitrogenoMAX = nivelNitrogenoMAX;
    }

    public void setNivelHumedadMAX(Float nivelHumedadMAX) {
        NivelHumedadMAX = nivelHumedadMAX;
    }

    public void setNivelTemperaturaMAX(Float nivelTemperaturaMAX) {
        NivelTemperaturaMAX = nivelTemperaturaMAX;
    }

    public void setNivelTemperaturaMIN(Float nivelTemperaturaMIN) {
        NivelTemperaturaMIN = nivelTemperaturaMIN;
    }

    public Tipoplanta getTipoplanta() {
        return tipoplanta;
    }

    public void setTipoplanta(Tipoplanta tipoplanta) {
        this.tipoplanta = tipoplanta;
    }

    public PlantaDto(Integer id, String nombrePlanta, LocalDate fechaPlantacion, String estado, Integer tipoplantaIdtipoplanta, Integer macetaIdmaceta, Float nivelActualHumedad, Float nivelActualLuminosidad, Float nivelActualTemperatura) {
        this.id = id;
        this.nombrePlanta = nombrePlanta;
        this.fechaPlantacion = fechaPlantacion;
        this.estado = estado;
        this.tipoplantaIdtipoplanta = tipoplantaIdtipoplanta;
        this.macetaIdmaceta = macetaIdmaceta;
        this.nivelActualHumedad = nivelActualHumedad;
        this.nivelActualLuminosidad = nivelActualLuminosidad;
        this.nivelActualTemperatura = nivelActualTemperatura;
        this.estadoActual=new EstadoFeliz();


    }
    public void resultado() {
        estadoActual.ejecutar(this); //comprobamos el estado final
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setMacetaIdmaceta(Integer macetaIdmaceta) {
        this.macetaIdmaceta = macetaIdmaceta;
    }

    public void setNivelActualHumedad(Float nivelActualHumedad) {
        this.nivelActualHumedad = nivelActualHumedad;
    }

    public void setNivelActualLuminosidad(Float nivelActualLuminosidad) {
        this.nivelActualLuminosidad = nivelActualLuminosidad;
    }

    public void setNivelActualTemperatura(Float nivelActualTemperatura) {
        this.nivelActualTemperatura = nivelActualTemperatura;
    }

    public Integer getId() {
        return id;
    }

    public String getNombrePlanta() {
        return nombrePlanta;
    }

    public LocalDate getFechaPlantacion() {
        return fechaPlantacion;
    }

    public String getEstado() {
        return estado;
    }

    public Integer getTipoplantaIdtipoplanta() {
        return tipoplantaIdtipoplanta;
    }

    public Integer getMacetaIdmaceta() {
        return macetaIdmaceta;
    }

    public Float getNivelActualHumedad() {
        return nivelActualHumedad;
    }

    public Float getNivelActualLuminosidad() {
        return nivelActualLuminosidad;
    }

    public Float getNivelActualTemperatura() {
        return nivelActualTemperatura;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlantaDto entity = (PlantaDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.nombrePlanta, entity.nombrePlanta) &&
                Objects.equals(this.fechaPlantacion, entity.fechaPlantacion) &&
                Objects.equals(this.estado, entity.estado) &&
                Objects.equals(this.tipoplantaIdtipoplanta, entity.tipoplantaIdtipoplanta) &&
                Objects.equals(this.macetaIdmaceta, entity.macetaIdmaceta) &&
                Objects.equals(this.nivelActualHumedad, entity.nivelActualHumedad) &&
                Objects.equals(this.nivelActualLuminosidad, entity.nivelActualLuminosidad) &&
                Objects.equals(this.nivelActualTemperatura, entity.nivelActualTemperatura);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombrePlanta, fechaPlantacion, estado, tipoplantaIdtipoplanta, macetaIdmaceta, nivelActualHumedad, nivelActualLuminosidad, nivelActualTemperatura);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "nombrePlanta = " + nombrePlanta + ", " +
                "fechaPlantacion = " + fechaPlantacion + ", " +
                "estado = " + estado + ", " +
                "tipoplantaIdtipoplanta = " + tipoplantaIdtipoplanta + ", " +
                "macetaIdmaceta = " + macetaIdmaceta + ", " +
                "nivelActualHumedad = " + nivelActualHumedad + ", " +
                "nivelActualLuminosidad = " + nivelActualLuminosidad + ", " +
                "nivelActualTemperatura = " + nivelActualTemperatura + ")";
    }

    public void setEstadoActual(Estado estadoActual) {
        this.estadoActual = estadoActual;
    }

    public Estado getEstadoActual() {
        return estadoActual;
    }

    public Float getNivelNitrogenoMIN() {
        return NivelNitrogenoMIN;
    }

    public Float getNivelHumedadMIN() {
        return NivelHumedadMIN;
    }

    public Float getNivelNitrogenoMAX() {
        return NivelNitrogenoMAX;
    }

    public Float getNivelHumedadMAX() {
        return NivelHumedadMAX;
    }

    public Float getNivelTemperaturaMAX() {
        return NivelTemperaturaMAX;
    }

    public Float getNivelTemperaturaMIN() {
        return NivelTemperaturaMIN;
    }

    public void setTipoplantaIdtipoplanta(Integer tipoplantaIdtipoplanta) {
        this.tipoplantaIdtipoplanta = tipoplantaIdtipoplanta;
    }


}