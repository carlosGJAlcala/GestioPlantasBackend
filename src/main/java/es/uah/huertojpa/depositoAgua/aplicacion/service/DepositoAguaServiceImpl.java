package es.uah.huertojpa.depositoAgua.aplicacion.service;

import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;
import es.uah.huertojpa.depositoAgua.aplicacion.state.EstadoAgua;
import es.uah.huertojpa.depositoAgua.infrastructura.database.IDepositoaguaDAO;
import es.uah.huertojpa.huerto.aplicacion.IHuertoService;
import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import es.uah.huertojpa.maceta.dominio.MacetaHasSensorRepository;
import es.uah.huertojpa.sensores.dominio.repository.SensorRepository;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDepositoAguaAdapterObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DepositoAguaServiceImpl implements IDepositoAguaService, ApplicationListener<SensorDepositoAguaAdapterObserver> {
    @Autowired
    IDepositoaguaDAO dao;
    @Autowired
    IHuertoService huertoService;
    @Autowired
    SensorRepository sensorRepository;
    @Autowired
    MacetaHasSensorRepository macetaHasSensorRepository;

    /**
     * W12 – Comprobaciones antes de dar de alta o editar un depósito (null si todo va bien). Un huerto tiene como
     * mucho un depósito: el panel, las alertas y {@code buscarporIdHuerto} dan por hecho que es único, y con dos
     * {@code findByHuertoIdhuerto} fallaba. El sensor del depósito es un ultrasonidos que no mide ninguna maceta
     * ni otro depósito.
     */
    @Override
    public String motivoNoValido(DepositoaguaDto obj) {
        boolean alta = obj.getId() == null;
        if (alta && obj.getHuertoIdhuerto() == null) return "Elige el huerto al que da agua el depósito.";
        if (obj.getCantidadMaxima() != null && obj.getCantidadMaxima() <= 0) return "La capacidad tiene que ser mayor que 0 litros.";
        if (obj.getAlturaDeposito() != null && obj.getAlturaDeposito() <= 0) return "La altura tiene que ser mayor que 0 cm.";
        if (obj.getHuertoIdhuerto() != null) {
            DepositoaguaDto otro = dao.buscarPorIdHuerto(obj.getHuertoIdhuerto());
            if (otro != null && !otro.getId().equals(obj.getId())) {
                return "El huerto " + obj.getHuertoIdhuerto() + " ya tiene un depósito (nº " + otro.getId() + ").";
            }
        }
        Integer idSensor = obj.getSensorIdsensor() == null ? null : obj.getSensorIdsensor().getId();
        if (idSensor != null) {
            if (!sensorRepository.existsById(idSensor)) return "No existe el sensor " + idSensor + ".";
            if (!macetaHasSensorRepository.findById_SensorIdsensor(idSensor).isEmpty()) {
                return "El sensor " + idSensor + " ya mide una maceta: el depósito necesita su propio sensor de distancia.";
            }
            DepositoaguaDto otro = dao.buscarPorIDSensor(idSensor);
            if (otro != null && !otro.getId().equals(obj.getId())) {
                return "El sensor " + idSensor + " ya es del depósito nº " + otro.getId() + ".";
            }
        }
        return null;
    }

    @Override
    public DepositoaguaDto buscarPorID(Integer id) {
        return dao.buscarPorID(id);
    }

    @Override
    public DepositoaguaDto buscarporIdHuerto(Integer id) {
        return dao.buscarPorIdHuerto(id);
    }

    @Override
    public List<DepositoaguaDto> buscarTodos() {
        return dao.buscarTodos();
    }

    @Override
    public boolean guardar(DepositoaguaDto obj) {
        // Alta sin id (los formularios ya no lo piden): lo genera la BD. Antes findById(null) daba un 500.
        if ((obj.getId() == null || dao.buscarPorID(obj.getId()) == null) && motivoNoValido(obj) == null) {
            dao.guardar(obj);

            return true;
        }
        return false;
    }


    @Override
    public boolean actualizar(DepositoaguaDto obj) {
        if (obj.getId() != null && dao.buscarPorID(obj.getId()) != null && motivoNoValido(obj) == null) {
            dao.actualizarDatos(obj); // W12: antes solo el estado

            return true;
        }
        return false;
    }

    @Override
    public boolean eliminar(Integer id) {
        if (dao.buscarPorID(id) != null) {
            dao.eliminar(id);

            return true;
        }
        return false;
    }

    @Override
    public List<DepositoaguaDto> buscarPorUserName(String userName) {
        List<DepositoaguaDto> resultado=new ArrayList<>();
        List<Huerto> huertos= huertoService.buscarHuertoPorUserId(userName);
        for(Huerto huerto : huertos){
            DepositoaguaDto deposito = this.buscarporIdHuerto(huerto.getId());
            if(deposito!=null){
                resultado.add(deposito);
            }
        }
        return resultado;
    }

    /**
     * Cada lectura del ultrasonidos actualiza el texto de estado del depósito («63,00% de agua»). El aviso de
     * depósito bajo ya no sale de aquí (A2, 2026-11-13): es la alerta {@code DEPOSITO_BAJO} del paquete
     * {@code alertas}, que se guarda en BD, se ve en el panel, respeta las preferencias del usuario y no se
     * duplica con el resto de correos. Antes se mandaba un correo «El deposito de agua esta vacio» con un
     * antirrepetición en memoria que se perdía al reiniciar.
     */
    @Override
    public void onApplicationEvent(SensorDepositoAguaAdapterObserver event) {
        DepositoaguaDto depositoaguaDto = dao.buscarPorIDSensor(event.getSensorDto().getId());
        if (depositoaguaDto == null || depositoaguaDto.getId() == null) return;
        Float cantidadmedida;
        try {
            cantidadmedida = Float.parseFloat(event.getSensorDto().getCantidadMedida());
        } catch (RuntimeException e) {
            return; // valor no numérico: lectura inválida
        }
        if (cantidadmedida < 0) {
            // El ultrasonidos devuelve -1 cuando no hay eco: lectura inválida, no se actualiza.
            return;
        }
        new EstadoAgua().ejecutar(depositoaguaDto, cantidadmedida);
        dao.actualizar(depositoaguaDto);
    }
}
