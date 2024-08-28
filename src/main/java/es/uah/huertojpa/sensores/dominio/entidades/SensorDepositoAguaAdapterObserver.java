package es.uah.huertojpa.sensores.dominio.entidades;


import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;


public class SensorDepositoAguaAdapterObserver extends ApplicationEvent {
    SensorDto sensorDto;


    public SensorDto getSensorDto() {
        return sensorDto;
    }

    public SensorDepositoAguaAdapterObserver(Object source, SensorDto sensorDto) {
        super(source);
        this.sensorDto=sensorDto;
    }
}
