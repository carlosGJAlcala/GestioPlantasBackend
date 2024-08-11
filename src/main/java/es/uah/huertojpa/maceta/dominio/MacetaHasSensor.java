package es.uah.huertojpa.maceta.dominio;

import es.uah.huertojpa.sensores.dominio.entidades.Sensor;
import jakarta.persistence.*;

@Entity
@Table(name = "maceta_has_sensor")
public class MacetaHasSensor {
    @EmbeddedId
    private MacetaHasSensorId id;

    @MapsId("macetaIdmaceta")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Maceta_IdMaceta", nullable = false)
    private Maceta macetaIdmaceta;

    @MapsId("sensorIdsensor")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Sensor_idSensor", nullable = false)
    private Sensor sensorIdsensor;

    public MacetaHasSensorId getId() {
        return id;
    }

    public void setId(MacetaHasSensorId id) {
        this.id = id;
    }

    public Maceta getMacetaIdmaceta() {
        return macetaIdmaceta;
    }

    public void setMacetaIdmaceta(Maceta macetaIdmaceta) {
        this.macetaIdmaceta = macetaIdmaceta;
    }

    public Sensor getSensorIdsensor() {
        return sensorIdsensor;
    }

    public void setSensorIdsensor(Sensor sensorIdsensor) {
        this.sensorIdsensor = sensorIdsensor;
    }

}