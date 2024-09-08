package es.uah.huertojpa.registro.infrastructura.restcontroller;

import es.uah.huertojpa.registro.aplicacion.IFechaHoraHasSensorService;
import es.uah.huertojpa.registro.dominio.entities.FechahoraHasSensor;
import es.uah.huertojpa.registro.dominio.entities.FechahoraHasSensorId;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

public class FechahoraHasSensorController {
    @Autowired
    IFechaHoraHasSensorService service;
    @GetMapping("/auth/fechahorahassensor")
    public List<FechahoraHasSensor> buscarTodos() {
        return service.buscarTodos();
    }

    @GetMapping("/auth/fechahorahassensor/FechaHora_idFecha={FechaHora_idFecha}&sensor_idSensor={sensor_idSensor}")
    public FechahoraHasSensor buscarPorId(@PathVariable("FechaHora_idFecha") Integer FechaHora_idFecha,@PathVariable("sensor_idSensor") Integer sensor_idSensor) {
        FechahoraHasSensorId fechahoraHasSensorId=new FechahoraHasSensorId();
        fechahoraHasSensorId.setSensorIdsensor(sensor_idSensor);
        fechahoraHasSensorId.setFechahoraIdfecha(FechaHora_idFecha);
        return service.buscarPorID(fechahoraHasSensorId);
    }
    @PostMapping(value= "/fechahorahassensor")
    public String guardar(@RequestBody FechahoraHasSensor fechahoraHasSensor) {
        return String.valueOf(service.guardar(fechahoraHasSensor));
    }

    @PutMapping(value= "/auth/fechahorahassensor")
    public boolean actualizar(@RequestBody FechahoraHasSensor fechahoraHasSensor) {
        return service.actualizar(fechahoraHasSensor);
    }
}
