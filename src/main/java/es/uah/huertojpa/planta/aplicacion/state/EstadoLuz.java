package es.uah.huertojpa.planta.aplicacion.state;

import es.uah.huertojpa.planta.aplicacion.dto.PlantaDto;
import org.springframework.stereotype.Component;

@Component
public class EstadoLuz implements Estado{
    @Override
    public void ejecutar(PlantaDto t) {
        Estado feliz = new EstadoFeliz();
        Estado triste = new EstadoTriste();
        Float nivelActual=t.getNivelActualLuminosidad();
        Float nivelMinimoRequerido=2500.0F;
        Float nivelMaximoRequerido=5500.0F;



        if (nivelActual>= nivelMinimoRequerido && nivelActual <= nivelMaximoRequerido) {

            t.setEstadoActual(feliz);
        } else {

            t.setEstadoActual( triste);
        }
    }
}
