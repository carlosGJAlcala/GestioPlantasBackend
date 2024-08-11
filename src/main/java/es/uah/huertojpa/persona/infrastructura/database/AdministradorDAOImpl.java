package es.uah.huertojpa.persona.infrastructura.database;

import es.uah.huertojpa.persona.dominio.entidades.Administrador;
import es.uah.huertojpa.persona.dominio.entidades.Persona;
import es.uah.huertojpa.persona.dominio.entidades.Usuario;
import es.uah.huertojpa.persona.dominio.repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AdministradorDAOImpl  implements  IAdministradorDAO{
    @Autowired
    AdministradorRepository repository;
    @Override
    public List<Administrador> buscarTodas() {
        return repository.findAll();
    }

    @Override
    public Administrador buscarPorId(Integer id) {
        Optional<Administrador> optional= repository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Override
    public void guardar(Administrador persona) {
        repository.save(persona);

    }

    @Override
    public void actualizar(Administrador persona) {

    }

    @Override
    public void eliminar(Integer id) {
        repository.deleteById(id);
    }


}
