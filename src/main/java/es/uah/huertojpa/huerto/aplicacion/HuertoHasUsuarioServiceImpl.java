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
    public List<HuertoHasUsuario> buscarPorIDUser(Integer usuarioPersonaId) {
        return dao.buscarPorIDUser(usuarioPersonaId);
    }
}
