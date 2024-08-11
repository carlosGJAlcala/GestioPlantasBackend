package es.uah.huertojpa.maceta.aplicacion;

import es.uah.huertojpa.maceta.dominio.Maceta;

import java.util.List;

public interface IMacetaService {

    Maceta buscarPorID(Integer id);
    List<Maceta> buscarPorIDHuerto(Integer id);

    List<Maceta> buscarTodos();
    boolean guardar(Maceta obj);

    boolean actualizar(Maceta obj);
    boolean eliminar(Integer id);

    List<Maceta> buscarPorUserName(String userName);
}
