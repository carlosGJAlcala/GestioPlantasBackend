package es.uah.huertojpa.riego.aplicacion;

import es.uah.huertojpa.maceta.infrastructura.database.IMacetaDAO;
import es.uah.huertojpa.riego.dominio.EstadoOrden;
import es.uah.huertojpa.riego.dominio.OrdenRiego;
import es.uah.huertojpa.riego.dominio.OrdenRiegoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * «Regar ahora»: la única acción que se puede ejecutar en la maceta.
 * <p>
 * Reglas (las salvaguardas físicas — nivel del depósito, humedad máxima del tipo, máximo de pulsos — las aplica
 * el firmware igual que en el riego automático):
 * <ul>
 *     <li>Como mucho <b>una orden pendiente</b> por maceta: pedirla dos veces devuelve la misma (idempotente).</li>
 *     <li>Una orden no recogida en {@code orden.caducidad-minutos} (15) caduca: si el ESP32 estaba sin red no
 *     riega horas después sin que nadie lo espere.</li>
 *     <li>Tras entregar una orden, no se acepta otra hasta pasados {@code orden.espera-minutos} (5): evita machacar
 *     la bomba pulsando el botón repetidamente y deja tiempo a que llegue el registro del riego.</li>
 * </ul>
 */
@Service
public class OrdenRiegoService {

    public enum Resultado { CREADA, YA_PENDIENTE, EN_ESPERA, MACETA_NO_EXISTE }

    /** {@code segundosEspera} solo tiene sentido con {@link Resultado#EN_ESPERA}. */
    public record Solicitud(Resultado resultado, OrdenRiegoDto orden, long segundosEspera) { }

    private final OrdenRiegoRepository repository;
    private final IMacetaDAO macetaDAO;
    private final Clock clock;
    private final long minutosCaducidad;
    private final long minutosEspera;

    @Autowired
    public OrdenRiegoService(OrdenRiegoRepository repository, IMacetaDAO macetaDAO,
                             @Value("${orden.caducidad-minutos:15}") long minutosCaducidad,
                             @Value("${orden.espera-minutos:5}") long minutosEspera) {
        this(repository, macetaDAO, Clock.system(RiegoServiceImpl.ZONA), minutosCaducidad, minutosEspera);
    }

    OrdenRiegoService(OrdenRiegoRepository repository, IMacetaDAO macetaDAO, Clock clock,
                      long minutosCaducidad, long minutosEspera) {
        this.repository = repository;
        this.macetaDAO = macetaDAO;
        this.clock = clock;
        this.minutosCaducidad = minutosCaducidad;
        this.minutosEspera = minutosEspera;
    }

    private LocalDateTime ahora() {
        // Milisegundos: en JSON sale "…T05:13:14.023" (con nanosegundos algunos navegadores no parsean la fecha).
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS);
    }

    /** Marca como CADUCADA las pendientes antiguas y devuelve la pendiente vigente (la más antigua), si la hay. */
    private Optional<OrdenRiego> pendienteVigente(Integer idMaceta) {
        LocalDateTime limite = ahora().minusMinutes(minutosCaducidad);
        List<OrdenRiego> pendientes = repository.findByMacetaIdAndEstadoOrderByCreadaAsc(idMaceta, EstadoOrden.PENDIENTE);
        OrdenRiego vigente = null;
        for (OrdenRiego o : pendientes) {
            if (o.getCreada().isBefore(limite)) {
                o.setEstado(EstadoOrden.CADUCADA);
                repository.save(o);
            } else if (vigente == null) {
                vigente = o;
            }
        }
        return Optional.ofNullable(vigente);
    }

    @Transactional
    public Solicitud solicitar(Integer idMaceta, String solicitante) {
        if (idMaceta == null || macetaDAO.buscarPorID(idMaceta) == null) {
            return new Solicitud(Resultado.MACETA_NO_EXISTE, null, 0);
        }
        Optional<OrdenRiego> pendiente = pendienteVigente(idMaceta);
        if (pendiente.isPresent()) {
            return new Solicitud(Resultado.YA_PENDIENTE, OrdenRiegoDto.de(pendiente.get(), minutosCaducidad), 0);
        }
        Optional<OrdenRiego> ultima = repository.findFirstByMacetaIdAndEstadoOrderByEntregadaDesc(idMaceta, EstadoOrden.ENTREGADA);
        if (ultima.isPresent() && ultima.get().getEntregada() != null) {
            LocalDateTime disponible = ultima.get().getEntregada().plusMinutes(minutosEspera);
            if (ahora().isBefore(disponible)) {
                return new Solicitud(Resultado.EN_ESPERA, OrdenRiegoDto.de(ultima.get(), minutosCaducidad),
                        Math.max(1, Duration.between(ahora(), disponible).toSeconds()));
            }
        }
        OrdenRiego o = new OrdenRiego();
        o.setMacetaId(idMaceta);
        o.setSolicitante(solicitante);
        o.setCreada(ahora());
        o.setEstado(EstadoOrden.PENDIENTE);
        return new Solicitud(Resultado.CREADA, OrdenRiegoDto.de(repository.save(o), minutosCaducidad), 0);
    }

    @Transactional
    public Optional<OrdenRiegoDto> pendiente(Integer idMaceta) {
        return pendienteVigente(idMaceta).map(o -> OrdenRiegoDto.de(o, minutosCaducidad));
    }

    /** La recoge el ESP32: pasa a ENTREGADA y no se vuelve a entregar. */
    @Transactional
    public Optional<OrdenRiegoDto> entregar(Integer idMaceta) {
        return pendienteVigente(idMaceta).map(o -> {
            o.setEstado(EstadoOrden.ENTREGADA);
            o.setEntregada(ahora());
            return OrdenRiegoDto.de(repository.save(o), minutosCaducidad);
        });
    }

    @Transactional
    public boolean cancelar(Integer idMaceta) {
        return pendienteVigente(idMaceta).map(o -> {
            o.setEstado(EstadoOrden.CANCELADA);
            repository.save(o);
            return true;
        }).orElse(false);
    }
}
