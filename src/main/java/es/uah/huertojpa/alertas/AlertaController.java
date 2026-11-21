package es.uah.huertojpa.alertas;

import es.uah.huertojpa.metricas.MetricasService;
import es.uah.huertojpa.persona.dominio.entidades.Persona;
import es.uah.huertojpa.seguridad.AccesoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AlertaController {
    private final AlertaService servicio;
    private final AccesoService acceso;
    private final MetricasService metricas;

    public AlertaController(AlertaService servicio, AccesoService acceso, MetricasService metricas) {
        this.servicio = servicio;
        this.acceso = acceso;
        this.metricas = metricas;
    }

    /** Alertas abiertas del usuario del token (de todas las macetas si es administrador). */
    @GetMapping("/alertas")
    public ResponseEntity<List<Alerta>> abiertas(Authentication auth) {
        Persona p = AccesoService.persona(auth);
        if (p == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(servicio.abiertas(acceso.esAdmin(auth) ? null : p.getId()));
    }

    /**
     * A3 – «Entendido»: deja de avisar por correo de esta alerta (sigue abierta y visible). Solo personas: la
     * placa no puede silenciar. La regla de {@code SecurityConfig} ya ha comprobado que la alerta es suya.
     */
    @PostMapping("/alertas/{id}/silenciar")
    public ResponseEntity<Alerta> silenciar(@PathVariable("id") Long id, Authentication auth) {
        if (AccesoService.persona(auth) == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return servicio.silenciar(id, metricas.ahora())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    /** Historial de una maceta: abiertas y cerradas en los últimos {@code dias} días (por defecto 7, máximo 90). */
    @GetMapping("/alertas/maceta/{id}")
    public ResponseEntity<List<Alerta>> historial(@PathVariable("id") Integer idMaceta,
                                                  @RequestParam(defaultValue = "7") int dias,
                                                  Authentication auth) {
        if (!acceso.puedeAccederMaceta(auth, idMaceta)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(servicio.historial(idMaceta, dias));
    }
}
