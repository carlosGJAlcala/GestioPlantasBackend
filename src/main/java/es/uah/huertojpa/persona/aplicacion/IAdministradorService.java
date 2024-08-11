package es.uah.huertojpa.persona.aplicacion;

import es.uah.huertojpa.persona.dominio.entidades.Administrador;
import es.uah.huertojpa.persona.dominio.entidades.Persona;

import java.util.List;

public interface IAdministradorService {
    List<Administrador> buscarTodas();
    Administrador buscarPorId(Integer id);


    boolean guardar(Administrador persona);

    boolean actualizar(Administrador persona);

    boolean eliminar(Integer id);
}
