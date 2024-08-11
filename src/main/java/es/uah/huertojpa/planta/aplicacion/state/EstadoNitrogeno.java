package es.uah.huertojpa.planta.aplicacion.state;

import es.uah.huertojpa.planta.aplicacion.dto.PlantaDto;
import org.springframework.stereotype.Component;

@Component
public class EstadoNitrogeno implements Estado{
    @Override
    public void ejecutar(PlantaDto t) {
/*        Estado feliz = new EstadoFeliz();
        Estado triste = new EstadoTriste();
        Float nivelActual=t.g();
        Float nivelMinimoRequerido=t.getNivelNitrogenoMIN();
        Float nivelMaximoRequerido=t.getNivelNitrogenoMAX();


        if (nivelActual>= nivelMinimoRequerido && nivelActual <= nivelMaximoRequerido) {

            t.setEstadoActual(feliz);
        } else {

            t.setEstadoActual( triste);
        }*/
    }
}
