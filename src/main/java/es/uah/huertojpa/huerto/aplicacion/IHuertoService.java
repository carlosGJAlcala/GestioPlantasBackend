package es.uah.huertojpa.huerto.aplicacion;

import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import es.uah.huertojpa.maceta.dominio.Humedad;

import java.util.List;

public interface IHuertoService {

    Huerto buscarPorID(Integer id);


    List<Huerto> buscarTodos();
    boolean guardar(Huerto obj);

    boolean actualizar(Huerto obj);
    boolean eliminar(Integer id);

    List<Huerto> buscarHuertoPorUserId(String UserName);

    /** Crea el huerto y lo asocia al usuario {@code idPersona} (huerto_has_usuario). false si ya existe o no es usuario. */
    boolean guardarParaUsuario(Huerto obj, Integer idPersona);

}
