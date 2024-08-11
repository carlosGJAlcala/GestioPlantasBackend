package es.uah.huertojpa.maceta.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MacetaHasSensorId implements Serializable {
    private static final long serialVersionUID = 1591767606422877107L;
    @Column(name = "Maceta_IdMaceta", nullable = false)
    private Integer macetaIdmaceta;

    @Column(name = "Sensor_idSensor", nullable = false)
    private Integer sensorIdsensor;

    public Integer getMacetaIdmaceta() {
        return macetaIdmaceta;
    }

    public void setMacetaIdmaceta(Integer macetaIdmaceta) {
        this.macetaIdmaceta = macetaIdmaceta;
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
        MacetaHasSensorId entity = (MacetaHasSensorId) o;
        return Objects.equals(this.macetaIdmaceta, entity.macetaIdmaceta) &&
                Objects.equals(this.sensorIdsensor, entity.sensorIdsensor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(macetaIdmaceta, sensorIdsensor);
    }

}