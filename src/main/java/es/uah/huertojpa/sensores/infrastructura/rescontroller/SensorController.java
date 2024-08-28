package es.uah.huertojpa.sensores.infrastructura.rescontroller;

import es.uah.huertojpa.sensores.aplicacion.ISensorService;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

public class SensorController {
    @Autowired
    ISensorService service;

    @GetMapping("/sensor")
    public List<SensorDto> buscarTodos() {
        return service.buscarTodos();
    }

    @GetMapping("/sensor/{id}")
    public SensorDto buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorID(id);
    }
    @GetMapping("/sensor/userName/{userName}")
    public List<SensorDto>  buscarporUserName(@PathVariable("userName") String userName) {
        return service.buscarByUserName(userName);
    }

    @GetMapping("/sensor/{nombre}")
    public List<SensorDto>  buscarporNombre(@PathVariable("nombre") String nombre) {
        return service.buscarPorNombre(nombre);
    }
    @PostMapping(value= "/sensor")
    public String guardar(@RequestBody SensorDto sensor) {
        return String.valueOf(service.guardar(sensor));
    }

    @PutMapping(value= "/sensor")
    public boolean actualizar(@RequestBody SensorDto sensor) {
        return service.actualizar(sensor);
    }
    @PutMapping(value= "/sensorDepositoAgua")
    public boolean actualizarDepositoAgua(@RequestBody SensorDto sensor) {
        return service.actualizarDepositoAgua(sensor);
    }
    @DeleteMapping(value = "/sensor/{id}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String eliminar(@PathVariable("id") Integer id) {
        return String.valueOf(service.eliminar(id));
    }
}
