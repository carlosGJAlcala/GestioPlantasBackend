package es.uah.huertojpa.depositoAgua.aplicacion.state;

import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
@Component
public class EstadoAgua implements Estado{
    Float porcentajeSinTruncar ,profundidadDepo;


    public boolean isAlerta(DepositoaguaDto t, Float medida) {
         this.profundidadDepo= t.getAlturaDeposito();
        this.porcentajeSinTruncar= ((1-(medida/profundidadDepo))*100);
        return porcentajeSinTruncar < 25.0F;
    }

    @Override
    public void ejecutar(DepositoaguaDto t, Float medida) {
        DecimalFormat df = new DecimalFormat("#.00");
        this. profundidadDepo= t.getAlturaDeposito();
         this.porcentajeSinTruncar= ((1-(medida/profundidadDepo))*100);
        t.setEstado(df.format(porcentajeSinTruncar)+"% de agua");
    }
}
