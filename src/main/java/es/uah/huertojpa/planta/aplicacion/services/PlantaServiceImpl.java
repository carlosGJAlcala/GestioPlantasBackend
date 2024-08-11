package es.uah.huertojpa.planta.aplicacion.services;

import es.uah.huertojpa.maceta.aplicacion.IMacetaService;
import es.uah.huertojpa.maceta.dominio.Maceta;
import es.uah.huertojpa.maceta.dominio.MacetaHasSensor;
import es.uah.huertojpa.maceta.infrastructura.database.IMacetaHasSensorDAO;
import es.uah.huertojpa.planta.aplicacion.dto.EstadoPlanta;
import es.uah.huertojpa.planta.aplicacion.dto.PlantaDto;
import es.uah.huertojpa.planta.aplicacion.state.*;
import es.uah.huertojpa.tipoPlanta.dominio.Tipoplanta;
import es.uah.huertojpa.planta.infrastructura.database.IPlantaDAO;
import es.uah.huertojpa.tipoPlanta.infrasctuctura.ITipoPlantaDAO;
import es.uah.huertojpa.sensores.dominio.entidades.SensorAdapterObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlantaServiceImpl implements IPlantaService, ApplicationListener<SensorAdapterObserver> {
    @Autowired
    IPlantaDAO plantaDAO;
    @Autowired
    IMacetaHasSensorDAO macetaHasSensorDAO;
    @Autowired
    ITipoPlantaDAO tipoPlantaDAO;
    @Autowired
    IMacetaService macetaService;
    @Override
    public List<PlantaDto> buscarPorEstado(EstadoPlanta estado) {
        return plantaDAO.buscarPorEstado(estado);
    }

    @Override
    public List<PlantaDto> buscarPorMacetaID(Integer id) {
        return plantaDAO.buscarPorMacetaID(id);
    }

    @Override
    public PlantaDto buscarPorId(Integer id) {
        return plantaDAO.buscarPorId(id);
    }

    @Override
    public List<PlantaDto> buscarTodas() {
        return plantaDAO.buscarTodas();
    }

    @Override
    public boolean guardarPlanta(PlantaDto obj) {
        if (plantaDAO.buscarPorId(obj.getId()) == null) {
            plantaDAO.guardarPlanta(obj);

            return true;
        }
        return false;
    }

    @Override
    public boolean actualizarPlanta(PlantaDto obj) {
        if (plantaDAO.buscarPorId(obj.getId()) != null) {
            plantaDAO.actualizarPlanta(obj);

            return true;
        }
        return false;
    }

    @Override
    public boolean eliminarPlanta(Integer id) {
        if (plantaDAO.buscarPorId(id) != null) {
            plantaDAO.eliminarPlanta(id);

            return true;
        }
        return false;
    }

    @Override
    public  List<PlantaDto> buscarPorUserName(String userName) {
        List<Maceta> macetas= macetaService.buscarPorUserName(userName);
        List<PlantaDto> resultado = null;
        for(Maceta maceta: macetas){
            if(resultado==null){
                resultado=this.buscarPorMacetaID(maceta.getId());
            }else {
                resultado.addAll(this.buscarPorMacetaID(maceta.getId()));
            }
        }
        return resultado;
    }

    public PlantaDto inicializarRangosMaxyMinimosMedidas(PlantaDto plantaDto) {
        Tipoplanta tipoplanta = tipoPlantaDAO.buscarPorId(plantaDto.getTipoplantaIdtipoplanta());
        plantaDto.setTipoplanta(tipoplanta);
        plantaDto.setNivelHumedadMIN(tipoplanta.getNivelHumedadNecesarioMinimo());
        plantaDto.setNivelHumedadMAX(tipoplanta.getNivelHumedadNecesarioMaximo());
        plantaDto.setNivelTemperaturaMAX(tipoplanta.getNivelTemperaturaNecesarioMaximo());
        plantaDto.setNivelTemperaturaMIN(tipoplanta.getNivelTemperaturaNecesarioMinimo());
        return plantaDto;
    }

    @Override
    public void onApplicationEvent(SensorAdapterObserver event) {
        int idSensor;

        idSensor = event.getSensorDto().getId();
        List<MacetaHasSensor> macetaHasSensor = macetaHasSensorDAO.buscarPorIDSensor(idSensor);
        List<Integer> idMaceta, idPlanta;
        idMaceta = new ArrayList<>();
        String magnitudAMedir = event.getSensorDto().getMagnitudAMedir();
        String cantidad = event.getSensorDto().getCantidadMedida();
        Float cantidadFloat = Float.parseFloat(cantidad);

        Estado estado = new EstadoFeliz();
        for (MacetaHasSensor macetasen : macetaHasSensor) {
            idMaceta.add(macetasen.getMacetaIdmaceta().getId());
        }
        for (Integer id : idMaceta) {
            List<PlantaDto> lista = plantaDAO.buscarPorMacetaID(id);
            for (PlantaDto plantaDto : lista
            ) {
                plantaDto= this.inicializarRangosMaxyMinimosMedidas(plantaDto);
                if ("HUMEDAD".equalsIgnoreCase(magnitudAMedir)) {

                    plantaDto.setNivelActualHumedad(cantidadFloat);
                    plantaDAO.actualizarPlantaHumedad(plantaDto);
                    estado = new EstadoHumedad();


                } else if (("TEMPERATURA".equalsIgnoreCase(magnitudAMedir))) {
                    plantaDto.setNivelActualTemperatura(cantidadFloat);
                    plantaDAO.actualizarPlantaTemperatura(plantaDto);
                    estado = new EstadoCalor();
                } else if (("LUZ".equalsIgnoreCase(magnitudAMedir))) {
                    plantaDto.setNivelActualLuminosidad(cantidadFloat);
                    plantaDAO.actualizarPlantaLux(plantaDto);
                    estado = new EstadoLuz();
                }
                estado.ejecutar(plantaDto);
                plantaDto.resultado();
                plantaDAO.actualizarPlantaEstado(plantaDto);

            }


        }

    }
}
