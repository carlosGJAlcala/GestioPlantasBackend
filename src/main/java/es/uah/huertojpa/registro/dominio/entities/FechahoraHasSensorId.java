package es.uah.huertojpa.registro.dominio.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FechahoraHasSensorId implements Serializable {
    private static final long serialVersionUID = -6832399864758885727L;
    @Column(name = "FechaHora_idFecha", nullable = false)
    private Integer fechahoraIdfecha;

    @Column(name = "sensor_idSensor", nullable = false)
    private Integer sensorIdsensor;

    public Integer getFechahoraIdfecha() {
        return fechahoraIdfecha;
    }

    public void setFechahoraIdfecha(Integer fechahoraIdfecha) {
        this.fechahoraIdfecha = fechahoraIdfecha;
    }

    public Integer getSensorIdsensor() {
        return sensorIdsensor;
    }

    public void setSensorIdsensor(Integer sensorIdsensor) {
        this.sensorIdsensor = sensorIdsensor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        FechahoraHasSensorId entity = (FechahoraHasSensorId) o;
        return Objects.equals(this.sensorIdsensor, entity.sensorIdsensor) &&
                Objects.equals(this.fechahoraIdfecha, entity.fechahoraIdfecha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensorIdsensor, fechahoraIdfecha);
    }

}