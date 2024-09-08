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
    private  String estado;

    private final Integer id;
    private final Float cantidadMaxima;
    private final Float alturaDeposito;
    private final SensorDto sensorIdsensor;

    private EstadoDeposito estadoDeposito;
    private Integer huertoIdhuerto;

    public EstadoDeposito getEstadoDeposito() {
        return estadoDeposito;
    }

    public void setEstadoDeposito(EstadoDeposito estadoDeposito) {
        this.estadoDeposito = estadoDeposito;
    }


    public DepositoaguaDto(Integer id, Float cantidadMaxima, Float alturaDeposito, String estado, SensorDto sensorIdsensor,
                           Integer huertoIdhuerto) {
        this.id = id;
        this.cantidadMaxima = cantidadMaxima;
        this.alturaDeposito = alturaDeposito;
        this.estado = estado;
        this.sensorIdsensor = sensorIdsensor;
        this.huertoIdhuerto = huertoIdhuerto;
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


    public Float getAlturaDeposito() {
        return alturaDeposito;
    }

    public String getEstado() {
        return estado;
    }






    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepositoaguaDto entity = (DepositoaguaDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.cantidadMaxima, entity.cantidadMaxima) &&
                Objects.equals(this.alturaDeposito, entity.alturaDeposito) &&
                Objects.equals(this.estado, entity.estado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cantidadMaxima,  alturaDeposito, estado);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "cantidadMaxima = " + cantidadMaxima + ", " +
                "alturaDeposito = " + alturaDeposito + ", " +
                "estado = " + estado + ")";
    }

    public SensorDto getSensorIdsensor() {
        return sensorIdsensor;
    }

    public Integer getHuertoIdhuerto() {
        return huertoIdhuerto;
    }

    public void setHuertoIdhuerto(Integer huertoIdhuerto) {
        this.huertoIdhuerto = huertoIdhuerto;
    }
}