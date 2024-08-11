package es.uah.huertojpa.depositoAgua.dominio;

import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import es.uah.huertojpa.sensores.dominio.entidades.Sensor;
import jakarta.persistence.*;

@Entity
@Table(name = "depositoagua")
public class Depositoagua {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDepositoAgua", nullable = false)
    private Integer id;

    @Column(name = "CantidadMaxima")
    private Float cantidadMaxima;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Huerto_idHuerto", nullable = false)
    private Huerto huertoIdhuerto;

    @Column(name = "AlturaDeposito")
    private Float alturaDeposito;

    @Column(name = "Estado", length = 45)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Sensor_idSensor")
    private Sensor sensorIdsensor;

    public Sensor getSensorIdsensor() {
        return sensorIdsensor;
    }

    public void setSensorIdsensor(Sensor sensorIdsensor) {
        this.sensorIdsensor = sensorIdsensor;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Float getCantidadMaxima() {
        return cantidadMaxima;
    }

    public void setCantidadMaxima(Float cantidadMaxima) {
        this.cantidadMaxima = cantidadMaxima;
    }

    public Huerto getHuertoIdhuerto() {
        return huertoIdhuerto;
    }

    public void setHuertoIdhuerto(Huerto huertoIdhuerto) {
        this.huertoIdhuerto = huertoIdhuerto;
    }

    public Float getAlturaDeposito() {
        return alturaDeposito;
    }

    public void setAlturaDeposito(Float alturaDeposito) {
        this.alturaDeposito = alturaDeposito;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}