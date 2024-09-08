package es.uah.huertojpa.registro.dominio.entities;

import es.uah.huertojpa.sensores.dominio.entidades.Sensor;
import jakarta.persistence.*;

@Entity
@Table(name = "fechahora_has_sensor")
public class FechahoraHasSensor{

        @EmbeddedId
        private FechahoraHasSensorId id;

        @Column(name = "CantidadRegistrada", length = 45)
        private String cantidadRegistrada;

        public String getCantidadRegistrada() {
            return cantidadRegistrada;
        }

        public void setCantidadRegistrada(String cantidadRegistrada) {
            this.cantidadRegistrada = cantidadRegistrada;
        }

        public FechahoraHasSensorId getId() {
            return id;
        }

        public void setId(FechahoraHasSensorId id) {
            this.id = id;
        }
/*
 TODO [JPA Buddy] create field to map the 'FechaHora_idFecha' column
 Available actions: Uncomment as is | Remove column mapping
    @MapsId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FechaHora_idFecha", nullable = false)
    private es.uah.huertojpa.registro.dominio.entities.Fechahora fechahoraIdfecha;
*/
/*
 TODO [JPA Buddy] create field to map the 'sensor_idSensor' column
 Available actions: Uncomment as is | Remove column mapping
    @MapsId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_idSensor", nullable = false)
    private es.uah.huertojpa.sensores.dominio.entidades.Sensor sensorIdsensor;
*/ }