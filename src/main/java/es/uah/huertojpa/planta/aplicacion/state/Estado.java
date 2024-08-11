package es.uah.huertojpa.planta.aplicacion.state;

import es.uah.huertojpa.planta.aplicacion.dto.PlantaDto;

public interface Estado {
    public void ejecutar(PlantaDto t);

}
