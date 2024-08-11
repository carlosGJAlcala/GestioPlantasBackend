package es.uah.huertojpa.huerto.infrastructura.database;

import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import es.uah.huertojpa.huerto.dominio.repository.HuertoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HuertoDAOImpl implements IHuertoDAO{
    @Autowired
    HuertoRepository repository;
    @Override
    public Huerto buscarPorID(Integer id) {

        Optional<Huerto> optional = repository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;

    }

    @Override
    public List<Huerto> buscarTodos() {
        return repository.findAll();
    }

    @Override
    public void guardar(Huerto obj) {
        repository.save(obj);

    }

    @Override
    public void actualizar(Huerto obj) {
        repository.updateNumeroMacetasById(obj.getNumeroMacetas(),obj.getId());

    }

    @Override
    public void eliminar(Integer id) {
        repository.deleteById(id);

    }
}
