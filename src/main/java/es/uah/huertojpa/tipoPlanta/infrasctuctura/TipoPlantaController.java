package es.uah.huertojpa.tipoPlanta.infrasctuctura;

import es.uah.huertojpa.tipoPlanta.aplicacion.ITipoPlantaService;
import es.uah.huertojpa.tipoPlanta.dominio.Tipoplanta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TipoPlantaController {
    @Autowired
    ITipoPlantaService tipoPlantaService;

    @GetMapping("/tipoPlanta")
    public List<Tipoplanta> buscarTodos() {
        return tipoPlantaService.buscarTodos();
    }

    @GetMapping("/tipoPlanta/{id}")
    public Tipoplanta buscarPorId(@PathVariable("id") Integer id) {
        return tipoPlantaService.buscarPorId(id);
    }

    @GetMapping("/tipoPlanta/humedad/minimo/{nivel}")
    public List<Tipoplanta> buscarTiposPlantasPorNivelHumedadMinimo(@PathVariable("nivel") Float f) {
        return tipoPlantaService.buscarTiposPlantasPorNivelHumedadMinimo(f);
    }

    @GetMapping("/tipoPlanta/temperatura/minimo/{nivel}")
    public List<Tipoplanta> buscarTiposPlantasPorNivelTempMinimo(@PathVariable("nivel") Float f) {
        return tipoPlantaService.buscarTiposPlantasPorNivelTempMinimo(f);
    }

    @GetMapping("/tipoPlanta/nitrogeno/minimo/{nivel}")
    public List<Tipoplanta> buscarTiposPlantasPorNivelNitrogenoMinimo(@PathVariable("nivel") Float f){
        return tipoPlantaService.buscarTiposPlantasPorNivelNitrogenoMinimo(f);
    }

    @GetMapping("/tipoPlanta/nombre/{nombre}")
    public Tipoplanta buscarPorNombre(@PathVariable("nombre") String nombre) {
        return tipoPlantaService.buscarPorNombre(nombre);
    }

    @PostMapping(value="/tipoPlanta")
    public String guardarTipoPlanta(@RequestBody Tipoplanta tipoPlanta) {
        return String.valueOf(tipoPlantaService.guardarTipoPlanta(tipoPlanta));
    }

    @PutMapping(value="/tipoPlanta")
    public void actualizarTipoPlanta(@RequestBody Tipoplanta tipoPlanta) {
        tipoPlantaService.actualizarTipoPlanta(tipoPlanta);
    }

    @DeleteMapping(value = "/tipoPlanta/{id}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String eliminarTipoPlantao(@PathVariable("id") Integer id) {
        return String.valueOf(tipoPlantaService.eliminarTipoPlanta(id));
    }
    @DeleteMapping(value = "/tipoPlanta?nombre={nombre}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String eliminarTipoPlantaoNombre(@PathVariable("nombre") String nombre) {
        return String.valueOf(tipoPlantaService.eliminarTipoPlantaPorNombre(nombre));
    }
}
