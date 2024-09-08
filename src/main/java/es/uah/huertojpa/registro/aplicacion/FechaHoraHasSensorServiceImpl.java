package es.uah.huertojpa.registro.aplicacion;

import es.uah.huertojpa.registro.dominio.entities.Fechahora;
import es.uah.huertojpa.registro.dominio.entities.FechahoraHasSensor;
import es.uah.huertojpa.registro.dominio.entities.FechahoraHasSensorId;
import es.uah.huertojpa.registro.infrastructura.database.IFechaHoraHasSensorDAO;
import es.uah.huertojpa.registro.infrastructura.database.IFechahoraDAO;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service

public class FechaHoraHasSensorServiceImpl implements IFechaHoraHasSensorService{
    @Autowired
    IFechaHoraHasSensorDAO dao;
    @Override
    public FechahoraHasSensor buscarPorID(FechahoraHasSensorId id) {
        return dao.buscarPorID(id);
    }

    @Override
    public List<FechahoraHasSensor> buscarTodos() {
        return dao.buscarTodos();
    }

    @Override
    public List<FechahoraHasSensor> buscarPorFecha(Integer fechahoraIdfecha) {
        return dao.buscarPorFecha(fechahoraIdfecha);
    }

    @Override
    public List<FechahoraHasSensor> buscarPorSensor(Integer sensorIdsensor) {
        return dao.buscarPorSensor(sensorIdsensor);
    }

    @Override
    public boolean guardar(FechahoraHasSensor obj) {
            dao.guardar(obj);


        return true;
    }

    @Override
    public boolean actualizar(FechahoraHasSensor obj) {
        if(dao.buscarPorID(obj.getId())!=null){
            dao.actualizar(obj);

            return true;
        }
        return false;
    }

    @Override
    public boolean eliminar(FechahoraHasSensorId id) {
        if(dao.buscarPorID(id)!=null){
            dao.eliminar(id);

            return true;
        }
        return false;
    }

    @Override
    public boolean isRegistrado(Fechahora fechahora, SensorDto obj) {
        FechahoraHasSensorId fechahoraHasSensorId= new FechahoraHasSensorId();
        fechahoraHasSensorId.setFechahoraIdfecha(fechahora.getId());
        fechahoraHasSensorId.setSensorIdsensor(obj.getId());
      FechahoraHasSensor  fechahoraHasSensor= new FechahoraHasSensor();
        if(this.buscarPorID(fechahoraHasSensorId)!=null){
            return true;
        }else{
            fechahoraHasSensor.setId(fechahoraHasSensorId);
            fechahoraHasSensor.setCantidadRegistrada(obj.getCantidadMedida());
            this.guardar(fechahoraHasSensor);
        }
        return false;
    }
}
