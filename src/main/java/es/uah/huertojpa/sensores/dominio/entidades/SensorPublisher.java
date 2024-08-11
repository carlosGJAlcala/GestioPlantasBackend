package es.uah.huertojpa.sensores.dominio.entidades;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SensorPublisher {
    private final ApplicationEventPublisher publisher;

    public SensorPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }
    public void publish(SensorDto sensor){
        publisher.publishEvent(new SensorAdapterObserver(this,sensor));
    }
}
