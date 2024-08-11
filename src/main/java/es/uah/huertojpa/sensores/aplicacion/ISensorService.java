package es.uah.huertojpa.sensores.aplicacion;

import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import es.uah.huertojpa.sensores.dominio.entidades.Sensor;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDto;

import java.util.List;

public interface ISensorService {

    SensorDto buscarPorID(Integer id);


    List<SensorDto> buscarTodos();

    List<SensorDto> buscarPorNombre(String nombre);
    boolean guardar(SensorDto obj);

    boolean actualizar(SensorDto obj);
    boolean actualizarDepositoAgua(SensorDto obj);
    boolean eliminar(Integer id);

    List<SensorDto> buscarByUserName(String userName);
}
