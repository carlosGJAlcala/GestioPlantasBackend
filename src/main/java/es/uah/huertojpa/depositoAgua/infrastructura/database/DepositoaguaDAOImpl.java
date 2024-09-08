package es.uah.huertojpa.depositoAgua.infrastructura.database;

import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;
import es.uah.huertojpa.depositoAgua.aplicacion.mapper.DepositoaguaMapper;
import es.uah.huertojpa.depositoAgua.dominio.DepositoAgua;
import es.uah.huertojpa.depositoAgua.dominio.DepositoaguaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class DepositoaguaDAOImpl implements IDepositoaguaDAO {
    @Autowired
    DepositoaguaRepository repository;
    @Autowired
    DepositoaguaMapper depositoaguaMapper;
    @Override
    public DepositoaguaDto buscarPorID(Integer id) {

        Optional<DepositoAgua> optional = repository.findById(id);
        if (optional.isPresent()) {
            return depositoaguaMapper.toDto(optional.get());
        }
        return null;

    }

    @Override
    public DepositoaguaDto buscarPorIdHuerto(Integer id) {
        Optional<DepositoAgua> optional = repository.findByHuertoIdhuerto(id);
        if (optional.isPresent()) {
            return depositoaguaMapper.toDto(optional.get());
        }
        return null;
    }

    @Override
    public DepositoaguaDto buscarPorIDSensor(Integer id) {
        Optional<DepositoAgua> optional = repository.findBySensorIdsensor_Id(id);
        if (optional.isPresent()) {
            return depositoaguaMapper.toDto(optional.get());
        }
        return null;
    }

    @Override
    public List<DepositoaguaDto> buscarTodos() {
        List<DepositoAgua> depositoaguas = repository.findAll();
        return depositoaguas.stream().map(depositoaguaMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void guardar(DepositoaguaDto obj) {

        repository.save(depositoaguaMapper.toEntity(obj));

    }

    @Override
    public void actualizar(DepositoaguaDto obj) {
        repository.updateEstadoById(obj.getEstado(),obj.getId());
    }

    @Override
    public void eliminar(Integer id) {
        repository.deleteById(id);
    }
}
