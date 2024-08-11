package es.uah.huertojpa.depositoAgua.infrastructura.database;

import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;

import java.util.List;

public interface IDepositoaguaDAO {
    DepositoaguaDto buscarPorID(Integer id);
    DepositoaguaDto buscarPorUserName(Integer id);

    DepositoaguaDto buscarPorIDSensor(Integer id);
    List<DepositoaguaDto> buscarTodos();
    void guardar(DepositoaguaDto obj);

    void actualizar(DepositoaguaDto obj);
    void eliminar(Integer id);
}
