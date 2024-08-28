package es.uah.huertojpa.huerto.aplicacion;

import es.uah.huertojpa.huerto.dominio.entidades.HuertoHasUsuario;
import es.uah.huertojpa.huerto.infrastructura.database.IHuertoHasUsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HuertoHasUsuarioServiceImpl implements IHuertoHasUsuarioService{
    @Autowired
    IHuertoHasUsuarioDAO dao;
    @Override
    public HuertoHasUsuario buscarPorID(Integer huertoIdhuerto, Integer usuarioPersonaId) {
        return dao.buscarPorID(huertoIdhuerto,usuarioPersonaId);
    }

    @Override
    public List<HuertoHasUsuario> buscarTodos() {
        List<HuertoHasUsuario> resultado=dao.buscarTodos();
        return resultado;
    }

    @Override
    public List<HuertoHasUsuario> buscarPorIDUser(Integer usuarioPersonaId) {
        List<HuertoHasUsuario> resultado=dao.buscarPorIDUser(usuarioPersonaId);


        return resultado;

    }


    @Override
    public List<HuertoHasUsuario> buscarPorIDHuerto(Integer huertoId) {
        List<HuertoHasUsuario> resultado=dao.buscarPorIDHuerto(huertoId);

        return resultado;

    }

}
