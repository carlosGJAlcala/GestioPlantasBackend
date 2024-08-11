package es.uah.huertojpa.sensores.infrastructura.database;

import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import es.uah.huertojpa.sensores.dominio.entidades.Sensor;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDto;
import es.uah.huertojpa.sensores.dominio.entidades.SensorMapper;
import es.uah.huertojpa.sensores.dominio.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class SensorDAOImpl implements ISensorDAO{
    @Autowired
    SensorRepository repository;
    @Autowired
    private SensorMapper sensorMapper;

    @Override
    public SensorDto buscarPorID(Integer id) {
        Optional<Sensor> optional = repository.findById(id);
        if (optional.isPresent()) {
            return SensorMapper.mapper.toDto(optional.get());
        }
        return null;
    }

    @Override
    public List<SensorDto> buscarTodos() {
        List<Sensor> sensores= repository.findAll();
        return sensores.stream().map(sensorMapper::toDto).collect(Collectors.toList());

    }

    @Override
    public List<SensorDto> buscarPorNombre(String nombre) {
        List<Sensor> sensores=repository.findByNombreSensor(nombre);
        return sensores.stream().map(sensorMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void guardar(SensorDto obj) {
        repository.save(SensorMapper.mapper.toEntity(obj));

    }

    @Override
    public void actualizar(SensorDto obj) {
        repository.updateCantidadMedidaById(obj.getCantidadMedida(),obj.getId());
    }

    @Override
    public void eliminar(Integer id) {
        repository.deleteById(id);

    }
}
