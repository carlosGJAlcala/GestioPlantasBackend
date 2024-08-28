package es.uah.huertojpa.huerto.infrastructura.database;

import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import es.uah.huertojpa.huerto.dominio.entidades.HuertoHasUsuario;
import es.uah.huertojpa.huerto.dominio.repository.HuertoHasUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HuertoHasUsuarioDAOImpl implements  IHuertoHasUsuarioDAO{
    @Autowired
    HuertoHasUsuarioRepository repository;
    @Override
    public HuertoHasUsuario buscarPorID(Integer huertoIdhuerto, Integer usuarioPersonaId){

        Optional<HuertoHasUsuario> optional = repository.findById_HuertoIdhuertoAndId_UsuarioPersonaId(huertoIdhuerto,usuarioPersonaId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Override
    public List<HuertoHasUsuario> buscarPorIDUser(Integer usuarioPersonaId) {
        return repository.findById_UsuarioPersonaId(usuarioPersonaId);
    }

    @Override
    public List<HuertoHasUsuario> buscarPorIDHuerto(Integer huertoId) {
        return repository.findById_HuertoIdhuerto(huertoId);
    }

    @Override
    public void guardar(HuertoHasUsuario obj) {
        repository.save(obj);
    }

    @Override
    public void actualizar(HuertoHasUsuario obj) {

    }

    @Override
    public void eliminar(Integer id) {
    }

    @Override
    public List<HuertoHasUsuario> buscarTodos() {
        return repository.findAll();
    }
}
