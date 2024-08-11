package es.uah.huertojpa.tipoPlanta.aplicacion;

import es.uah.huertojpa.tipoPlanta.dominio.Tipoplanta;
import es.uah.huertojpa.tipoPlanta.infrasctuctura.ITipoPlantaDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class TipoPlantaServiceImpl implements ITipoPlantaService{
    @Autowired
    ITipoPlantaDAO tipoplantaDAO;
    @Override
    public List<Tipoplanta> buscarTodos() {
        return tipoplantaDAO.buscarTodos();
    }

    @Override
    public Tipoplanta buscarPorId(Integer id) {
        return tipoplantaDAO.buscarPorId(id);
    }

    @Override
    public List<Tipoplanta> buscarTiposPlantasPorNivelHumedadMinimo(Float f) {
        return tipoplantaDAO.buscarTiposPlantasPorNivelHumedadMinimo(f);
    }

    @Override
    public List<Tipoplanta> buscarTiposPlantasPorNivelTempMinimo(Float f) {
        return tipoplantaDAO.buscarTiposPlantasPorNivelTempMinimo(f);
    }

    @Override
    public List<Tipoplanta> buscarTiposPlantasPorNivelNitrogenoMinimo(Float f) {
        return tipoplantaDAO.buscarTiposPlantasPorNivelNitrogenoMinimo(f);
    }

    @Override
    public Tipoplanta buscarPorNombre(String nombre) {
        return tipoplantaDAO.buscarPorNombre(nombre);
    }

    @Override
    public boolean guardarTipoPlanta(Tipoplanta tipoPlanta) {
        if(tipoplantaDAO.buscarPorNombre(tipoPlanta.getNombrePlanta())==null){
            tipoplantaDAO.guardarTipoPlanta(tipoPlanta);

            return true;
        }
        return false;

    }

    @Override
    public void actualizarTipoPlanta(Tipoplanta tipoPlanta) {
        if (tipoplantaDAO.buscarPorId(tipoPlanta.getId())!=null){
            tipoplantaDAO.actualizarTipoPlanta(tipoPlanta);
        }
    }

    @Override
    public boolean eliminarTipoPlanta(Integer id) {
        if(tipoplantaDAO.buscarPorId(id)!=null){
            tipoplantaDAO.eliminarTipoPlanta(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean eliminarTipoPlantaPorNombre(String nombre) {
        if(tipoplantaDAO.buscarPorNombre(nombre)!=null){
            Tipoplanta tipo =buscarPorNombre(nombre);
            tipoplantaDAO.eliminarTipoPlanta(tipo.getId());
            return true;
        }
        return false;
    }
}
