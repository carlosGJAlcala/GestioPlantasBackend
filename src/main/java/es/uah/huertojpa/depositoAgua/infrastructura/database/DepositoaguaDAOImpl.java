package es.uah.huertojpa.depositoAgua.infrastructura.database;

import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;
import es.uah.huertojpa.depositoAgua.aplicacion.mapper.DepositoaguaMapper;
import es.uah.huertojpa.depositoAgua.dominio.DepositoAgua;
import es.uah.huertojpa.depositoAgua.dominio.DepositoaguaRepository;
import es.uah.huertojpa.sensores.dominio.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class DepositoaguaDAOImpl implements IDepositoaguaDAO {
    @Autowired
    DepositoaguaRepository repository;
    @Autowired
    DepositoaguaMapper depositoaguaMapper;
    @Autowired
    SensorRepository sensorRepository;
    @Override
    public DepositoaguaDto buscarPorID(Integer id) {

        Optional<DepositoAgua> optional = repository.findById(id);
        if (optional.isPresent()) {
            return depositoaguaMapper.toDto(optional.get());
        }
        return null;

    }

    @Override
    public DepositoaguaDto buscarPorIdHuerto(Integer id) {
        Optional<DepositoAgua> optional = repository.findByHuertoIdhuerto(id);
        if (optional.isPresent()) {
            return depositoaguaMapper.toDto(optional.get());
        }
        return null;
    }

    @Override
    public DepositoaguaDto buscarPorIDSensor(Integer id) {
        Optional<DepositoAgua> optional = repository.findBySensorIdsensor_Id(id);
        if (optional.isPresent()) {
            return depositoaguaMapper.toDto(optional.get());
        }
        return null;
    }

    @Override
    public List<DepositoaguaDto> buscarTodos() {
        List<DepositoAgua> depositoaguas = repository.findAll();
        return depositoaguas.stream().map(depositoaguaMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void guardar(DepositoaguaDto obj) {

        DepositoAgua nuevo = depositoaguaMapper.toEntity(obj);
        // El sensor llega como {id}: se enlaza la fila existente (el mapper crearía un Sensor suelto con el resto
        // de campos a null).
        Integer idSensor = obj.getSensorIdsensor() == null ? null : obj.getSensorIdsensor().getId();
        nuevo.setSensorIdsensor(idSensor == null ? null : sensorRepository.getReferenceById(idSensor));
        repository.save(nuevo);
    }

    /**
     * W12 – Edición desde la web. Los campos que llegan a null se dejan como están; el sensor solo cambia si llega
     * {@code sensorIdsensor} ({@code {"id": null}} lo quita). Antes el PUT solo guardaba el estado: la capacidad y
     * la altura editadas se perdían sin avisar.
     */
    @Override
    public void actualizarDatos(DepositoaguaDto obj) {
        repository.findById(obj.getId()).ifPresent(d -> {
            if (obj.getCantidadMaxima() != null) d.setCantidadMaxima(obj.getCantidadMaxima());
            if (obj.getAlturaDeposito() != null) d.setAlturaDeposito(obj.getAlturaDeposito());
            if (obj.getEstado() != null) d.setEstado(obj.getEstado());
            if (obj.getHuertoIdhuerto() != null) d.setHuertoIdhuerto(obj.getHuertoIdhuerto());
            if (obj.getSensorIdsensor() != null) {
                Integer idSensor = obj.getSensorIdsensor().getId();
                d.setSensorIdsensor(idSensor == null ? null : sensorRepository.getReferenceById(idSensor));
            }
            repository.save(d);
        });
    }

    @Override
    public void actualizar(DepositoaguaDto obj) {
        repository.updateEstadoById(obj.getEstado(),obj.getId());
    }

    @Override
    public void eliminar(Integer id) {
        repository.deleteById(id);
    }
}
