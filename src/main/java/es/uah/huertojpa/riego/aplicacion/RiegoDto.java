package es.uah.huertojpa.riego.aplicacion;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.uah.huertojpa.riego.dominio.MotivoRiego;
import es.uah.huertojpa.riego.dominio.ResultadoRiego;
import es.uah.huertojpa.riego.dominio.Riego;

import java.time.LocalDateTime;

public record RiegoDto(
        Long id,
        Integer idMaceta,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime ts,
        Integer duracionMs,
        Integer pulsos,
        Float humedadAntes,
        Float humedadDespues,
        Float nivelDepositoAntes,
        Float nivelDepositoDespues,
        MotivoRiego motivo,
        ResultadoRiego resultado,
        boolean fallido) {

    public static RiegoDto de(Riego r) {
        return new RiegoDto(r.getId(), r.getMacetaId(), r.getTs(), r.getDuracionMs(), r.getPulsos(),
                r.getHumedadAntes(), r.getHumedadDespues(), r.getNivelDepositoAntes(), r.getNivelDepositoDespues(),
                r.getMotivo(), r.getResultado(), r.esFallido());
    }
}
