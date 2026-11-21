package es.uah.huertojpa.maceta.infrastructura.database;

import es.uah.huertojpa.maceta.dominio.Maceta;
import es.uah.huertojpa.sensores.dominio.entidades.Sensor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
    @PersistenceContext
    private EntityManager entityManager;
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
        // W12b: el JSON solo trae el id compuesto. Con @MapsId Hibernate necesita la maceta y el sensor: sin ellos
        // fallaba con «attempted to assign id from null one-to-one property» (500 en cada alta desde la web).
        if (obj.getMacetaIdmaceta() == null) {
            obj.setMacetaIdmaceta(entityManager.getReference(Maceta.class, obj.getId().getMacetaIdmaceta()));
        }
        if (obj.getSensorIdsensor() == null) {
            obj.setSensorIdsensor(entityManager.getReference(Sensor.class, obj.getId().getSensorIdsensor()));
        }
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
