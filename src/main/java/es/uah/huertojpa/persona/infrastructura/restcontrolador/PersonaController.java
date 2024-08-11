package es.uah.huertojpa.persona.infrastructura.restcontrolador;

import es.uah.huertojpa.persona.aplicacion.IPersonaService;
import es.uah.huertojpa.persona.aplicacion.IUsuarioService;
import es.uah.huertojpa.persona.dominio.entidades.Persona;
import es.uah.huertojpa.persona.dominio.entidades.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
public class PersonaController {

    @Autowired
    IPersonaService personaService;
    @GetMapping("/persona")
    public List<Persona> buscarTodos() {
        return personaService.buscarTodas();
    }
    @GetMapping("/persona/{id}")
    public Persona buscarPorId(@PathVariable("id") Integer id) {
        return personaService.buscarPorId(id);
    }
    @GetMapping("/persona/userName/{userName}")
    public Persona buscarPorUserName(@PathVariable("userName") String userName) {
        return personaService.buscarPorUserName(userName);
    }


    @PostMapping(value="/persona")
    public String guardarPersona(@RequestBody Persona persona) {
        return String.valueOf(personaService.guardarPersona(persona));
    }
    @PutMapping(value="/persona")
    public String actualizarTipoPersona(@RequestBody Persona persona) {
        return String.valueOf( personaService.actualizarPersona(persona));
    }

    @DeleteMapping(value = "/persona/{id}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String eliminarTipoPersona(@PathVariable("id") Integer id) {
        return String.valueOf(personaService.eliminarPersona(id));
    }
    @DeleteMapping(value = "/persona/userName/{userName}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String borrarPorUserName(@PathVariable("userName") String userName) {
        return String.valueOf(personaService.borrarPorUserName(userName));
    }
}
