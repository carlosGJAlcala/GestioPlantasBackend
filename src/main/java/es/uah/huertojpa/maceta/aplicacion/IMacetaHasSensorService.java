package es.uah.huertojpa.maceta.aplicacion;

import es.uah.huertojpa.maceta.dominio.Humedad;
import es.uah.huertojpa.maceta.dominio.MacetaHasSensor;

import java.util.List;

public interface IMacetaHasSensorService {
    List<MacetaHasSensor> buscarPorIDMaceta(Integer id);
    List<MacetaHasSensor>  buscarPorIDSensor(Integer id);
    List<MacetaHasSensor>  buscarPorUserName(String  userName);
    List<MacetaHasSensor> buscarTodos();




    void guardar(MacetaHasSensor obj);


    void eliminar(MacetaHasSensor id);
}
