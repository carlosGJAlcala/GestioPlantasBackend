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

    /** W12b: alta guardando quién lo crea ({@code null} = sin dueño, p. ej. alta del administrador sin persona). */
    void guardar(SensorDto obj, Integer creadoPor);

    /** W12b: nombre, magnitud y unidades (no la medida). */
    void actualizarDatos(SensorDto obj);

    List<SensorDto> visiblesPara(String userName);

    void actualizar(SensorDto obj);
    void eliminar(Integer id);
}
