package es.uah.huertojpa.persona.infrastructura.restcontrolador;

import es.uah.huertojpa.persona.aplicacion.IAdministradorService;
import es.uah.huertojpa.persona.aplicacion.IPersonaService;
import es.uah.huertojpa.persona.dominio.entidades.Administrador;
import es.uah.huertojpa.persona.dominio.entidades.Persona;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AdministradorController {

    @Autowired
    IAdministradorService administradorService;


    @GetMapping("/administrador")
    public List<Administrador> buscarTodos() {
        return administradorService.buscarTodas();
    }
    @GetMapping("/administrador/{id}")
    public Administrador buscarPorId(@PathVariable("id") Integer id) {
        return administradorService.buscarPorId(id);
    }

    @PostMapping(value="/administrador")
    public String guardarPersona(@RequestBody Administrador persona) {
        return String.valueOf(administradorService.guardar(persona));
    }
    @PutMapping(value="/administrador")
    public String actualizarTipoPersona(@RequestBody Administrador persona) {
        return String.valueOf( administradorService.actualizar(persona));
    }

    @DeleteMapping(value = "/administrador/{id}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String eliminarTipoPersona(@PathVariable("id") Integer id) {
        return String.valueOf(administradorService.eliminar(id));
    }
}
