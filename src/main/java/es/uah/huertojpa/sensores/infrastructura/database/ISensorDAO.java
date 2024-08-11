package es.uah.huertojpa.sensores.infrastructura.database;

import es.uah.huertojpa.huerto.dominio.entidades.HuertoHasUsuario;
import es.uah.huertojpa.sensores.dominio.entidades.Sensor;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDto;

import java.util.List;

public interface ISensorDAO {

    SensorDto buscarPorID(Integer id);
    List<SensorDto> buscarTodos();

    List<SensorDto> buscarPorNombre(String nombre);

    void guardar(SensorDto obj);

    void actualizar(SensorDto obj);
    void eliminar(Integer id);
}
