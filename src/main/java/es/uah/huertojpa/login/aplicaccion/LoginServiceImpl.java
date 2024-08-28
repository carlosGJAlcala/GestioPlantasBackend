package es.uah.huertojpa.login.aplicaccion;

import es.uah.huertojpa.jwt.JwtService;
import es.uah.huertojpa.persona.dominio.entidades.Administrador;
import es.uah.huertojpa.login.dominio.LoginRequest;
import es.uah.huertojpa.login.dominio.LoginResponse;
import es.uah.huertojpa.persona.dominio.entidades.Persona;
import es.uah.huertojpa.persona.infrastructura.database.IAdministradorDAO;
import es.uah.huertojpa.persona.infrastructura.database.IPersonaDAO;
import es.uah.huertojpa.persona.infrastructura.database.IUsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements ILoginService {
    @Autowired
    IPersonaDAO personaDAO;
    @Autowired
    IAdministradorDAO administradorDAO;
    @Autowired
    IUsuarioDAO usuarioDAO;
    @Autowired
    JwtService jwtService;
    @Override
    public LoginResponse auth(LoginRequest login) {
        LoginResponse respuesta = new LoginResponse();
        if(personaDAO.buscarPorUserNameYPassword(login.getUserName(),login.getPassword())!=null){
            Persona persona=personaDAO.buscarPorUserNameYPassword(login.getUserName(),login.getPassword());
            respuesta.setIdUser(persona.getId().toString());
            respuesta.setUserName(persona.getUsuario());
            respuesta.setResultado("Autentificiacion correcta");
            respuesta.setToken(jwtService.getToken(persona));
            if(isAdmind(persona.getId())){
               Administrador admin= administradorDAO.buscarPorId(persona.getId());
                respuesta.setPermisoAdmin("1");
            }else {
                respuesta.setPermisoAdmin("0");
            }
        }else {
            respuesta.setIdUser("-1");
            respuesta.setPermisoAdmin("-1");
            respuesta.setResultado("Autentificiacion incorrecta");
        }

        return respuesta;
    }

    @Override
    public boolean isAdmind(Integer id) {
        if(administradorDAO.buscarPorId(id)!=null){
            return true ;
        }

        return false;
    }
}
