package es.uah.huertojpa.depositoAgua.aplicacion.dto;

import es.uah.huertojpa.depositoAgua.dominio.DepositoAgua;
import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDto;

import java.io.Serializable;
import java.util.Objects;

/**
 * DTO for {@link DepositoAgua}
 */
public class DepositoaguaDto implements Serializable {
    private Huerto huertoIdhuerto;
    private final Integer id;
    private final Float cantidadMaxima;
    private final Integer huertoIdhuertoId;
    private final Integer huertoIdhuertoNumeroMacetas;
    private final Float alturaDeposito;
    private SensorDto sensorIdsensor;
    private EstadoDeposito estadoDeposito;

    public EstadoDeposito getEstadoDeposito() {
        return estadoDeposito;
    }

    public void setEstadoDeposito(EstadoDeposito estadoDeposito) {
        this.estadoDeposito = estadoDeposito;
    }

    private  String estado;

    public DepositoaguaDto(Integer id, Float cantidadMaxima, Integer huertoIdhuertoId, Integer huertoIdhuertoNumeroMacetas, Float alturaDeposito, String estado) {
        this.id = id;
        this.cantidadMaxima = cantidadMaxima;
        this.huertoIdhuertoId = huertoIdhuertoId;
        this.huertoIdhuertoNumeroMacetas = huertoIdhuertoNumeroMacetas;
        this.alturaDeposito = alturaDeposito;
        this.estado = estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getId() {
        return id;
    }

    public Float getCantidadMaxima() {
        return cantidadMaxima;
    }

    public Integer getHuertoIdhuertoId() {
        return huertoIdhuertoId;
    }

    public Integer getHuertoIdhuertoNumeroMacetas() {
        return huertoIdhuertoNumeroMacetas;
    }

    public Float getAlturaDeposito() {
        return alturaDeposito;
    }

    public String getEstado() {
        return estado;
    }

    public void setSensorIdsensor(SensorDto sensorIdsensor) {
        this.sensorIdsensor = sensorIdsensor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepositoaguaDto entity = (DepositoaguaDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.cantidadMaxima, entity.cantidadMaxima) &&
                Objects.equals(this.huertoIdhuertoId, entity.huertoIdhuertoId) &&
                Objects.equals(this.huertoIdhuertoNumeroMacetas, entity.huertoIdhuertoNumeroMacetas) &&
                Objects.equals(this.alturaDeposito, entity.alturaDeposito) &&
                Objects.equals(this.estado, entity.estado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cantidadMaxima, huertoIdhuertoId, huertoIdhuertoNumeroMacetas, alturaDeposito, estado);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "cantidadMaxima = " + cantidadMaxima + ", " +
                "huertoIdhuertoId = " + huertoIdhuertoId + ", " +
                "huertoIdhuertoNumeroMacetas = " + huertoIdhuertoNumeroMacetas + ", " +
                "alturaDeposito = " + alturaDeposito + ", " +
                "estado = " + estado + ")";
    }

    public SensorDto getSensorIdsensor() {
        return sensorIdsensor;
    }


}