package es.uah.huertojpa.persona.aplicacion;

import es.uah.huertojpa.persona.dominio.entidades.Persona;

import java.util.List;


public interface IPersonaService   {
    List<Persona> buscarTodas();
    Persona buscarPorId(Integer id);
    Persona buscarPorUserName(String userName);

    boolean guardarPersona(Persona persona);
    boolean actualizarPersona(Persona persona);
    boolean eliminarPersona(Integer idPersona);

    boolean borrarPorUserName(String userName);

    boolean mandarEmail(Integer idPersona);
}
