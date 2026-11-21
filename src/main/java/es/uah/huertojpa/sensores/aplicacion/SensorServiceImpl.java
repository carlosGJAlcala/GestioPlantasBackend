package es.uah.huertojpa.sensores.aplicacion;

import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;
import es.uah.huertojpa.depositoAgua.aplicacion.service.IDepositoAguaService;
import es.uah.huertojpa.maceta.aplicacion.IMacetaHasSensorService;
import es.uah.huertojpa.maceta.dominio.MacetaHasSensor;
import es.uah.huertojpa.registro.aplicacion.IFechaHoraHasSensorService;
import es.uah.huertojpa.registro.aplicacion.IFechahoraService;
import es.uah.huertojpa.registro.dominio.entities.Fechahora;
import es.uah.huertojpa.registro.dominio.entities.FechahoraHasSensorId;
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
    @Autowired
    IFechahoraService fechahoraService;
    @Autowired
    IFechaHoraHasSensorService fechaHoraHasSensorService;
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
        // Alta sin id (los formularios ya no lo piden): lo genera la BD. Antes findById(null) daba un 500.
        if (obj.getId() == null || dao.buscarPorID(obj.getId())==null) {
            dao.guardar(obj);

            return true;
        }
        return false;
    }

    @Override
    public boolean guardar(SensorDto obj, Integer creadoPor) {
        if (obj.getId() == null || dao.buscarPorID(obj.getId()) == null) {
            dao.guardar(obj, creadoPor);
            return true;
        }
        return false;
    }

    @Override
    public boolean actualizarDatos(SensorDto obj) {
        if (obj.getId() == null || dao.buscarPorID(obj.getId()) == null) return false;
        dao.actualizarDatos(obj);
        return true;
    }

    @Override
    public boolean actualizar(SensorDto obj) {
        Fechahora fechahora=fechahoraService.getTiempoActual();
        if(fechaHoraHasSensorService.isRegistrado(fechahora,obj)){
            return false;
        }else{
            if(dao.buscarPorID(obj.getId())!=null){
                sensorPublisher.publish(obj);
                dao.actualizar(obj);

                return true;
            }
            return false;

        }

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

    /**
     * W12b: sensores de las macetas y del depósito de sus huertos, más los libres que dio de alta. Antes solo miraba
     * las macetas (el del depósito no salía), repetía un sensor usado en dos macetas y, sin macetas, daba un 500.
     */
    @Override
    public List<SensorDto> buscarByUserName(String userName) {
        return dao.visiblesPara(userName);
    }
}
