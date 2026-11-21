package es.uah.huertojpa.riego.aplicacion;

import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;
import es.uah.huertojpa.depositoAgua.infrastructura.database.IDepositoaguaDAO;
import es.uah.huertojpa.maceta.dominio.Maceta;
import es.uah.huertojpa.maceta.infrastructura.database.IMacetaDAO;
import es.uah.huertojpa.riego.dominio.MotivoRiego;
import es.uah.huertojpa.riego.dominio.ResultadoRiego;
import es.uah.huertojpa.riego.dominio.Riego;
import es.uah.huertojpa.riego.dominio.RiegoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class RiegoServiceImpl implements IRiegoService {
    static final ZoneId ZONA = ZoneId.of("Europe/Madrid");

    private final RiegoRepository repository;
    private final IMacetaDAO macetaDAO;
    private final IDepositoaguaDAO depositoDAO;
    private final Clock clock;

    @Autowired // hay dos constructores (el otro, con Clock, es para los tests): sin esto Spring no arranca
    public RiegoServiceImpl(RiegoRepository repository, IMacetaDAO macetaDAO, IDepositoaguaDAO depositoDAO) {
        this(repository, macetaDAO, depositoDAO, Clock.system(ZONA));
    }

    RiegoServiceImpl(RiegoRepository repository, IMacetaDAO macetaDAO, IDepositoaguaDAO depositoDAO, Clock clock) {
        this.repository = repository;
        this.macetaDAO = macetaDAO;
        this.depositoDAO = depositoDAO;
        this.clock = clock;
    }

    @Override
    public RiegoDto registrar(RiegoRequest req) {
        if (req == null || req.idMaceta() == null || macetaDAO.buscarPorID(req.idMaceta()) == null) {
            return null;
        }
        int duracion = req.duracionMs() == null ? 0 : Math.max(0, req.duracionMs());
        Riego r = new Riego();
        r.setMacetaId(req.idMaceta());
        // El ESP32 no tiene reloj: se toma la hora de recepción menos lo que duró el ciclo.
        r.setTs(LocalDateTime.now(clock).minusNanos(duracion * 1_000_000L));
        r.setDuracionMs(duracion);
        r.setPulsos(req.pulsos() == null ? 0 : Math.max(0, req.pulsos()));
        r.setHumedadAntes(req.humedadAntes());
        r.setHumedadDespues(req.humedadDespues());
        r.setNivelDepositoAntes(req.nivelDepositoAntes());
        r.setNivelDepositoDespues(req.nivelDepositoDespues());
        r.setMotivo(req.motivo() == null ? MotivoRiego.AUTO : req.motivo());
        r.setResultado(req.resultado() == null ? ResultadoRiego.OK : req.resultado());
        return RiegoDto.de(repository.save(r));
    }

    @Override
    public List<RiegoDto> buscarPorMaceta(Integer idMaceta, LocalDateTime desde, LocalDateTime hasta) {
        LocalDateTime h = hasta == null ? LocalDateTime.now(clock) : hasta;
        LocalDateTime d = desde == null ? h.minusDays(7) : desde;
        return repository.findByMacetaIdAndTsBetweenOrderByTsAsc(idMaceta, d, h).stream().map(RiegoDto::de).toList();
    }

    @Override
    public ResumenRiego resumen(Integer idMaceta, int dias) {
        Maceta maceta = macetaDAO.buscarPorID(idMaceta);
        if (maceta == null) {
            return null;
        }
        int ventana = Math.max(1, Math.min(dias, 365));
        LocalDateTime ahora = LocalDateTime.now(clock);
        List<Riego> riegos = repository.findByMacetaIdAndTsBetweenOrderByTsAsc(idMaceta, ahora.minusDays(ventana), ahora);
        Riego ultimo = repository.findFirstByMacetaIdAndPulsosGreaterThanOrderByTsDesc(idMaceta, 0).orElse(null);
        Float capacidad = null;
        if (maceta.getHuertoIdhuerto() != null) {
            DepositoaguaDto deposito = depositoDAO.buscarPorIdHuerto(maceta.getHuertoIdhuerto());
            capacidad = deposito == null ? null : deposito.getCantidadMaxima();
        }
        return ResumenRiego.calcular(riegos, ultimo, ahora, ventana, capacidad);
    }
}
