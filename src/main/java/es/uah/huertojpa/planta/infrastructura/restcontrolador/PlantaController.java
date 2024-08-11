package es.uah.huertojpa.planta.infrastructura.restcontrolador;

import es.uah.huertojpa.planta.aplicacion.dto.EstadoPlanta;
import es.uah.huertojpa.planta.aplicacion.dto.PlantaDto;
import es.uah.huertojpa.planta.aplicacion.services.IPlantaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PlantaController {
    @Autowired
    IPlantaService plantaService;

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


    @PostMapping(value="/planta")
    public String guardarTipoPlanta(@RequestBody PlantaDto planta) {

        return String.valueOf(plantaService.guardarPlanta(planta));
    }

    @PutMapping(value="/planta")
    public void actualizarTipoPlanta(@RequestBody PlantaDto planta) {

        plantaService.actualizarPlanta(planta);
    }

    @DeleteMapping(value = "/planta/{id}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String eliminarTipoPlantao(@PathVariable("id") Integer id) {
        return String.valueOf(plantaService.eliminarPlanta(id));
    }

}
