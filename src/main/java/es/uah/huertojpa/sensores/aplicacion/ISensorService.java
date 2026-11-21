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

    /** W12b: alta guardando quién lo da de alta (el sensor libre es suyo). */
    boolean guardar(SensorDto obj, Integer creadoPor);

    /** W12b: cambia nombre, magnitud y unidades; {@code false} si no existe. */
    boolean actualizarDatos(SensorDto obj);

    boolean actualizar(SensorDto obj);
    boolean actualizarDepositoAgua(SensorDto obj);
    boolean eliminar(Integer id);

    List<SensorDto> buscarByUserName(String userName);
}
