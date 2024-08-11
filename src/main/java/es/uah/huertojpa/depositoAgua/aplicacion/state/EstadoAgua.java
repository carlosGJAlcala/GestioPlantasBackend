package es.uah.huertojpa.depositoAgua.aplicacion.state;

import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
@Component
public class EstadoAgua implements Estado{

    @Override
    public void ejecutar(DepositoaguaDto t, Float medida) {
        DecimalFormat df = new DecimalFormat("#.00");
        Float profundidadDepo= t.getAlturaDeposito();
        Float estadoSinTruncar= ((1-(medida/profundidadDepo))*100);
        t.setEstado(df.format(estadoSinTruncar)+"% de agua");
    }
}
