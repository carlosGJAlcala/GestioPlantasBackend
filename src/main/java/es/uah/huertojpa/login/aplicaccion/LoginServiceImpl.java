package es.uah.huertojpa.login.aplicaccion;

import es.uah.huertojpa.persona.aplicacion.ILoginService;
import es.uah.huertojpa.persona.dominio.entidades.Administrador;
import es.uah.huertojpa.persona.dominio.entidades.Login;
import es.uah.huertojpa.persona.dominio.entidades.LoginResp;
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
    @Override
    public LoginResp auth(Login login) {
        LoginResp respuesta = new LoginResp();
        if(personaDAO.buscarPorUserNameYPassword(login.getUserName(),login.getPassword())!=null){
            Persona persona=personaDAO.buscarPorUserNameYPassword(login.getUserName(),login.getPassword());
            respuesta.setIdUser(persona.getId().toString());
            respuesta.setUserName(persona.getUsuario());
            respuesta.setResultado("Autentificiacion correcta");
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
