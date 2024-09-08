package es.uah.huertojpa.maceta.aplicacion;

import es.uah.huertojpa.maceta.dominio.Humedad;
import es.uah.huertojpa.maceta.dominio.Maceta;
import es.uah.huertojpa.maceta.dominio.MacetaHasSensor;
import es.uah.huertojpa.maceta.infrastructura.database.IMacetaHasSensorDAO;
import es.uah.huertojpa.planta.aplicacion.services.IPlantaService;
import es.uah.huertojpa.tipoPlanta.aplicacion.ITipoPlantaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class MacetaHasSensorServiceImpl implements IMacetaHasSensorService {
    @Autowired
    IMacetaHasSensorDAO dao;
    @Autowired
    IMacetaService macetaService;

    @Override
    public List<MacetaHasSensor> buscarPorIDMaceta(Integer id) {
        return dao.buscarPorIDMaceta(id);
    }

    @Override
    public List<MacetaHasSensor> buscarPorIDSensor(Integer id) {
        return dao.buscarPorIDSensor(id);
    }

    @Override
    public List<MacetaHasSensor> buscarPorUserName(String userName) {
        List<Maceta> macetas = macetaService.buscarPorUserName(userName);

        List<MacetaHasSensor> resultado=null;
        for(Maceta maceta:macetas){
            if(resultado==null){
                resultado= this.buscarPorIDMaceta(maceta.getId());
            }else {
                resultado.addAll(this.buscarPorIDMaceta(maceta.getId()));

            }
        }
        return resultado;
    }


    @Override
    public List<MacetaHasSensor> buscarTodos() {
        return dao.buscarTodos();
    }



    @Override
    public void guardar(MacetaHasSensor obj) {
    dao.guardar(obj);
    }

    @Override
    public void eliminar(MacetaHasSensor id) {
dao.eliminar(id);
    }
}
