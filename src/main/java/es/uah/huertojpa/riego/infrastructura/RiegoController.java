package es.uah.huertojpa.riego.infrastructura;

import es.uah.huertojpa.riego.aplicacion.IRiegoService;
import es.uah.huertojpa.riego.aplicacion.ResumenRiego;
import es.uah.huertojpa.riego.aplicacion.RiegoDto;
import es.uah.huertojpa.riego.aplicacion.RiegoRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class RiegoController {
    private final IRiegoService service;

    public RiegoController(IRiegoService service) {
        this.service = service;
    }

    /**
     * Lo llama el ESP32 al terminar cada ciclo de riego. Está bajo /auth/** como el resto de endpoints del
     * dispositivo hasta que se implemente la API key por dispositivo (backlog S3).
     */
    @PostMapping("/auth/riego")
    public ResponseEntity<RiegoDto> registrar(@RequestBody RiegoRequest request) {
        RiegoDto dto = service.registrar(request);
        return dto == null ? ResponseEntity.badRequest().build() : ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /** Historial de riegos. Sin parámetros: últimos 7 días. Fechas ISO: 2026-11-04T00:00:00 */
    @GetMapping("/riego/maceta/{id}")
    public List<RiegoDto> historial(@PathVariable("id") Integer idMaceta,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return service.buscarPorMaceta(idMaceta, desde, hasta);
    }

    @GetMapping("/riego/maceta/{id}/resumen")
    public ResponseEntity<ResumenRiego> resumen(@PathVariable("id") Integer idMaceta,
                                                @RequestParam(defaultValue = "7") int dias) {
        ResumenRiego resumen = service.resumen(idMaceta, dias);
        return resumen == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(resumen);
    }
}
