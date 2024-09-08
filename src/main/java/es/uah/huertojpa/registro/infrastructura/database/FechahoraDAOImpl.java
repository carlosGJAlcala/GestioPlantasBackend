package es.uah.huertojpa.registro.infrastructura.database;

import es.uah.huertojpa.registro.dominio.entities.Fechahora;
import es.uah.huertojpa.registro.dominio.entities.FechahoraHasSensor;
import es.uah.huertojpa.registro.dominio.repository.FechahoraHasSensorRepository;
import es.uah.huertojpa.registro.dominio.repository.FechahoraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public class FechahoraDAOImpl implements IFechahoraDAO{
    @Autowired
    FechahoraRepository repository;
    @Override
    public Fechahora buscarPorID(Integer id) {
        Optional<Fechahora> optional = repository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Override
    public List<Fechahora> buscarTodos() {
        List<Fechahora> resultado= repository.findAll();
        return resultado;    }

    @Override
    public Fechahora buscarPorFecha(String anyio, String mes, String dia, String hora,String minute) {
        Optional<Fechahora> optional = repository.findByAnyioAndMesAndDiaAndHoraAndMinuto(anyio,mes,dia,hora,minute);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Override
    public void guardar(Fechahora obj) {
        repository.save(obj);

    }

    @Override
    public void actualizar(Fechahora obj) {

    }

    @Override
    public void eliminar(Integer id) {
        repository.deleteById(id);

    }
}
