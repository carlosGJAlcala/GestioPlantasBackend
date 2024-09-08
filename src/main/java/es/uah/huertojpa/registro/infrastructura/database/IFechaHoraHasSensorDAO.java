package es.uah.huertojpa.registro.infrastructura.database;

import es.uah.huertojpa.registro.dominio.entities.Fechahora;
import es.uah.huertojpa.registro.dominio.entities.FechahoraHasSensor;
import es.uah.huertojpa.registro.dominio.entities.FechahoraHasSensorId;

import java.util.List;

public interface IFechaHoraHasSensorDAO {
    FechahoraHasSensor buscarPorID(FechahoraHasSensorId id);
    List<FechahoraHasSensor> buscarTodos();

    List<FechahoraHasSensor> buscarPorFecha(Integer fechahoraIdfecha);
    List<FechahoraHasSensor> buscarPorSensor(Integer sensorIdsensor);
    void guardar(FechahoraHasSensor obj);

    void actualizar(FechahoraHasSensor obj);
    void eliminar(FechahoraHasSensorId id);
}
