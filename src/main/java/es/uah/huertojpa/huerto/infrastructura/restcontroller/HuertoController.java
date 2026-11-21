package es.uah.huertojpa.huerto.infrastructura.restcontroller;

import es.uah.huertojpa.huerto.aplicacion.IHuertoService;
import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import es.uah.huertojpa.seguridad.AccesoService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
public class HuertoController {
    @Autowired
    IHuertoService service;
    @Autowired
    AccesoService acceso;

    @GetMapping("/huerto")
    public List<Huerto> buscarTodos() {
        return service.buscarTodos();
    }

    @GetMapping("/huerto/{id}")
    public Huerto buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorID(id);
    }


    @PostMapping(value="/huerto")
    public String guardar(@RequestBody Huerto huerto, Authentication auth) {
        // S4b: un usuario que crea un huerto queda asociado a él (antes se creaba «huérfano» y no lo veía nunca).
        if (acceso.esAdmin(auth)) {
            return String.valueOf(service.guardar(huerto));
        }
        var persona = AccesoService.persona(auth);
        AccesoService.exigir(persona != null);
        return String.valueOf(service.guardarParaUsuario(huerto, persona.getId()));
    }
    @GetMapping(value="/huerto/userName/{userName}")
    public List<Huerto> buscarHuertoPorUserId(@PathVariable("userName") String userName) {
        return service.buscarHuertoPorUserId(userName);
    }
    @PutMapping(value="/huerto")
    public void actualizar(@RequestBody Huerto huerto, Authentication auth) {
        AccesoService.exigir(acceso.puedeAccederHuerto(auth, huerto.getId()));
        service.actualizar(huerto);
    }

    @DeleteMapping(value = "/huerto/{id}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String eliminar(@PathVariable("id") Integer id) {
        return String.valueOf(service.eliminar(id));
    }
}
