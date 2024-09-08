package es.uah.huertojpa.registro.infrastructura.database;

import es.uah.huertojpa.registro.dominio.entities.FechahoraHasSensor;
import es.uah.huertojpa.registro.dominio.entities.FechahoraHasSensorId;
import es.uah.huertojpa.registro.dominio.repository.FechahoraHasSensorRepository;
import es.uah.huertojpa.sensores.dominio.entidades.Sensor;
import es.uah.huertojpa.sensores.dominio.entidades.SensorMapper;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository

public class FechaHoraHasSensorDAOImpl implements IFechaHoraHasSensorDAO{
    @Autowired
    FechahoraHasSensorRepository repository;
    @Override
    public FechahoraHasSensor buscarPorID(FechahoraHasSensorId id) {
        Optional<FechahoraHasSensor> optional = repository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Override
    public List<FechahoraHasSensor> buscarTodos() {
        List<FechahoraHasSensor> resultado= repository.findAll();
        return resultado;
    }

    @Override
    public List<FechahoraHasSensor> buscarPorFecha(Integer fechahoraIdfecha) {
        List<FechahoraHasSensor> resultado= repository.findAll();
        return resultado;
    }

    @Override
    public List<FechahoraHasSensor> buscarPorSensor(Integer sensorIdsensor) {
        List<FechahoraHasSensor> resultado= repository.findAll();
        return resultado;
    }

    @Override
    public void guardar(FechahoraHasSensor obj) {
        repository.save(obj);
    }

    @Override
    public void actualizar(FechahoraHasSensor obj) {
        repository.updateCantidadRegistradaById(obj.getCantidadRegistrada(),obj.getId());

    }

    @Override
    public void eliminar(FechahoraHasSensorId id) {
        repository.deleteById(id);

    }
}
