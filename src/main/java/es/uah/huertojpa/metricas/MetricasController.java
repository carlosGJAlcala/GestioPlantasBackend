package es.uah.huertojpa.metricas;

import es.uah.huertojpa.persona.dominio.entidades.Persona;
import es.uah.huertojpa.seguridad.AccesoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetricasController {
    private final MetricasService service;
    private final PanelService panel;
    private final AccesoService acceso;

    public MetricasController(MetricasService service, PanelService panel, AccesoService acceso) {
        this.service = service;
        this.panel = panel;
        this.acceso = acceso;
    }

    /**
     * W9 – Panel del usuario del token en una sola petición: sus macetas con métricas y estado, y sus alertas
     * abiertas (todas si es administrador). Solo personas: la placa no tiene panel.
     */
    @GetMapping("/metricas/panel")
    public ResponseEntity<PanelUsuario> panel(@RequestParam(defaultValue = "24") int horas, Authentication auth) {
        Persona p = AccesoService.persona(auth);
        if (p == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(panel.panel(acceso.esAdmin(auth) ? null : p.getId(), horas));
    }

    /** Métricas de las últimas {@code horas} horas (por defecto 7 días, máximo 90). */
    @GetMapping("/metricas/maceta/{id}")
    public ResponseEntity<MetricasMaceta> metricas(@PathVariable("id") Integer idMaceta,
                                                   @RequestParam(defaultValue = "168") int horas,
                                                   Authentication auth) {
        if (!acceso.puedeAccederMaceta(auth, idMaceta)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(service.calcular(idMaceta, horas));
    }
}
