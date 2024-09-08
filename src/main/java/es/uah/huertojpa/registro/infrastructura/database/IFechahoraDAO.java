package es.uah.huertojpa.registro.infrastructura.database;

import es.uah.huertojpa.registro.dominio.entities.Fechahora;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDto;

import java.util.List;

public interface IFechahoraDAO {
    Fechahora buscarPorID(Integer id);
    List<Fechahora> buscarTodos();

    Fechahora buscarPorFecha(String anyio, String mes, String dia, String hora,String minute);


    void guardar(Fechahora obj);

    void actualizar(Fechahora obj);
    void eliminar(Integer id);
}

