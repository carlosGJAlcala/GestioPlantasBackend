package es.uah.huertojpa.depositoAgua.aplicacion.service;

import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;

import java.util.List;

public interface IDepositoAguaService {

    DepositoaguaDto buscarPorID(Integer id);
    DepositoaguaDto buscarporIdHuerto(Integer id);


    List<DepositoaguaDto> buscarTodos();
    boolean guardar(DepositoaguaDto obj);

    boolean actualizar(DepositoaguaDto obj);
    boolean eliminar(Integer id);

    List<DepositoaguaDto> buscarPorUserName(String userName);
}
