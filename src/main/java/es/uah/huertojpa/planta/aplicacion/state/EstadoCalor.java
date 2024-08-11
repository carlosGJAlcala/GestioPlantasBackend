package es.uah.huertojpa.planta.aplicacion.state;

import es.uah.huertojpa.planta.aplicacion.dto.PlantaDto;
import org.springframework.stereotype.Component;

@Component
public class EstadoCalor implements Estado{
    @Override
    public void ejecutar(PlantaDto t) {
        Estado feliz = new EstadoFeliz();
        Estado triste = new EstadoTriste();
        Float nivelActual=t.getNivelActualTemperatura();
        Float nivelMinimoRequerido=t.getNivelTemperaturaMIN();
        Float nivelMaximoRequerido=t.getNivelTemperaturaMAX();


        if (nivelActual>= nivelMinimoRequerido && nivelActual <= nivelMaximoRequerido) {

            t.setEstadoActual(feliz);
        } else {

            t.setEstadoActual( triste);
        }

    }
}
