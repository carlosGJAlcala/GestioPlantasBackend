package es.uah.huertojpa.persona.aplicacion;

import com.google.common.hash.Hashing;
import es.uah.huertojpa.mensajería.IMediador;
import es.uah.huertojpa.persona.dominio.entidades.Persona;
import es.uah.huertojpa.persona.infrastructura.database.IPersonaDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class PersonaServiceImpl implements IPersonaService{
    @Autowired
    IPersonaDAO personaDAO;

    @Override
    public List<Persona> buscarTodas() {
        return personaDAO.buscarTodas();
    }

    @Override
    public Persona buscarPorId(Integer id) {
        return personaDAO.buscarPorId(id);
    }

    @Override
    public Persona buscarPorUserName(String userName) {
        return personaDAO.buscarPorUserName(userName);
    }

    @Autowired
    IMediador mediador;
    @Override
    public boolean guardarPersona(Persona persona) {
        if(personaDAO.buscarPorId(persona.getId())==null){
            String passSinHash =persona.getPasswordSHA256();
            String sha256hex = Hashing.sha256()
                    .hashString(passSinHash, StandardCharsets.UTF_8)
                    .toString();
            persona.setPasswordSHA256(sha256hex);
            personaDAO.guardarPersona(persona);
            return true;
        }
        return false;
    }

    @Override
    public boolean actualizarPersona(Persona persona) {
        if(personaDAO.buscarPorId(persona.getId())!=null){
            personaDAO.actualizarPersona(persona);
            return true;
        }
        return false;

    }

    @Override
    public boolean eliminarPersona(Integer idPersona) {
        if(personaDAO.buscarPorId(idPersona)!=null){
            personaDAO.eliminarPersona(idPersona);
            return true;
        }
        return false;
    }

    @Override
    public boolean borrarPorUserName(String userName) {
        if(this.buscarPorUserName(userName)!=null){
            personaDAO.eliminarPersona(this.buscarPorUserName(userName).getId());
            return true;
        }
        return false;
    }

    @Override
    public boolean mandarEmail(Integer idPersona) {
        Persona persona= this.buscarPorId(idPersona);
        String correo= persona.getCorreoElectronico();

        try {
            mediador.enviarMensaje(correo,"DEPOSITO DE AGUA","El deposito de agua esta vacio");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
