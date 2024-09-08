package es.uah.huertojpa.huerto.infrastructura.restcontroller;

import es.uah.huertojpa.huerto.aplicacion.IHuertoService;
import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
public class HuertoController {
    @Autowired
    IHuertoService service;

    @GetMapping("/huerto")
    public List<Huerto> buscarTodos() {
        return service.buscarTodos();
    }

    @GetMapping("/huerto/{id}")
    public Huerto buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorID(id);
    }


    @PostMapping(value="/huerto")
    public String guardar(@RequestBody Huerto huerto) {
        return String.valueOf(service.guardar(huerto));
    }
    @GetMapping(value="/huerto/userName/{userName}")
    public List<Huerto> buscarHuertoPorUserId(@PathVariable("userName") String userName) {
        return service.buscarHuertoPorUserId(userName);
    }
    @PutMapping(value="/huerto")
    public void actualizar(@RequestBody Huerto huerto) {
        service.actualizar(huerto);
    }

    @DeleteMapping(value = "/huerto/{id}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String eliminar(@PathVariable("id") Integer id) {
        return String.valueOf(service.eliminar(id));
    }
}
