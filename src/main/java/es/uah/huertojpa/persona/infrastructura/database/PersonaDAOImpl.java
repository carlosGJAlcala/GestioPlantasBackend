package es.uah.huertojpa.persona.infrastructura.database;

import es.uah.huertojpa.persona.dominio.entidades.Persona;
import es.uah.huertojpa.persona.dominio.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PersonaDAOImpl implements IPersonaDAO{

    @Autowired
    PersonaRepository personaRepository;
    @Override
    public List<Persona> buscarTodas() {
        return personaRepository.findAll();
    }

    @Override
    public Persona buscarPorId(Integer id) {
        Optional<Persona> optional = personaRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Override
    public Persona buscarPorUserName(String userName) {
        Optional<Persona> optional = personaRepository.findByUsuario(userName);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Override
    public void guardarPersona(Persona persona) {
        personaRepository.save(persona);
    }

    @Override
    public void actualizarPersona(Persona persona) {
        personaRepository.actualizarFicha(persona.getNombre(), persona.getApellido(), persona.getUsuario(),
                persona.getPasswordSHA256(), persona.getDni(), persona.getCorreoElectronico(),
                persona.getAvisosCorreo(), persona.getId());
    }

    @Override
    public void eliminarPersona(Integer id) {
        personaRepository.deleteById(id);
    }

    @Override
    public void actualizarContrasena(Integer id, String hash) {
        personaRepository.actualizarContrasena(id, hash);
    }

    @Override
    public void ponerContrasena(Integer id, String hash, boolean debeCambiar, LocalDateTime desde) {
        personaRepository.ponerContrasena(id, hash, debeCambiar, desde);
    }

    @Override
    public void cerrarSesiones(Integer id, LocalDateTime desde) {
        personaRepository.cerrarSesiones(id, desde);
    }

    @Override
    public void registrarAcceso(Integer id, LocalDateTime cuando) {
        personaRepository.registrarAcceso(id, cuando);
    }
}
