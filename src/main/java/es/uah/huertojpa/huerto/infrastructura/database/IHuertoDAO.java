package es.uah.huertojpa.huerto.infrastructura.database;

import es.uah.huertojpa.huerto.dominio.entidades.Huerto;

import java.util.List;

public interface IHuertoDAO {

    Huerto buscarPorID(Integer id);


    List<Huerto> buscarTodos();
    void guardar(Huerto obj);

    void actualizar(Huerto obj);
    void eliminar(Integer id);

}
