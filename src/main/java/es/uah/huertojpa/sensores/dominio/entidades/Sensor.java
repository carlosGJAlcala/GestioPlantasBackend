package es.uah.huertojpa.sensores.dominio.entidades;

import jakarta.persistence.*;

@Entity
@Table(name = "sensor")
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSensor", nullable = false)
    private Integer id;

    @Column(name = "nombreSensor", length = 45)
    private String nombreSensor;

    @Column(name = "MagnitudAMedir", length = 45)
    private String magnitudAMedir;

    @Column(name = "CantidadMedida", length = 45)
    private String cantidadMedida;

    @Column(name = "Unidades", length = 45)
    private String unidades;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreSensor() {
        return nombreSensor;
    }

    public void setNombreSensor(String nombreSensor) {
        this.nombreSensor = nombreSensor;
    }

    public String getMagnitudAMedir() {
        return magnitudAMedir;
    }

    public void setMagnitudAMedir(String magnitudAMedir) {
        this.magnitudAMedir = magnitudAMedir;
    }

    public String getCantidadMedida() {
        return cantidadMedida;
    }

    public void setCantidadMedida(String cantidadMedida) {
        this.cantidadMedida = cantidadMedida;
    }

    public String getUnidades() {
        return unidades;
    }

    public void setUnidades(String unidades) {
        this.unidades = unidades;
    }

}