package es.uah.huertojpa.sensores.dominio.entidades;


import org.springframework.context.ApplicationEvent;

public class SensorAdapterObserver extends ApplicationEvent {
    SensorDto sensorDto;

    public SensorDto getSensorDto() {
        return sensorDto;
    }

    public SensorAdapterObserver(Object source, SensorDto sensorDto) {
        super(source);
        this.sensorDto=sensorDto;
    }
}
