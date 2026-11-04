package es.uah.huertojpa.depositoAgua.aplicacion.service;

import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;
import es.uah.huertojpa.depositoAgua.aplicacion.state.Estado;
import es.uah.huertojpa.depositoAgua.aplicacion.state.EstadoAgua;
import es.uah.huertojpa.depositoAgua.infrastructura.database.IDepositoaguaDAO;
import es.uah.huertojpa.huerto.aplicacion.IHuertoHasUsuarioService;
import es.uah.huertojpa.huerto.aplicacion.IHuertoService;
import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import es.uah.huertojpa.huerto.dominio.entidades.HuertoHasUsuario;
import es.uah.huertojpa.persona.aplicacion.IPersonaService;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDepositoAguaAdapterObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DepositoAguaServiceImpl implements IDepositoAguaService, ApplicationListener<SensorDepositoAguaAdapterObserver> {
    @Autowired
    IDepositoaguaDAO dao;
    @Autowired
    IHuertoService huertoService;
    @Autowired
    IPersonaService personaService;
    @Autowired
    IHuertoHasUsuarioService huertoHasUsuarioService;

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
        if (dao.buscarPorID(obj.getId()) == null) {
            dao.guardar(obj);

            return true;
        }
        return false;
    }


    @Override
    public boolean actualizar(DepositoaguaDto obj) {
        if (dao.buscarPorID(obj.getId()) != null) {
            dao.actualizar(obj);

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

    /** Evita mandar un correo por cada lectura (el ESP32 envía una por minuto) mientras el depósito siga bajo. */
    private static final Duration ESPERA_ENTRE_ALERTAS = Duration.ofHours(12);
    private final Map<Integer, Instant> ultimaAlertaPorDeposito = new ConcurrentHashMap<>();

    private boolean debeAvisar(Integer idDeposito) {
        Instant ahora = Instant.now();
        Instant ultima = ultimaAlertaPorDeposito.get(idDeposito);
        if (ultima != null && Duration.between(ultima, ahora).compareTo(ESPERA_ENTRE_ALERTAS) < 0) {
            return false;
        }
        ultimaAlertaPorDeposito.put(idDeposito, ahora);
        return true;
    }

    @Override
    public void onApplicationEvent(SensorDepositoAguaAdapterObserver event) {
        DepositoaguaDto depositoaguaDto;
        depositoaguaDto = dao.buscarPorIDSensor(event.getSensorDto().getId());
        if (depositoaguaDto != null && depositoaguaDto.getId() != null) {
            Estado estado = new EstadoAgua();
            Float cantidadmedida = Float.parseFloat(event.getSensorDto().getCantidadMedida());
            if (cantidadmedida < 0) {
                // El ultrasonidos devuelve -1 cuando no hay eco: lectura inválida, no se actualiza.
                return;
            }
            estado.ejecutar(depositoaguaDto, cantidadmedida);
            dao.actualizar(depositoaguaDto);
            if (!estado.isAlerta(depositoaguaDto, cantidadmedida)) {
                ultimaAlertaPorDeposito.remove(depositoaguaDto.getId()); // se ha rellenado: rearmar la alerta
            }
            if(estado.isAlerta(depositoaguaDto, cantidadmedida) && debeAvisar(depositoaguaDto.getId())){
                List<HuertoHasUsuario> huertosHasUsuarios = huertoHasUsuarioService.buscarPorIDHuerto(depositoaguaDto.getHuertoIdhuerto());
                for(HuertoHasUsuario huertoHasUsuario:huertosHasUsuarios){
                    Integer idPersona=huertoHasUsuario.getId().getUsuarioPersonaId();
                    personaService.mandarEmail(idPersona);

                }


            }

        }

    }
}
