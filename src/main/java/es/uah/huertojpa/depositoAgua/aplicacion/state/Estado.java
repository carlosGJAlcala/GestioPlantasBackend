package es.uah.huertojpa.depositoAgua.aplicacion.state;

import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;

public interface Estado {
    public void ejecutar(DepositoaguaDto t,Float medida);
    public boolean isAlerta(DepositoaguaDto t,Float medida);
}
