package es.uah.huertojpa.metricas;

import es.uah.huertojpa.depositoAgua.dominio.DepositoAgua;
import es.uah.huertojpa.depositoAgua.dominio.DepositoaguaRepository;
import es.uah.huertojpa.maceta.dominio.Maceta;
import es.uah.huertojpa.maceta.dominio.MacetaRepository;
import es.uah.huertojpa.metricas.MetricasMaceta.Rango;
import es.uah.huertojpa.planta.dominio.entities.Planta;
import es.uah.huertojpa.planta.dominio.repository.PlantaRepository;
import es.uah.huertojpa.registro.dominio.entities.Lectura;
import es.uah.huertojpa.registro.dominio.repository.LecturaRepository;
import es.uah.huertojpa.riego.dominio.OrdenRiegoRepository;
import es.uah.huertojpa.riego.dominio.RiegoRepository;
import es.uah.huertojpa.tipoPlanta.dominio.TipoPlantaRepository;
import es.uah.huertojpa.tipoPlanta.dominio.Tipoplanta;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MetricasService {
    static final int HORAS_MAXIMAS = 24 * 90;

    private final LecturaRepository lecturas;
    private final RiegoRepository riegos;
    private final OrdenRiegoRepository ordenes;
    private final PlantaRepository plantas;
    private final TipoPlantaRepository tipos;
    private final MacetaRepository macetas;
    private final DepositoaguaRepository depositos;
    private final Clock clock;

    public MetricasService(LecturaRepository lecturas, RiegoRepository riegos, OrdenRiegoRepository ordenes,
                           PlantaRepository plantas, TipoPlantaRepository tipos, MacetaRepository macetas,
                           DepositoaguaRepository depositos) {
        this.lecturas = lecturas;
        this.riegos = riegos;
        this.ordenes = ordenes;
        this.plantas = plantas;
        this.tipos = tipos;
        this.macetas = macetas;
        this.depositos = depositos;
        this.clock = Clock.system(ZoneId.of("Europe/Madrid"));
    }

    public MetricasMaceta calcular(Integer idMaceta, int horas) {
        return calcular(idMaceta, horas, ahora());
    }

    /** Hora local del huerto (la misma con la que el backend sella lecturas y riegos). */
    public LocalDateTime ahora() {
        return LocalDateTime.now(clock);
    }

    /** Métricas a una hora concreta (las alertas y los tests fijan {@code ahora}). */
    public MetricasMaceta calcular(Integer idMaceta, int horas, LocalDateTime ahora) {
        return calcular(idMaceta, horas, ahora, deposito(idMaceta, ahora));
    }

    /**
     * W9 – Igual, pero el depósito se calcula una sola vez por huerto: {@code depositosPorHuerto} guarda los ya
     * calculados en esta vuelta (panel de un usuario, revisión de alertas). Todas las macetas de un huerto comparten
     * depósito, así que con N macetas antes se leían N veces las mismas lecturas del ultrasonidos.
     */
    public MetricasMaceta calcular(Integer idMaceta, int horas, LocalDateTime ahora,
                                   Map<Integer, NivelDeposito> depositosPorHuerto) {
        Integer huerto = macetas.findById(idMaceta).map(Maceta::getHuertoIdhuerto).orElse(null);
        NivelDeposito deposito = huerto == null ? NivelDeposito.SIN_DATOS
                : depositosPorHuerto.computeIfAbsent(huerto, h -> depositoDeHuerto(h, ahora));
        return calcular(idMaceta, horas, ahora, deposito);
    }

    private MetricasMaceta calcular(Integer idMaceta, int horas, LocalDateTime ahora, NivelDeposito deposito) {
        horas = Math.max(1, Math.min(horas, HORAS_MAXIMAS));
        LocalDateTime desde = ahora.minusHours(horas);

        // Rangos del tipo de la (primera) planta de la maceta: son los mismos que usa el ESP32 para regar.
        Optional<Tipoplanta> tipo = tipoDeMaceta(idMaceta);
        Rango humedad = tipo.map(t -> new Rango(t.getNivelHumedadNecesarioMinimo(), t.getNivelHumedadNecesarioMaximo())).orElse(null);
        Rango temperatura = tipo.map(t -> new Rango(t.getNivelTemperaturaNecesarioMinimo(), t.getNivelTemperaturaNecesarioMaximo())).orElse(null);
        Rango luz = tipo.map(t -> new Rango(t.getNivelLuxNecesarioMinimo(), t.getNivelLuxNecesarioMaximo())).orElse(null);

        return MetricasMaceta.calcular(horas, ahora,
                serie(idMaceta, "HUMEDAD", desde, ahora),
                serie(idMaceta, "TEMPERATURA", desde, ahora),
                serie(idMaceta, "LUZ", desde, ahora),
                humedad, temperatura, luz,
                // Se piden 48 h extra para conocer el último riego aunque sea anterior a la ventana.
                riegos.findByMacetaIdAndTsBetweenOrderByTsAsc(idMaceta, desde.minusHours(48), ahora),
                ordenes.findByMacetaIdAndCreadaBetween(idMaceta, desde, ahora),
                lecturas.findFirstByMacetaIdOrderByTsDesc(idMaceta).map(Lectura::getTs).orElse(null),
                deposito);
    }

    /**
     * A2 – Nivel del depósito del huerto de la maceta (el que la riega). Las lecturas del ultrasonidos se guardan
     * como distancia en cm; aquí se pasan a % con la altura del depósito, descartando las inválidas.
     */
    public NivelDeposito deposito(Integer idMaceta, LocalDateTime ahora) {
        return macetas.findById(idMaceta).map(Maceta::getHuertoIdhuerto)
                .map(h -> depositoDeHuerto(h, ahora)).orElse(NivelDeposito.SIN_DATOS);
    }

    /** Nivel del depósito de un huerto (sin depósito o sin sensor ⇒ {@link NivelDeposito#SIN_DATOS}). */
    public NivelDeposito depositoDeHuerto(Integer idHuerto, LocalDateTime ahora) {
        Optional<DepositoAgua> deposito = depositos.findByHuertoIdhuerto(idHuerto);
        if (deposito.isEmpty() || deposito.get().getSensorIdsensor() == null) return NivelDeposito.SIN_DATOS;
        DepositoAgua d = deposito.get();
        Integer sensor = d.getSensorIdsensor().getId(); // proxy LAZY: leer el id no lanza consulta
        List<Lectura> niveles = lecturas.findBySensorIdAndTsBetweenOrderByTsAsc(sensor,
                        ahora.minus(NivelDeposito.VENTANA), ahora).stream()
                .map(l -> {
                    Float p = NivelDeposito.porcentaje(l.getValor(), d.getAlturaDeposito());
                    return p == null ? null : new Lectura(l.getSensorId(), null, "DEPOSITO", p, l.getTs());
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        // A5: el último riego con bomba de cualquier maceta del huerto (todas beben del mismo depósito).
        LocalDateTime ultimoRiego = riegos.ultimoRiegoConBombaEnHuerto(idHuerto, ahora);
        return NivelDeposito.calcular(niveles, ahora, d.getCantidadMaxima(), ultimoRiego);
    }

    /** Tipo de la (primera) planta de la maceta: sus rangos son los mismos que usa el ESP32 para regar. */
    public Optional<Tipoplanta> tipoDeMaceta(Integer idMaceta) {
        return plantas.findByMacetaIdmaceta(idMaceta).stream()
                .map(Planta::getTipoplantaIdtipoplanta).findFirst().flatMap(tipos::findById);
    }

    /** Rango ideal de la planta para una magnitud (HUMEDAD, TEMPERATURA, LUZ); vacío si no hay tipo o magnitud. */
    public Rango rango(Integer idMaceta, String magnitud) {
        return tipoDeMaceta(idMaceta).map(t -> switch (magnitud) {
            case "HUMEDAD" -> new Rango(t.getNivelHumedadNecesarioMinimo(), t.getNivelHumedadNecesarioMaximo());
            case "TEMPERATURA" -> new Rango(t.getNivelTemperaturaNecesarioMinimo(), t.getNivelTemperaturaNecesarioMaximo());
            case "LUZ" -> new Rango(t.getNivelLuxNecesarioMinimo(), t.getNivelLuxNecesarioMaximo());
            default -> Rango.VACIO;
        }).orElse(Rango.VACIO);
    }

    private List<Lectura> serie(Integer idMaceta, String magnitud, LocalDateTime desde, LocalDateTime hasta) {
        return lecturas.findByMacetaIdAndMagnitudAndTsBetweenOrderByTsAsc(idMaceta, magnitud, desde, hasta);
    }
}
