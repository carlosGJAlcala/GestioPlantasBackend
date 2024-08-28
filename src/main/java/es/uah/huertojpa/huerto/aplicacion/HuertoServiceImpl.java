package es.uah.huertojpa.huerto.aplicacion;

import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import es.uah.huertojpa.huerto.dominio.entidades.HuertoHasUsuario;
import es.uah.huertojpa.huerto.infrastructura.database.IHuertoDAO;
import es.uah.huertojpa.persona.aplicacion.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HuertoServiceImpl implements IHuertoService {
    @Autowired
    IHuertoDAO dao;
    @Autowired
    IHuertoHasUsuarioService huertoHasUsuarioService;
    @Autowired
    IUsuarioService usuarioService;

    @Override
    public Huerto buscarPorID(Integer id) {
        Huerto huerto = dao.buscarPorID(id);
        return huerto;
    }

    @Override
    public List<Huerto> buscarTodos() {
        return dao.buscarTodos();
    }

    @Override
    public boolean guardar(Huerto obj) {
        if(dao.buscarPorID(obj.getId())==null){
            dao.guardar(obj);

            return true;
        }
        return false;
    }

    @Override
    public boolean actualizar(Huerto obj) {
        if(dao.buscarPorID(obj.getId())!=null){
            dao.actualizar(obj);

            return true;
        }
        return false;
    }

    @Override
    public boolean eliminar(Integer id) {
        if(dao.buscarPorID(id)!=null){
            dao.eliminar(id);

            return true;
        }
        return false;
    }

    @Override
    public List<Huerto> buscarHuertoPorUserId(String userName) {
        Integer idhuerto ;
        Huerto huerto= this.buscarPorID(1);
        Integer id =usuarioService.buscarPorUserName(userName).getId();

        List<HuertoHasUsuario> huertoHasUsuarios = huertoHasUsuarioService.buscarPorIDUser(id);
        List<Huerto> huertos =new ArrayList<>();
        for (HuertoHasUsuario huertoHasUsuario : huertoHasUsuarios) {
            idhuerto = huertoHasUsuario.getId().getHuertoIdhuerto();
            huerto = this.buscarPorID(idhuerto);

            huertos.add(huerto);
        }
        return huertos;
    }
}
