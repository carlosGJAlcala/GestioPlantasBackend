package es.uah.huertojpa.persona.infrastructura.database;

import es.uah.huertojpa.persona.dominio.entidades.Usuario;

import java.util.List;

public interface IUsuarioDAO {

    List<Usuario> buscarTodos();
    Usuario buscarPorId(Integer id);


    void guardar(Usuario persona);

    void actualizar(Usuario persona);

    void eliminar(Integer id);


}
