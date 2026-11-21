package es.uah.huertojpa.riego.infrastructura;

import es.uah.huertojpa.riego.aplicacion.OrdenRiegoDto;
import es.uah.huertojpa.riego.aplicacion.OrdenRiegoService;
import es.uah.huertojpa.seguridad.AccesoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** «Regar ahora» (backlog 3.3). */
@RestController
public class OrdenRiegoController {
    private final OrdenRiegoService service;
    private final AccesoService acceso;

    public OrdenRiegoController(OrdenRiegoService service, AccesoService acceso) {
        this.service = service;
        this.acceso = acceso;
    }

    /**
     * Pide un riego manual. 202 orden creada · 200 ya había una pendiente (la misma) · 429 riego manual reciente
     * (cabecera Retry-After) · 403 maceta ajena o inexistente.
     */
    @PostMapping("/maceta/{id}/riego")
    public ResponseEntity<?> solicitar(@PathVariable("id") Integer idMaceta, Authentication auth) {
        if (!acceso.puedeAccederMaceta(auth, idMaceta)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var s = service.solicitar(idMaceta, auth.getName());
        return switch (s.resultado()) {
            case CREADA -> ResponseEntity.status(HttpStatus.ACCEPTED).body(s.orden());
            case YA_PENDIENTE -> ResponseEntity.ok(s.orden());
            case EN_ESPERA -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", String.valueOf(s.segundosEspera()))
                    .body(Map.of("mensaje", "Se acaba de regar esta maceta; espera unos minutos",
                            "segundosEspera", s.segundosEspera()));
            case MACETA_NO_EXISTE -> ResponseEntity.notFound().build();
        };
    }

    /** Orden pendiente para mostrar en la web (204 si no hay). */
    @GetMapping("/maceta/{id}/riego/orden")
    public ResponseEntity<OrdenRiegoDto> pendiente(@PathVariable("id") Integer idMaceta, Authentication auth) {
        if (!acceso.puedeAccederMaceta(auth, idMaceta)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return service.pendiente(idMaceta).map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @DeleteMapping("/maceta/{id}/riego")
    public ResponseEntity<Void> cancelar(@PathVariable("id") Integer idMaceta, Authentication auth) {
        if (!acceso.puedeAccederMaceta(auth, idMaceta)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return service.cancelar(idMaceta) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Lo consulta el ESP32 una vez por minuto (X-Device-Key). 200 {@code {"regar":true,"id":..}} y la orden queda
     * ENTREGADA · 204 sin orden. Respuesta mínima para parsearla fácil en el firmware.
     */
    @GetMapping("/auth/maceta/{id}/orden")
    public ResponseEntity<Map<String, Object>> recoger(@PathVariable("id") Integer idMaceta) {
        return service.entregar(idMaceta)
                .<ResponseEntity<Map<String, Object>>>map(o -> ResponseEntity.ok(Map.of("regar", true, "id", o.id())))
                .orElse(ResponseEntity.noContent().build());
    }
}
