package es.uah.huertojpa.persona.infrastructura.restcontrolador;

import es.uah.huertojpa.persona.aplicacion.IUsuarioService;
import es.uah.huertojpa.persona.dominio.entidades.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

public class UsuarioController {
    @Autowired
    IUsuarioService usuarioService;
    @GetMapping("/usuario")
    public List<Usuario> buscarTodos() {
        return usuarioService.buscarTodas();
    }
    @GetMapping("/usuario/{id}")
    public Usuario buscarPorId(@PathVariable("id") Integer id) {
        return usuarioService.buscarPorId(id);
    }

    @PostMapping(value="/usuario")
    public String guardarUsuario(@RequestBody Usuario usuario) {
        return String.valueOf(usuarioService.guardarUsuario(usuario));
    }
    @PutMapping(value="/usuario")
    public String actualizarUsuario(@RequestBody Usuario usuario) {
        return String.valueOf( usuarioService.actualizarUsuario(usuario));
    }

    @DeleteMapping(value = "/usuario/{id}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String eliminarUsuario(@PathVariable("id") Integer id) {
        return String.valueOf(usuarioService.eliminarUsuario(id));
    }


}
