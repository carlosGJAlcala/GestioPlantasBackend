package es.uah.huertojpa.maceta.infrastructura.restcontroller;

import es.uah.huertojpa.maceta.aplicacion.IMacetaService;
import es.uah.huertojpa.maceta.dominio.Humedad;
import es.uah.huertojpa.maceta.dominio.Maceta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

public class MacetaController {
    @Autowired
    IMacetaService service;
    @GetMapping("/maceta")
    public List<Maceta> buscarTodasMacetas() {
        return service.buscarTodos();
    }

    @GetMapping("/maceta/{id}")
    public Maceta buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorID(id);
    }
    @GetMapping("/maceta/huerto/{id}")
    public List<Maceta> buscarPorIdMaceta(@PathVariable("id") Integer id) {
        return service.buscarPorIDHuerto(id);
    }
    @GetMapping("/maceta/userName/{userName}")
    public List<Maceta> buscarPorUserName(@PathVariable("userName") String userName) {
        return service.buscarPorUserName(userName);
    }

    @GetMapping("/auth/maceta/huerto/humedades/{id}")
    public List<Humedad> getAllHumedades(@PathVariable("id") Integer id) {
        return service.getAllHumdedadeByHuerto(id);
    }
    @GetMapping("/auth/maceta/huerto/humedad/{id}")
    public Humedad getAllHumedad(@PathVariable("id") Integer id) {
        return service.conseguirDatosHumedad(id);
    }


    @PostMapping(value= "/maceta")
    public String guardarMaceta(@RequestBody Maceta maceta) {
        return String.valueOf(service.guardar(maceta));
    }

    @PutMapping(value= "/maceta")
    public String actualizarMaceta(@RequestBody Maceta maceta) {
        return String.valueOf(service.actualizar(maceta));
    }

    @DeleteMapping(value = "/maceta/{id}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String eliminarMaceta(@PathVariable("id") Integer id) {
        return String.valueOf(service.eliminar(id));
    }
}
