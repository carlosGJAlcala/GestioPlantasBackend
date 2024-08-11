package es.uah.huertojpa.persona.infrastructura.database;

import es.uah.huertojpa.persona.dominio.entidades.Persona;

import java.util.List;

public interface IPersonaDAO {
    List<Persona> buscarTodas();
    Persona buscarPorId(Integer id);
    Persona buscarPorUserName(String userName);

    void guardarPersona(Persona persona);

    void actualizarPersona(Persona persona);

    void eliminarPersona(Integer id);
    Persona buscarPorUserNameYPassword(String username,String password);
}
