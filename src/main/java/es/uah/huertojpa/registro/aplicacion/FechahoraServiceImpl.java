package es.uah.huertojpa.registro.aplicacion;

import es.uah.huertojpa.registro.dominio.entities.Fechahora;
import es.uah.huertojpa.registro.infrastructura.database.IFechahoraDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class FechahoraServiceImpl implements IFechahoraService{
    @Autowired
    IFechahoraDAO dao;

    @Override
    public Fechahora buscarPorID(Integer id) {
        return dao.buscarPorID(id);
    }

    @Override
    public List<Fechahora> buscarTodos() {
        return dao.buscarTodos();
    }

    @Override
    public Fechahora buscarPorFecha(String anyio, String mes, String dia, String hora,String minute) {
        return dao.buscarPorFecha(anyio,mes,dia,hora, minute);
    }

    @Override
    public boolean guardar(Fechahora obj) {
            dao.guardar(obj);
            return true;
    }

    @Override
    public boolean actualizar(Fechahora obj) {
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
    public Fechahora getTiempoActual() {
        Date fechadate= new Date();
        String year,day,month,hour,minute;
        year=fechadate.getYear()+"";
        month=fechadate.getMonth()+"";
        day=fechadate.getDay()+"";
        hour=fechadate.getHours()+"";
        minute=fechadate.getMinutes()+"";
        if(buscarPorFecha(year,month,day,hour,minute)!=null){
            return buscarPorFecha(year,month,day,hour,minute);
        }else {
            Fechahora fechaactual=new Fechahora();
            fechaactual.setAnyio(year);
            fechaactual.setMes(month);
            fechaactual.setDia(day);
            fechaactual.setHora(hour);
            fechaactual.setMinuto(minute);
            this.guardar(fechaactual);
            return buscarPorFecha(year,month,day,hour,minute);
        }
    }
}
