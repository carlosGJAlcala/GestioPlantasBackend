package es.uah.huertojpa.persona.aplicacion;

import es.uah.huertojpa.persona.dominio.entidades.Administrador;
import es.uah.huertojpa.persona.infrastructura.database.IAdministradorDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdministradorServiceImpl implements IAdministradorService{
    @Autowired
    IAdministradorDAO administradorDAO;

    @Override
    public List<Administrador> buscarTodas() {
        return administradorDAO.buscarTodas();
    }

    @Override
    public Administrador buscarPorId(Integer id) {
        return administradorDAO.buscarPorId(id);
    }

    @Override
    public boolean guardar(Administrador persona) {
        if(administradorDAO.buscarPorId(persona.getId())==null){
            administradorDAO.guardar(persona);
            return true;
        }
        return false;
    }

    @Override
    public boolean actualizar(Administrador persona) {
        return false;
    }

    @Override
    public boolean eliminar(Integer id) {
        return false;
    }
}
