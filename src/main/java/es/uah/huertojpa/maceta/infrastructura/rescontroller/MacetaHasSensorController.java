package es.uah.huertojpa.maceta.infrastructura.rescontroller;

import es.uah.huertojpa.maceta.aplicacion.IMacetaHasSensorService;
import es.uah.huertojpa.maceta.dominio.MacetaHasSensor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MacetaHasSensorController {

    @Autowired
    IMacetaHasSensorService service;
    @GetMapping("/macetahassensor")
    public List<MacetaHasSensor> buscarTodos() {
        return service.buscarTodos();
    }

    @GetMapping("/macetahassensor/maceta/{id}")
    public List<MacetaHasSensor> buscarPorIDMaceta(@PathVariable("id") Integer id) {
        return service.buscarPorIDMaceta(id);
    }
    @GetMapping("/macetahassensor/sensor/{id}")
    public List<MacetaHasSensor> buscarPorIDSensor(@PathVariable("id") Integer id) {
        return service.buscarPorIDSensor(id);
    }


    @PostMapping(value= "/macetahassensor")
    public String guardar(@RequestBody MacetaHasSensor depositoaguaDto) {
   service.guardar(depositoaguaDto);
        return "";
    }



    @DeleteMapping(value = "/macetahassensor",produces = MediaType.TEXT_PLAIN_VALUE)
    public String eliminar(@RequestBody MacetaHasSensor macetaHasSensor ) {
        service.eliminar(macetaHasSensor);
    return "";
    }
}
