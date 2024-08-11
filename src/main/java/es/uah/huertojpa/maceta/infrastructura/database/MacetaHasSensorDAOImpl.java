package es.uah.huertojpa.maceta.infrastructura.database;

import es.uah.huertojpa.maceta.dominio.MacetaHasSensor;
import es.uah.huertojpa.maceta.dominio.MacetaHasSensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MacetaHasSensorDAOImpl implements IMacetaHasSensorDAO {
    @Autowired
    MacetaHasSensorRepository repository;
    @Override
    public List<MacetaHasSensor> buscarPorIDMaceta(Integer id) {
        return repository.findById_MacetaIdmaceta(id);
    }

    @Override
    public List<MacetaHasSensor> buscarPorIDSensor(Integer id) {
        return repository.findById_SensorIdsensor(id);
    }

    @Override
    public List<MacetaHasSensor> buscarTodos() {
        return repository.findAll();
    }

    @Override
    public void guardar(MacetaHasSensor obj) {
    repository.save(obj);
    }

    @Override
    public void eliminar(MacetaHasSensor id) {

        Optional<MacetaHasSensor> optional = repository.findById(id.getId());
        if (optional.isPresent()) {
            repository.deleteById(  optional.get().getId());
        }

    }
}
