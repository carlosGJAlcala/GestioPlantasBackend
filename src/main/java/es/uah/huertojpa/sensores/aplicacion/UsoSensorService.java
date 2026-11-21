package es.uah.huertojpa.sensores.aplicacion;

import es.uah.huertojpa.depositoAgua.dominio.DepositoAgua;
import es.uah.huertojpa.depositoAgua.dominio.DepositoaguaRepository;
import es.uah.huertojpa.maceta.dominio.MacetaHasSensor;
import es.uah.huertojpa.maceta.dominio.MacetaHasSensorRepository;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDto;
import es.uah.huertojpa.sensores.dominio.entidades.SensorUso;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * W12b – Dónde se usa cada sensor (macetas y depósito), en dos consultas para toda la lista. También da el motivo por
 * el que un sensor no se puede borrar: antes el DELETE lo quitaba sin avisar de la maceta (CASCADE) o del depósito
 * (SET NULL), y la maceta se quedaba sin humedad o el depósito sin nivel.
 */
@Service
public class UsoSensorService {
    private final MacetaHasSensorRepository macetaHasSensorRepository;
    private final DepositoaguaRepository depositoRepository;

    public UsoSensorService(MacetaHasSensorRepository macetaHasSensorRepository, DepositoaguaRepository depositoRepository) {
        this.macetaHasSensorRepository = macetaHasSensorRepository;
        this.depositoRepository = depositoRepository;
    }

    public List<SensorUso> conUso(List<SensorDto> sensores) {
        if (sensores.isEmpty()) return List.of();
        Set<Integer> ids = sensores.stream().map(SensorDto::getId).collect(Collectors.toSet());
        Map<Integer, List<Integer>> macetas = new HashMap<>();
        for (MacetaHasSensor ms : macetaHasSensorRepository.findById_SensorIdsensorIn(ids)) {
            macetas.computeIfAbsent(ms.getId().getSensorIdsensor(), k -> new ArrayList<>()).add(ms.getId().getMacetaIdmaceta());
        }
        Map<Integer, Integer> huertoDeposito = new HashMap<>();
        for (DepositoAgua d : depositoRepository.findBySensorIdsensor_IdIn(ids)) {
            huertoDeposito.put(d.getSensorIdsensor().getId(), d.getHuertoIdhuerto());
        }
        return sensores.stream()
                .sorted(Comparator.comparing(SensorDto::getId))
                .map(s -> {
                    List<Integer> enMacetas = macetas.getOrDefault(s.getId(), List.of()).stream().sorted().toList();
                    Integer huerto = huertoDeposito.get(s.getId());
                    return new SensorUso(s.getId(), s.getNombreSensor(), s.getMagnitudAMedir(), s.getCantidadMedida(),
                            s.getUnidades(), enMacetas, huerto, enMacetas.isEmpty() && huerto == null);
                })
                .toList();
    }

    /** {@code null} si el sensor no lo usa nada; si no, el texto que se enseña al usuario. */
    public String motivoNoBorrable(Integer idSensor) {
        var enMacetas = macetaHasSensorRepository.findById_SensorIdsensor(idSensor);
        if (!enMacetas.isEmpty()) {
            String lista = enMacetas.stream().map(ms -> String.valueOf(ms.getId().getMacetaIdmaceta()))
                    .sorted().collect(Collectors.joining(", "));
            return "El sensor mide la maceta " + lista + ". Quítalo de la maceta antes de borrarlo.";
        }
        return depositoRepository.findBySensorIdsensor_Id(idSensor)
                .map(d -> "El sensor mide el depósito del huerto " + d.getHuertoIdhuerto()
                        + ". Quítalo del depósito antes de borrarlo.")
                .orElse(null);
    }
}
