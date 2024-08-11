package es.uah.huertojpa.maceta.aplicacion;

import es.uah.huertojpa.huerto.aplicacion.IHuertoService;
import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import es.uah.huertojpa.maceta.dominio.Maceta;
import es.uah.huertojpa.maceta.infrastructura.database.IMacetaDAO;
import es.uah.huertojpa.persona.aplicacion.IPersonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class MacetaServiceImpl implements IMacetaService {
    @Autowired
    IMacetaDAO dao;
    @Autowired
    IHuertoService huertoService;
    @Autowired
    IPersonaService personaService;

    @Override
    public Maceta buscarPorID(Integer id) {
        return dao.buscarPorID(id);
    }

    @Override
    public List<Maceta> buscarPorIDHuerto(Integer id) {
        return dao.buscarPorIDHuerto(id);
    }

    @Override
    public List<Maceta> buscarTodos() {
        return dao.buscarTodos();
    }

    @Override
    public boolean guardar(Maceta obj) {
        if (dao.buscarPorID(obj.getId()) == null) {
            dao.guardar(obj);

            return true;
        }
        return false;
    }

    @Override
    public boolean actualizar(Maceta obj) {
        if (dao.buscarPorID(obj.getId()) != null) {
            dao.actualizar(obj);

            return true;
        }
        return false;
    }

    @Override
    public boolean eliminar(Integer id) {
        if (dao.buscarPorID(id) != null) {
            dao.eliminar(id);

            return true;
        }
        return false;
    }

    @Override
    public List<Maceta> buscarPorUserName(String userName) {
        List<Huerto> huertos = huertoService.buscarHuertoPorUserId(userName);
        List<Maceta> resultado=null;
        List<Maceta> maceta;
        for (Huerto huerto: huertos){
            maceta=dao.buscarPorIDHuerto(huerto.getId());
            if(resultado==null){

                resultado=maceta;
            }else {
                resultado.addAll(maceta);
            }
        }
        return resultado;
    }
}