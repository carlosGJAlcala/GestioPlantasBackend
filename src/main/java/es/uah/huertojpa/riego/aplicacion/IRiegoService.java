package es.uah.huertojpa.riego.aplicacion;

import java.time.LocalDateTime;
import java.util.List;

public interface IRiegoService {
    /** Registra un ciclo de riego informado por el ESP32. Devuelve null si la maceta no existe o el cuerpo es inválido. */
    RiegoDto registrar(RiegoRequest request);

    List<RiegoDto> buscarPorMaceta(Integer idMaceta, LocalDateTime desde, LocalDateTime hasta);

    /** Métricas de riego de los últimos {@code dias} días, o null si la maceta no existe. */
    ResumenRiego resumen(Integer idMaceta, int dias);
}
