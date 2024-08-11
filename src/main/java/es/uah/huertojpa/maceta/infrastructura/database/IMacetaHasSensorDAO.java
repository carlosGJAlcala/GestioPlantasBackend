package es.uah.huertojpa.maceta.infrastructura.database;

import es.uah.huertojpa.maceta.dominio.MacetaHasSensor;

import java.util.List;

public interface IMacetaHasSensorDAO {
    List<MacetaHasSensor>  buscarPorIDMaceta(Integer id);
    List<MacetaHasSensor>  buscarPorIDSensor(Integer id);

    List<MacetaHasSensor> buscarTodos();


    void guardar(MacetaHasSensor obj);


    void eliminar(MacetaHasSensor id);
}
