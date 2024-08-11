package es.uah.huertojpa.huerto.infrastructura.rescontroller;

import es.uah.huertojpa.huerto.aplicacion.IHuertoHasUsuarioService;
import es.uah.huertojpa.huerto.dominio.entidades.HuertoHasUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsuarioHasUsuarioController {
    @Autowired
    IHuertoHasUsuarioService service;
    @GetMapping("/UsuarioHasUsuario/{idHuerto}&{idUsuario}")
    public HuertoHasUsuario buscarPorId(@PathVariable("idUsuario") Integer idUsuario,@PathVariable("idHuerto") Integer idHuerto) {
        return service.buscarPorID(idHuerto,idUsuario);
    }

}
