package es.uah.huertojpa.huerto.infrastructura.restcontroller;

import es.uah.huertojpa.huerto.aplicacion.IHuertoHasUsuarioService;
import es.uah.huertojpa.huerto.dominio.entidades.HuertoHasUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HuertoHasUsuarioController {
    @Autowired
    IHuertoHasUsuarioService service;
    @GetMapping("/huertoHasUsuario")
    public List<HuertoHasUsuario> buscarTodos() {
        return service.buscarTodos();
    }

    @GetMapping("/huertoHasUsuario/{idHuerto}&{idUsuario}")
    public HuertoHasUsuario buscarPorId(@PathVariable("idUsuario") Integer idUsuario,@PathVariable("idHuerto") Integer idHuerto) {
        return service.buscarPorID(idHuerto,idUsuario);
    }
    @GetMapping("/huertoHasUsuario/idHuerto/{idHuerto}")
    public List<HuertoHasUsuario> buscarPorIdHuerto(@PathVariable("idHuerto") Integer idHuerto) {
        return service.buscarPorIDHuerto(idHuerto);
    }
    @GetMapping("/huertoHasUsuario/idUser/{idUser}")
    public List<HuertoHasUsuario> buscarPorIdUser(@PathVariable("idUser") Integer idUser) {
        return service.buscarPorIDUser(idUser);
    }
}

