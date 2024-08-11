package es.uah.huertojpa.persona.infrastructura.database;

import es.uah.huertojpa.persona.dominio.entidades.Administrador;
import es.uah.huertojpa.persona.dominio.entidades.Persona;

import java.util.List;

public interface IAdministradorDAO {
    List<Administrador> buscarTodas();
    Administrador buscarPorId(Integer id);


    void guardar(Administrador persona);

    void actualizar(Administrador persona);

    void eliminar(Integer id);
}
