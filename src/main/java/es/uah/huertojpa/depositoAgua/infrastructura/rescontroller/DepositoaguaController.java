package es.uah.huertojpa.depositoAgua.infrastructura.rescontroller;

import es.uah.huertojpa.depositoAgua.aplicacion.service.IDepositoAguaService;
import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DepositoaguaController {
    @Autowired
    IDepositoAguaService service;
    @GetMapping("/depositoagua")
    public List<DepositoaguaDto> buscarTodos() {
        return service.buscarTodos();
    }

    @GetMapping("/depositoagua/{id}")
    public DepositoaguaDto buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorID(id);
    }
    @GetMapping("/depositoagua/userName/{userName}")
    public List<DepositoaguaDto> buscarPorId(@PathVariable("userName") String userName) {
        return service.buscarPorUserName(userName);
    }
    @GetMapping("/depositoagua/huerto/{id}")
    public DepositoaguaDto buscarporIdHuerto(@PathVariable("id") Integer id) {
        return service.buscarporIdHuerto(id);
    }


    @PostMapping(value= "/depositoagua")
    public String guardar(@RequestBody DepositoaguaDto depositoaguaDto) {
        return String.valueOf(service.guardar(depositoaguaDto));
    }

    @PutMapping(value= "/depositoagua")
    public void actualizar(@RequestBody DepositoaguaDto depositoaguaDto) {
        service.actualizar(depositoaguaDto);
    }

    @DeleteMapping(value = "/depositoagua/{id}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String eliminar(@PathVariable("id") Integer id) {
        return String.valueOf(service.eliminar(id));
    }
}
