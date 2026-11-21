package es.uah.huertojpa.registro.aplicacion;

import es.uah.huertojpa.maceta.dominio.MacetaHasSensor;
import es.uah.huertojpa.maceta.infrastructura.database.IMacetaHasSensorDAO;
import es.uah.huertojpa.registro.dominio.entities.Lectura;
import es.uah.huertojpa.registro.dominio.repository.LecturaRepository;
import es.uah.huertojpa.sensores.dominio.entidades.SensorAdapterObserver;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDepositoAguaAdapterObserver;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * P-DB1 – Guarda en {@code lectura} cada medida que llega del ESP32 (mismos eventos que ya escuchan
 * PlantaServiceImpl y DepositoAguaServiceImpl). Un fallo aquí nunca debe impedir que se actualice la planta:
 * se registra en el log y se sigue.
 */
@Component
public class LecturaRegistrador {
    private static final Logger log = LoggerFactory.getLogger(LecturaRegistrador.class);

    private final LecturaRepository repository;
    private final IMacetaHasSensorDAO macetaHasSensorDAO;
    private final Clock clock;

    @Autowired
    public LecturaRegistrador(LecturaRepository repository, IMacetaHasSensorDAO macetaHasSensorDAO) {
        this(repository, macetaHasSensorDAO, Clock.system(ZoneId.of("Europe/Madrid")));
    }

    LecturaRegistrador(LecturaRepository repository, IMacetaHasSensorDAO macetaHasSensorDAO, Clock clock) {
        this.repository = repository;
        this.macetaHasSensorDAO = macetaHasSensorDAO;
        this.clock = clock;
    }

    @EventListener
    public void alMedirSensorMaceta(SensorAdapterObserver evento) {
        registrar(evento.getSensorDto());
    }

    @EventListener
    public void alMedirDeposito(SensorDepositoAguaAdapterObserver evento) {
        registrar(evento.getSensorDto());
    }

    void registrar(SensorDto sensor) {
        try {
            Float valor = valor(sensor.getCantidadMedida());
            if (sensor.getId() == null || valor == null) return;
            String magnitud = normalizarMagnitud(sensor.getMagnitudAMedir());
            LocalDateTime ahora = LocalDateTime.now(clock);

            List<Lectura> filas = new ArrayList<>();
            List<MacetaHasSensor> macetas = macetaHasSensorDAO.buscarPorIDSensor(sensor.getId());
            if (macetas == null || macetas.isEmpty()) {
                filas.add(new Lectura(sensor.getId(), null, magnitud, valor, ahora));
            } else {
                for (MacetaHasSensor m : macetas) {
                    filas.add(new Lectura(sensor.getId(), m.getId().getMacetaIdmaceta(), magnitud, valor, ahora));
                }
            }
            repository.saveAll(filas);
        } catch (RuntimeException e) {
            log.warn("No se pudo guardar la lectura del sensor {}: {}", sensor.getId(), e.getMessage());
        }
    }

    /** Valor numérico de la medida; null si no es un número finito (lectura inválida del ESP32). */
    static Float valor(String cantidad) {
        if (cantidad == null) return null;
        try {
            float v = Float.parseFloat(cantidad.trim().replace(',', '.'));
            return Float.isFinite(v) ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Unifica los nombres que usa el firmware: «Luminosidad» y «luz» son la misma magnitud. */
    static String normalizarMagnitud(String magnitud) {
        if (magnitud == null || magnitud.isBlank()) return "DESCONOCIDA";
        String m = magnitud.trim().toUpperCase(Locale.ROOT);
        if (m.equals("LUMINOSIDAD")) return "LUZ";
        return m.length() > 20 ? m.substring(0, 20) : m;
    }
}
