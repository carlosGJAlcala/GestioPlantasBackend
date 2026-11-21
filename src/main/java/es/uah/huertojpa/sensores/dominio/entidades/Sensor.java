package es.uah.huertojpa.sensores.dominio.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

// Estas entidades llegan a veces como proxy LAZY de Hibernate (p. ej. desde huerto_has_usuario o maceta_has_sensor):
// sin esto Jackson falla al serializar el campo interno «hibernateLazyInitializer» (500 en «Mis huertos»).
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    /** W12b (V10): persona que dio de alta el sensor; {@code null} en los sensores antiguos. No sale en el JSON. */
    @JsonIgnore
    @Column(name = "creado_por")
    private Integer creadoPor;

    public Integer getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(Integer creadoPor) {
        this.creadoPor = creadoPor;
    }

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