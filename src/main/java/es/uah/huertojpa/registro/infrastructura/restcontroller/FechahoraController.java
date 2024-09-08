package es.uah.huertojpa.registro.infrastructura.restcontroller;

import es.uah.huertojpa.registro.aplicacion.IFechahoraService;
import es.uah.huertojpa.registro.dominio.entities.Fechahora;
import es.uah.huertojpa.sensores.aplicacion.ISensorService;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController

public class FechahoraController {
    @Autowired
    IFechahoraService service;
    @GetMapping("/fechahora")
    public List<Fechahora> buscarTodos() {
        return service.buscarTodos();
    }


}
