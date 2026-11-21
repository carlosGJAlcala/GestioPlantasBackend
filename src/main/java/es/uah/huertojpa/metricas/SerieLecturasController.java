package es.uah.huertojpa.metricas;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * W7 – {@code GET /lectura/maceta/{id}?magnitud=HUMEDAD&desde=2026-11-01T00:00:00&hasta=...&paso=15}.
 * El control de propiedad (la maceta tiene que ser del usuario) está en {@code SecurityConfig}.
 */
@RestController
public class SerieLecturasController {
    private final SerieLecturasService service;

    public SerieLecturasController(SerieLecturasService service) {
        this.service = service;
    }

    @GetMapping("/lectura/maceta/{id}")
    public SerieLecturas serie(@PathVariable("id") Integer idMaceta,
                               @RequestParam(defaultValue = "HUMEDAD") String magnitud,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
                               @RequestParam(name = "paso", required = false) Integer pasoMinutos) {
        return service.serie(idMaceta, magnitud, desde, hasta, pasoMinutos);
    }

    /**
     * A4 – {@code GET /lectura/deposito/huerto/{id}?desde=…&hasta=…&paso=…}: nivel del depósito del huerto (%).
     * 404 si el huerto no tiene depósito con ultrasonidos y altura. Propiedad comprobada en {@code SecurityConfig}.
     */
    @GetMapping("/lectura/deposito/huerto/{id}")
    public ResponseEntity<SerieLecturas> serieDeposito(@PathVariable("id") Integer idHuerto,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
                               @RequestParam(name = "paso", required = false) Integer pasoMinutos) {
        return ResponseEntity.of(service.serieDeposito(idHuerto, desde, hasta, pasoMinutos));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> peticionIncorrecta(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
