package es.uah.huertojpa.riego.aplicacion;

import es.uah.huertojpa.riego.dominio.EstadoOrden;
import es.uah.huertojpa.riego.dominio.OrdenRiego;

import java.time.LocalDateTime;

public record OrdenRiegoDto(Long id, Integer idMaceta, String solicitante, LocalDateTime creada,
                            LocalDateTime entregada, EstadoOrden estado, LocalDateTime caduca) {
    static OrdenRiegoDto de(OrdenRiego o, long minutosCaducidad) {
        return new OrdenRiegoDto(o.getId(), o.getMacetaId(), o.getSolicitante(), o.getCreada(), o.getEntregada(),
                o.getEstado(), o.getCreada().plusMinutes(minutosCaducidad));
    }
}
