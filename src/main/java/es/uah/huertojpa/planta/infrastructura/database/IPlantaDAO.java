package es.uah.huertojpa.planta.infrastructura.database;

import es.uah.huertojpa.planta.aplicacion.dto.EstadoPlanta;
import es.uah.huertojpa.planta.aplicacion.dto.PlantaDto;

import java.util.List;

public interface IPlantaDAO {

    List<PlantaDto> buscarPorEstado(EstadoPlanta estado);
    List<PlantaDto>  buscarPorMacetaID(Integer id);
    PlantaDto buscarPorId(Integer id);

    List<PlantaDto> buscarTodas();
    void guardarPlanta(PlantaDto obj);

    void actualizarPlanta(PlantaDto obj);
    void eliminarPlanta(Integer id);
    void actualizarPlantaHumedad(PlantaDto obj);
    void actualizarPlantaLux(PlantaDto obj);
    void actualizarPlantaTemperatura(PlantaDto obj);
    void actualizarPlantaEstado(PlantaDto obj);
}
