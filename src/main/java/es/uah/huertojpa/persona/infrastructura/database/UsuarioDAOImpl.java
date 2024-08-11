package es.uah.huertojpa.persona.infrastructura.database;

import es.uah.huertojpa.persona.dominio.entidades.Usuario;
import es.uah.huertojpa.persona.dominio.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioDAOImpl implements  IUsuarioDAO{
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    IPersonaDAO personaDAO;
    @Override
    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario buscarPorId(Integer id) {
        Optional<Usuario> optional= usuarioRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Override
    public void guardar(Usuario usuario) {

        usuarioRepository.save(usuario);

    }

    @Override
    public void actualizar(Usuario usuario) {
        personaDAO.actualizarPersona(personaDAO.buscarPorId(usuario.getId()));

    }

    @Override
    public void eliminar(Integer id) {
        usuarioRepository.deleteById(id);

    }
}
