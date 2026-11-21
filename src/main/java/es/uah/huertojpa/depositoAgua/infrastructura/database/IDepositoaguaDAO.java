package es.uah.huertojpa.depositoAgua.infrastructura.database;

import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;

import java.util.List;

public interface IDepositoaguaDAO {
    DepositoaguaDto buscarPorID(Integer id);
    DepositoaguaDto buscarPorIdHuerto(Integer id);

    DepositoaguaDto buscarPorIDSensor(Integer id);
    List<DepositoaguaDto> buscarTodos();
    void guardar(DepositoaguaDto obj);

    /** Solo el texto de estado (lo usa cada lectura del ultrasonidos). */
    void actualizar(DepositoaguaDto obj);

    /** Capacidad, altura, estado, huerto y sensor (edición desde la web). */
    void actualizarDatos(DepositoaguaDto obj);
    void eliminar(Integer id);
}
