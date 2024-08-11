package es.uah.huertojpa.sensores.dominio.entidades;

import org.springframework.context.ApplicationEvent;

import java.io.Serializable;
import java.util.Objects;

/**
 * DTO for {@link Sensor}
 */
public class SensorDto  implements Serializable{
    private final Integer id;
    private final String nombreSensor;
    private final String magnitudAMedir;
    private final String cantidadMedida;
    private final String unidades;

    public SensorDto(Integer id, String nombreSensor, String magnitudAMedir, String cantidadMedida, String unidades) {

        this.id = id;
        this.nombreSensor = nombreSensor;
        this.magnitudAMedir = magnitudAMedir;
        this.cantidadMedida = cantidadMedida;
        this.unidades = unidades;
    }

    public Integer getId() {
        return id;
    }

    public String getNombreSensor() {
        return nombreSensor;
    }

    public String getMagnitudAMedir() {
        return magnitudAMedir;
    }

    public String getCantidadMedida() {
        return cantidadMedida;
    }

    public String getUnidades() {
        return unidades;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensorDto entity = (SensorDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.nombreSensor, entity.nombreSensor) &&
                Objects.equals(this.magnitudAMedir, entity.magnitudAMedir) &&
                Objects.equals(this.cantidadMedida, entity.cantidadMedida) &&
                Objects.equals(this.unidades, entity.unidades);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombreSensor, magnitudAMedir, cantidadMedida, unidades);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "nombreSensor = " + nombreSensor + ", " +
                "magnitudAMedir = " + magnitudAMedir + ", " +
                "cantidadMedida = " + cantidadMedida + ", " +
                "unidades = " + unidades + ")";
    }
}