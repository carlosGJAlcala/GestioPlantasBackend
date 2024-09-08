package es.uah.huertojpa.tipoPlanta.infrasctuctura;

import es.uah.huertojpa.tipoPlanta.dominio.Tipoplanta;
import es.uah.huertojpa.tipoPlanta.dominio.TipoPlantaRepository;
import es.uah.huertojpa.tipoPlanta.infrasctuctura.ITipoPlantaDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TipoPlantaDAOImpl implements ITipoPlantaDAO {

    @Autowired
    TipoPlantaRepository tipoplanta;

    @Override
    public List<Tipoplanta> buscarTiposPlantasPorNivelHumedadMinimo(Float f) {


        return tipoplanta.findByNivelHumedadNecesarioMinimo(f);
    }

    @Override
    public List<Tipoplanta> buscarTiposPlantasPorNivelTempMinimo(Float f) {
        return tipoplanta.findByNivelTemperaturaNecesarioMinimo(f);
    }

    @Override
    public List<Tipoplanta> buscarTiposPlantasPorNivelNitrogenoMinimo(Float f) {
        return null;
    }

    @Override
    public List<Tipoplanta> buscarTodos() {
        return tipoplanta.findAll();
    }

    @Override
    public Tipoplanta buscarPorNombre(String nombre) {
        Optional<Tipoplanta> optional = tipoplanta.findByNombrePlanta(nombre);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Override
    public Tipoplanta buscarPorId(Integer id) {
        Optional<Tipoplanta> optional = tipoplanta.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Override
    public void guardarTipoPlanta(Tipoplanta obj) {
        tipoplanta.save(obj);

    }

    @Override
    public void actualizarTipoPlanta(Tipoplanta obj) {
        this.eliminarTipoPlanta(obj.getId());
        tipoplanta.save(obj);
    }

    @Override
    public void eliminarTipoPlanta(Integer id) {
        tipoplanta.deleteById(id);

    }
}
