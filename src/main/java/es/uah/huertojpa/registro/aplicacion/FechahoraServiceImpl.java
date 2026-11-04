package es.uah.huertojpa.registro.aplicacion;

import java.time.LocalDateTime;
import java.time.ZoneId;

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
        // Antes se usaba java.util.Date#getYear/getMonth/getDay: año-1900, mes 0-11 y getDay() = DÍA DE LA SEMANA.
        // Eso hacía que la misma franja (p.ej. martes 10:05) colisionara cada semana y se descartaran lecturas.
        LocalDateTime ahora = LocalDateTime.now(ZoneId.of("Europe/Madrid"));
        String year,day,month,hour,minute;
        year=String.valueOf(ahora.getYear());
        month=String.valueOf(ahora.getMonthValue());
        day=String.valueOf(ahora.getDayOfMonth());
        hour=String.valueOf(ahora.getHour());
        minute=String.valueOf(ahora.getMinute());
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
