package es.uah.huertojpa.registro.aplicacion;

import es.uah.huertojpa.registro.dominio.entities.Fechahora;

import java.util.List;

public interface IFechahoraService {
    Fechahora buscarPorID(Integer id);
    List<Fechahora> buscarTodos();

    Fechahora buscarPorFecha(String anyio, String mes, String dia, String hora,String minute);

    boolean guardar(Fechahora obj);

    boolean actualizar(Fechahora obj);
    boolean eliminar(Integer id);

    Fechahora getTiempoActual();
}
