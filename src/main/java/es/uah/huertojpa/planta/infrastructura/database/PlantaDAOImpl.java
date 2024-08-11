package es.uah.huertojpa.planta.infrastructura.database;

import es.uah.huertojpa.planta.aplicacion.dto.EstadoPlanta;
import es.uah.huertojpa.planta.aplicacion.dto.PlantaDto;
import es.uah.huertojpa.planta.aplicacion.mapper.PlantaMapper;
import es.uah.huertojpa.planta.dominio.entities.Planta;
import es.uah.huertojpa.planta.dominio.repository.PlantaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PlantaDAOImpl implements IPlantaDAO {
    @Autowired
    PlantaRepository repository;
    @Autowired
    PlantaMapper plantaMapper;

    @Override
    public List<PlantaDto> buscarPorEstado(EstadoPlanta estado) {
        List<Planta> plantasentitie = repository.findByEstado(estado.toString());

        return plantasentitie.stream().map(plantaMapper.mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public  List<PlantaDto>  buscarPorMacetaID(Integer id) {
   List<Planta>  lista = repository.findByMacetaIdmaceta(id);
      return lista.stream().map(plantaMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public PlantaDto buscarPorId(Integer id) {

        Optional<Planta> optional = repository.findById(id);
        if (optional.isPresent()) {
            return plantaMapper.mapper.toDto(optional.get());
        }
        return null;

    }

    @Override
    public List<PlantaDto> buscarTodas() {
        List<Planta> plantasentitie = repository.findAll();

        return plantasentitie.stream().map(plantaMapper.mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void guardarPlanta(PlantaDto obj) {
        repository.save(plantaMapper.mapper.toEntity(obj));

    }

    @Override
    public void actualizarPlanta(PlantaDto obj) {
  /*      Planta planta = plantaMapper.mapper.toEntity(obj);
        repository.updateNombrePlantaAndFechaPlantacionAndEstadoAndTipoplantaIdtipoplantaAndMacetaIdmacetaById(planta.getNombrePlanta(), planta.getFechaPlantacion(), planta.getEstado(), planta.getTipoplantaIdtipoplanta(), planta.getMacetaIdmaceta(), planta.getId());
*/
    }

    @Override
    public void eliminarPlanta(Integer id) {
        repository.deleteById(id);

    }

    @Override
    public void actualizarPlantaHumedad(PlantaDto obj) {
        repository.updateNivelActualHumedadById(obj.getNivelActualHumedad(), obj.getId());
    }

    @Override
    public void actualizarPlantaLux(PlantaDto obj) {
        repository.updateNivelActualLuminosidadById(obj.getNivelActualLuminosidad(), obj.getId());
    }

    @Override
    public void actualizarPlantaTemperatura(PlantaDto obj) {
        repository.updateNivelActualTemperaturaById(obj.getNivelActualTemperatura(), obj.getId());
    }

    @Override
    public void actualizarPlantaEstado(PlantaDto obj) {
        repository.updateEstadoById(obj.getEstado(),obj.getId());
    }
}
