package es.uah.huertojpa.planta.aplicacion.state;

import es.uah.huertojpa.planta.aplicacion.dto.EstadoPlanta;
import es.uah.huertojpa.planta.aplicacion.dto.PlantaDto;
import org.springframework.stereotype.Component;

@Component
public class EstadoTriste implements Estado{
    @Override
    public void ejecutar(PlantaDto t) {
        t.setEstado(EstadoPlanta.TRISTE.toString());
       }
}
