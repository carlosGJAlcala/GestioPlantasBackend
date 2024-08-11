package es.uah.huertojpa.planta.aplicacion.services;

import es.uah.huertojpa.planta.aplicacion.dto.EstadoPlanta;
import es.uah.huertojpa.planta.aplicacion.dto.PlantaDto;

import java.util.List;

public interface IPlantaService {

    List<PlantaDto> buscarPorEstado(EstadoPlanta estado);
    List<PlantaDto>  buscarPorMacetaID(Integer id);
    PlantaDto buscarPorId(Integer id);

    List<PlantaDto> buscarTodas();
    boolean guardarPlanta(PlantaDto obj);

    boolean actualizarPlanta(PlantaDto obj);
    boolean eliminarPlanta(Integer id);


    List<PlantaDto> buscarPorUserName(String userName);
}
