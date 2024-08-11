package es.uah.huertojpa.huerto.aplicacion;

import es.uah.huertojpa.huerto.dominio.entidades.Huerto;

import java.util.List;

public interface IHuertoService {

    Huerto buscarPorID(Integer id);


    List<Huerto> buscarTodos();
    boolean guardar(Huerto obj);

    boolean actualizar(Huerto obj);
    boolean eliminar(Integer id);

    List<Huerto> buscarHuertoPorUserId(String UserName);

}
