package es.uah.huertojpa.tipoPlanta.infrasctuctura;

import es.uah.huertojpa.tipoPlanta.dominio.Tipoplanta;

import java.util.List;

public interface ITipoPlantaDAO {

    List<Tipoplanta> buscarTiposPlantasPorNivelHumedadMinimo(Float f);
    List<Tipoplanta> buscarTiposPlantasPorNivelTempMinimo(Float f);
    List<Tipoplanta> buscarTiposPlantasPorNivelNitrogenoMinimo(Float f);

    List<Tipoplanta> buscarTodos();
    Tipoplanta buscarPorNombre(String nombre);

    Tipoplanta buscarPorId(Integer id);

    void guardarTipoPlanta(Tipoplanta obj);

    void actualizarTipoPlanta(Tipoplanta obj);
    void eliminarTipoPlanta(Integer id);

}
