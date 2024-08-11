package es.uah.huertojpa.persona.aplicacion;

import es.uah.huertojpa.persona.dominio.entidades.Usuario;

import java.util.List;

public interface IUsuarioService {

    List<Usuario> buscarTodas();
    Usuario buscarPorId(Integer id);


    boolean guardarUsuario(Usuario persona);
    boolean actualizarUsuario(Usuario persona);
    boolean eliminarUsuario(Integer idPersona);

    Usuario buscarPorUserName(String userName);
}
