package es.uah.huertojpa.maceta.infrastructura.database;

import es.uah.huertojpa.maceta.dominio.Maceta;

import java.util.List;

public interface IMacetaDAO {
    Maceta buscarPorID(Integer id);
    List<Maceta> buscarPorIDHuerto(Integer id);

    List<Maceta> buscarTodos();
    void guardar(Maceta obj);

    void actualizar(Maceta obj);
    void eliminar(Integer id);
}
