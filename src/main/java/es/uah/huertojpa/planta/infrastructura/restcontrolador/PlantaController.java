package es.uah.huertojpa.planta.infrastructura.restcontrolador;

import es.uah.huertojpa.planta.aplicacion.dto.EstadoPlanta;
import es.uah.huertojpa.planta.aplicacion.dto.PlantaDto;
import es.uah.huertojpa.planta.aplicacion.services.IPlantaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import es.uah.huertojpa.tipoPlanta.infrasctuctura.ITipoPlantaDAO;
import es.uah.huertojpa.seguridad.AccesoService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class PlantaController {
    @Autowired
    IPlantaService plantaService;
    @Autowired
    AccesoService acceso;
    @Autowired
    ITipoPlantaDAO tipoPlantaDAO;

    /**
     * W13 (2026-11-21): datos que la BD no comprueba o que daban 500. Sin nombre la tarjeta no dice nada; un tipo de
     * planta inexistente violaba la FK (500) y sin tipo el riego no tiene umbrales; una fecha futura no tiene
     * sentido. Devuelve el texto del 400 o {@code null} si la planta es válida.
     */
    String motivoNoValida(PlantaDto planta) {
        if (planta.getNombrePlanta() == null || planta.getNombrePlanta().isBlank()) return "La planta necesita un nombre.";
        if (planta.getNombrePlanta().length() > 45) return "El nombre de la planta no puede pasar de 45 caracteres.";
        if (planta.getTipoplantaIdtipoplanta() == null || tipoPlantaDAO.buscarPorId(planta.getTipoplantaIdtipoplanta()) == null) {
            return "Elige un tipo de planta de la biblioteca.";
        }
        if (planta.getFechaPlantacion() != null && planta.getFechaPlantacion().isAfter(LocalDate.now())) {
            return "La fecha de plantación no puede ser futura.";
        }
        return null;
    }

    @GetMapping("/planta")
    public List<PlantaDto> buscarTodos() {
        return plantaService.buscarTodas();
    }

    @GetMapping("/planta/{id}")
    public PlantaDto buscarPorId(@PathVariable("id") Integer id) {
        return plantaService.buscarPorId(id);
    }
    @GetMapping("/planta/userName/{userName}")
    public  List<PlantaDto> buscarPorUserName(@PathVariable("userName") String userName) {
        return plantaService.buscarPorUserName(userName);
    }
    @GetMapping("/planta/macetaid/{macetaid}")
    public  List<PlantaDto> buscarPorMacetaId(@PathVariable("macetaid") Integer macetaid) {
        return plantaService.buscarPorMacetaID(macetaid);
    }
    @GetMapping("/planta/estado/{estado}")
    public List<PlantaDto> buscarPorEstado(@PathVariable("estado") String estado) {
        return plantaService.buscarPorEstado(EstadoPlanta.valueOf(estado));
    }


    @PostMapping(value="/planta", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> guardarTipoPlanta(@RequestBody PlantaDto planta, Authentication auth) {
        AccesoService.exigir(acceso.puedeAccederMaceta(auth, planta.getMacetaIdmaceta())); // S4b
        String motivo = motivoNoValida(planta);
        if (motivo != null) return ResponseEntity.badRequest().body(motivo);

        return ResponseEntity.ok(String.valueOf(plantaService.guardarPlanta(planta)));
    }

    /** W13: ahora sí guarda (antes no hacía nada) y responde «true»/«false» como el resto de altas y ediciones. */
    @PutMapping(value="/planta", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> actualizarTipoPlanta(@RequestBody PlantaDto planta, Authentication auth) {
        AccesoService.exigir(acceso.puedeAccederPlanta(auth, planta.getId())
                && acceso.puedeAccederMaceta(auth, planta.getMacetaIdmaceta())); // S4b
        String motivo = motivoNoValida(planta);
        if (motivo != null) return ResponseEntity.badRequest().body(motivo);

        return ResponseEntity.ok(String.valueOf(plantaService.actualizarPlanta(planta)));
    }

    @DeleteMapping(value = "/planta/{id}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String eliminarTipoPlantao(@PathVariable("id") Integer id) {
        return String.valueOf(plantaService.eliminarPlanta(id));
    }

}
