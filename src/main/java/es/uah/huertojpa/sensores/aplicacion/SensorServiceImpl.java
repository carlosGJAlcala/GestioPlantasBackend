package es.uah.huertojpa.sensores.aplicacion;

import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;
import es.uah.huertojpa.depositoAgua.aplicacion.service.IDepositoAguaService;
import es.uah.huertojpa.depositoAgua.dominio.Depositoagua;
import es.uah.huertojpa.maceta.aplicacion.IMacetaHasSensorService;
import es.uah.huertojpa.maceta.dominio.MacetaHasSensor;
import es.uah.huertojpa.sensores.dominio.entidades.Sensor;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDepositoAguaPublisher;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDto;
import es.uah.huertojpa.sensores.dominio.entidades.SensorPublisher;
import es.uah.huertojpa.sensores.infrastructura.database.ISensorDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class SensorServiceImpl implements ISensorService{
    @Autowired
    ISensorDAO dao;
    @Autowired
    SensorPublisher sensorPublisher;
    @Autowired
    SensorDepositoAguaPublisher sensorDepositoAguaPublisher;
    @Autowired
    IMacetaHasSensorService macetaHasSensorService;
    @Autowired
    IDepositoAguaService depositoAguaService;
    @Override
    public SensorDto buscarPorID(Integer id) {
        return dao.buscarPorID(id);
    }

    @Override
    public List<SensorDto> buscarTodos() {
        return dao.buscarTodos();
    }

    @Override
    public List<SensorDto> buscarPorNombre(String nombre) {
        return dao.buscarPorNombre(nombre);
    }

    @Override
    public boolean guardar(SensorDto obj) {
        if(dao.buscarPorID(obj.getId())==null){
            dao.guardar(obj);

            return true;
        }
        return false;
    }

    @Override
    public boolean actualizar(SensorDto obj) {
        if(dao.buscarPorID(obj.getId())!=null){
            sensorPublisher.publish(obj);
            dao.actualizar(obj);

            return true;
        }
        return false;
    }

    @Override
    public boolean actualizarDepositoAgua(SensorDto obj) {
        if(dao.buscarPorID(obj.getId())!=null){
            sensorDepositoAguaPublisher.publish(obj);
            dao.actualizar(obj);

            return true;
        }
        return false;
    }

    @Override
    public boolean eliminar(Integer id) {
        if(dao.buscarPorID(id)!=null){
            dao.eliminar(id);

            return true;
        }
        return false;
    }

    @Override
    public List<SensorDto> buscarByUserName(String userName) {
        List<MacetaHasSensor> macetaHasSensors= macetaHasSensorService.buscarPorUserName(userName);
        List<DepositoaguaDto> depositos = depositoAguaService.buscarPorUserName(userName);
        List<SensorDto> resultado=null;
        for(MacetaHasSensor macetaHasSensor:macetaHasSensors){
            if(resultado==null){
                resultado=new ArrayList<>();
            }
            resultado.add(this.buscarPorID(macetaHasSensor.getId().getSensorIdsensor()));

        }
  /*      for(DepositoaguaDto deposito:depositos){
            if(resultado==null){
                resultado=new ArrayList<>();
            }
            if(deposito.getSensorIdsensor().getId()!=null){
                resultado.add(this.buscarPorID(deposito.getSensorIdsensor().getId()));

            }

        }*/
if(resultado==null){
    resultado=new ArrayList<>();
}
        return resultado;
    }
}
