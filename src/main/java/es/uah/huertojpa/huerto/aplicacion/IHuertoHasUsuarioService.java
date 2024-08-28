package es.uah.huertojpa.huerto.aplicacion;

import es.uah.huertojpa.huerto.dominio.entidades.HuertoHasUsuario;

import java.util.List;

public interface IHuertoHasUsuarioService {
    HuertoHasUsuario buscarPorID(Integer huertoIdhuerto, Integer usuarioPersonaId);
    List<HuertoHasUsuario>buscarTodos();

    List<HuertoHasUsuario>buscarPorIDUser( Integer usuarioPersonaId);
    List<HuertoHasUsuario> buscarPorIDHuerto(Integer huertoId);
}
