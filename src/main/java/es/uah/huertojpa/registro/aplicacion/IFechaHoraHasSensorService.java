package es.uah.huertojpa.registro.aplicacion;

import es.uah.huertojpa.registro.dominio.entities.Fechahora;
import es.uah.huertojpa.registro.dominio.entities.FechahoraHasSensor;
import es.uah.huertojpa.registro.dominio.entities.FechahoraHasSensorId;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDto;

import java.util.List;

public interface IFechaHoraHasSensorService {
    FechahoraHasSensor buscarPorID(FechahoraHasSensorId id);
    List<FechahoraHasSensor> buscarTodos();

    List<FechahoraHasSensor> buscarPorFecha(Integer fechahoraIdfecha);
    List<FechahoraHasSensor> buscarPorSensor(Integer sensorIdsensor);
    boolean guardar(FechahoraHasSensor obj);

    boolean actualizar(FechahoraHasSensor obj);
    boolean eliminar(FechahoraHasSensorId id);

    boolean isRegistrado(Fechahora fechahora, SensorDto obj);
}
