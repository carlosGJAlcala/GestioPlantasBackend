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

import java.util.ArrayList;
import java.util.List;

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
        return dao.buscarPorUserName(id);
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
        List<DepositoaguaDto> resultado=null;
        List<Huerto> huertos= huertoService.buscarHuertoPorUserId(userName);
        for(Huerto huerto : huertos){
            if(resultado==null){
                resultado=new ArrayList<>();
            }
            resultado.add(this.buscarporIdHuerto(huerto.getId()));
        }
        return resultado;
    }

    @Override
    public void onApplicationEvent(SensorDepositoAguaAdapterObserver event) {
        DepositoaguaDto depositoaguaDto;
        if (dao.buscarPorIDSensor(event.getSensorDto().getId()).getId() != null) {
            depositoaguaDto = dao.buscarPorIDSensor(event.getSensorDto().getId());
            Estado estado = new EstadoAgua();
            Float cantidadmedida = Float.parseFloat(event.getSensorDto().getCantidadMedida());
            estado.ejecutar(depositoaguaDto, cantidadmedida);
            dao.actualizar(depositoaguaDto);
            if(estado.isAlerta(depositoaguaDto, cantidadmedida)){
                List<HuertoHasUsuario> huertosHasUsuarios = huertoHasUsuarioService.buscarPorIDHuerto(depositoaguaDto.getHuertoIdhuertoId());
                for(HuertoHasUsuario huertoHasUsuario:huertosHasUsuarios){
                    Integer idPersona=huertoHasUsuario.getId().getUsuarioPersonaId();
                    personaService.mandarEmail(idPersona);

                }


            }

        }

    }
}
