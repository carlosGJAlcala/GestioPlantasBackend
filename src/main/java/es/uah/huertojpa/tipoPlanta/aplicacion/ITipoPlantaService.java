package es.uah.huertojpa.tipoPlanta.aplicacion;

import es.uah.huertojpa.tipoPlanta.dominio.Tipoplanta;

import java.util.List;

public interface ITipoPlantaService {
    List<Tipoplanta> buscarTodos();
    Tipoplanta buscarPorId(Integer id);
    List<Tipoplanta> buscarTiposPlantasPorNivelHumedadMinimo(Float f);
    List<Tipoplanta> buscarTiposPlantasPorNivelTempMinimo(Float f);
    List<Tipoplanta> buscarTiposPlantasPorNivelNitrogenoMinimo(Float f);

    Tipoplanta buscarPorNombre(String nombre);

    boolean guardarTipoPlanta(Tipoplanta tipoPlanta);
    void actualizarTipoPlanta(Tipoplanta tipoPlanta);
    boolean eliminarTipoPlanta(Integer id);
    boolean eliminarTipoPlantaPorNombre(String id);
}
