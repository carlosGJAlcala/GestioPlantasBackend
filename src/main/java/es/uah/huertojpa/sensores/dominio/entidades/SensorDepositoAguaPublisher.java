package es.uah.huertojpa.sensores.dominio.entidades;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SensorDepositoAguaPublisher {
    private final ApplicationEventPublisher publisher;

    public SensorDepositoAguaPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }
    public void publish(SensorDto sensor){
        System.out.println("publicar sensor agua");
        publisher.publishEvent(new SensorDepositoAguaAdapterObserver(this,sensor));
    }
}
