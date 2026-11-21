package es.uah.huertojpa.huerto.aplicacion;

import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import es.uah.huertojpa.huerto.dominio.entidades.HuertoHasUsuario;
import es.uah.huertojpa.huerto.infrastructura.database.IHuertoDAO;
import es.uah.huertojpa.maceta.aplicacion.IMacetaService;
import es.uah.huertojpa.maceta.dominio.Humedad;
import es.uah.huertojpa.maceta.dominio.Maceta;
import es.uah.huertojpa.persona.aplicacion.IUsuarioService;
import es.uah.huertojpa.huerto.dominio.entidades.HuertoHasUsuarioId;
import es.uah.huertojpa.huerto.dominio.repository.HuertoHasUsuarioRepository;
import es.uah.huertojpa.huerto.dominio.repository.HuertoRepository;
import es.uah.huertojpa.persona.dominio.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HuertoServiceImpl implements IHuertoService {
    @Autowired
    IHuertoDAO dao;
    @Autowired
    IHuertoHasUsuarioService huertoHasUsuarioService;
    @Autowired
    IUsuarioService usuarioService;
    @Autowired
    HuertoRepository huertoRepository;
    @Autowired
    HuertoHasUsuarioRepository huertoHasUsuarioRepository;
    @Autowired
    UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public boolean guardarParaUsuario(Huerto obj, Integer idPersona) {
        if (obj == null || idPersona == null) {
            return false;
        }
        obj.setId(null); // V5: siempre un huerto nuevo con id generado; nunca se pisa uno existente
        var usuario = usuarioRepository.findById(idPersona);
        if (usuario.isEmpty()) {
            return false;
        }
        Huerto guardado = huertoRepository.save(obj);
        HuertoHasUsuarioId id = new HuertoHasUsuarioId();
        id.setHuertoIdhuerto(guardado.getId());
        id.setUsuarioPersonaId(idPersona);
        HuertoHasUsuario relacion = new HuertoHasUsuario();
        relacion.setId(id);
        relacion.setHuertoIdhuerto(guardado);
        relacion.setUsuarioPersona(usuario.get());
        huertoHasUsuarioRepository.save(relacion);
        return true;
    }


    @Override
    public Huerto buscarPorID(Integer id) {
        Huerto huerto = dao.buscarPorID(id);
        return huerto;
    }



    @Override
    public List<Huerto> buscarTodos() {
        return dao.buscarTodos();
    }

    @Override
    public boolean guardar(Huerto obj) {
        if (obj == null) {
            return false;
        }
        obj.setId(null); // V5: alta = huerto nuevo con id generado (el id del cuerpo se ignora)
        dao.guardar(obj);
        return true;
    }

    @Override
    public boolean actualizar(Huerto obj) {
        if(dao.buscarPorID(obj.getId())!=null){
            dao.actualizar(obj);

            return true;
        }
        return false;
    }

    @Override
    public boolean eliminar(Integer id) {
        if(dao.buscarPorID(id)!=null){
            dao.eliminar(id);

            return true;
        }
        return false;
    }

    @Override
    public List<Huerto> buscarHuertoPorUserId(String userName) {
        Integer idhuerto ;
        Huerto huerto= this.buscarPorID(1);
        Integer id =usuarioService.buscarPorUserName(userName).getId();

        List<HuertoHasUsuario> huertoHasUsuarios = huertoHasUsuarioService.buscarPorIDUser(id);
        List<Huerto> huertos =new ArrayList<>();
        for (HuertoHasUsuario huertoHasUsuario : huertoHasUsuarios) {
            idhuerto = huertoHasUsuario.getId().getHuertoIdhuerto();
            huerto = this.buscarPorID(idhuerto);

            huertos.add(huerto);
        }
        return huertos;
    }
}
