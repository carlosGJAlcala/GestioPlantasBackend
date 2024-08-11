package es.uah.huertojpa.maceta.infrastructura.database;

import es.uah.huertojpa.maceta.dominio.Maceta;
import es.uah.huertojpa.maceta.dominio.MacetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MacetaDAOImpl  implements IMacetaDAO {
    @Autowired
    MacetaRepository repository;
    @Override
    public Maceta buscarPorID(Integer id) {
        Optional<Maceta> optional = repository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Override
    public List<Maceta> buscarPorIDHuerto(Integer id) {
       /* return repository.findByHuertoIdhuerto_Id(id);*/
        return  repository.findByHuertoIdHuerto(id);
    }

    @Override
    public List<Maceta> buscarTodos() {
        return repository.findAll();

    }

    @Override
    public void guardar(Maceta obj) {
        repository.save(obj);
    }

    @Override
    public void actualizar(Maceta obj) {
        repository.updatePosicionMacetaenElHuertoById(obj.getPosicionMacetaenElHuerto(),obj.getId());
    }

    @Override
    public void eliminar(Integer id) {
        repository.deleteById(id);
    }
}
