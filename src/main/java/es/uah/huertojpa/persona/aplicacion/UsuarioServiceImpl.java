package es.uah.huertojpa.persona.aplicacion;

import es.uah.huertojpa.persona.dominio.entidades.Persona;
import es.uah.huertojpa.persona.dominio.entidades.Usuario;
import es.uah.huertojpa.persona.infrastructura.database.IUsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UsuarioServiceImpl implements IUsuarioService{
    @Autowired
    IUsuarioDAO usuarioDAO;
    @Autowired
    IPersonaService personaService;
    @Override
    public List<Usuario> buscarTodas() {
        return usuarioDAO.buscarTodos();
    }

    @Override
    public Usuario buscarPorId(Integer id) {
        return usuarioDAO.buscarPorId(id);
    }

    @Override
    public boolean guardarUsuario(Usuario usuario) {
        if(usuarioDAO.buscarPorId(usuario.getId())==null){
            usuarioDAO.guardar(usuario);
            return true;
        }
        return false;
    }

    @Override
    public boolean actualizarUsuario(Usuario usuario) {
        if(usuarioDAO.buscarPorId(usuario.getId())!=null){
            usuarioDAO.actualizar(usuario);
            return true;
        }
        return false;
    }

    @Override
    public boolean eliminarUsuario(Integer idPersona) {
        if(usuarioDAO.buscarPorId(idPersona)!=null){
            usuarioDAO.eliminar(idPersona);
            return true;
        }
        return false;
    }

    @Override
    public Usuario buscarPorUserName(String userName) {
        Persona persona = personaService.buscarPorUserName(userName);
       return  this.buscarPorId(persona.getId());
    }
}
