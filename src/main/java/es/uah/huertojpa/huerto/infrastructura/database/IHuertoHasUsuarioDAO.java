package es.uah.huertojpa.huerto.infrastructura.database;

import es.uah.huertojpa.huerto.dominio.entidades.HuertoHasUsuario;

import java.util.List;

public interface IHuertoHasUsuarioDAO {
    HuertoHasUsuario buscarPorID(Integer huertoIdhuerto, Integer usuarioPersonaId);

    List<HuertoHasUsuario> buscarPorIDUser(Integer usuarioPersonaId);

    void guardar(HuertoHasUsuario obj);

    void actualizar(HuertoHasUsuario obj);
    void eliminar(Integer id);
}

