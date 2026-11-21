package es.uah.huertojpa.metricas;

import es.uah.huertojpa.alertas.AlertaService;
import es.uah.huertojpa.huerto.dominio.repository.HuertoHasUsuarioRepository;
import es.uah.huertojpa.maceta.dominio.Maceta;
import es.uah.huertojpa.maceta.dominio.MacetaRepository;
import es.uah.huertojpa.planta.dominio.entities.Planta;
import es.uah.huertojpa.planta.dominio.repository.PlantaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * W9 – Panel de un usuario en una sola petición: macetas de sus huertos (todas si es administrador), métricas de
 * cada una calculadas a la misma hora y con el depósito una vez por huerto, y sus alertas abiertas.
 */
@Service
public class PanelService {
    private final MetricasService metricas;
    private final AlertaService alertas;
    private final MacetaRepository macetas;
    private final PlantaRepository plantas;
    private final HuertoHasUsuarioRepository huertoUsuarios;

    public PanelService(MetricasService metricas, AlertaService alertas, MacetaRepository macetas,
                        PlantaRepository plantas, HuertoHasUsuarioRepository huertoUsuarios) {
        this.metricas = metricas;
        this.alertas = alertas;
        this.macetas = macetas;
        this.plantas = plantas;
        this.huertoUsuarios = huertoUsuarios;
    }

    /** @param idPersona persona del token, o {@code null} para el administrador (todas las macetas). */
    public PanelUsuario panel(Integer idPersona, int horas) {
        return panel(idPersona, horas, metricas.ahora());
    }

    public PanelUsuario panel(Integer idPersona, int horas, LocalDateTime ahora) {
        horas = Math.max(1, Math.min(horas, MetricasService.HORAS_MAXIMAS));
        Map<Integer, NivelDeposito> depositos = new HashMap<>();
        List<PanelUsuario.Tarjeta> tarjetas = new java.util.ArrayList<>();
        for (Maceta m : macetasVisibles(idPersona)) {
            List<String> nombres = plantas.findByMacetaIdmaceta(m.getId()).stream()
                    .map(Planta::getNombrePlanta).filter(Objects::nonNull).toList();
            tarjetas.add(new PanelUsuario.Tarjeta(m.getId(), m.getHuertoIdhuerto(), m.getPosicionMacetaenElHuerto(),
                    nombres, metricas.calcular(m.getId(), horas, ahora, depositos)));
        }
        return new PanelUsuario(ahora, horas, tarjetas, alertas.abiertas(idPersona));
    }

    /** Macetas de los huertos de la persona (sin repetir), o todas si {@code idPersona} es null. */
    List<Maceta> macetasVisibles(Integer idPersona) {
        List<Maceta> lista = idPersona == null ? macetas.findAll()
                : huertoUsuarios.findById_UsuarioPersonaId(idPersona).stream()
                        .map(hu -> hu.getId().getHuertoIdhuerto()).distinct()
                        .flatMap(h -> macetas.findByHuertoIdHuerto(h).stream())
                        .toList();
        return lista.stream()
                .sorted(Comparator.comparing(Maceta::getHuertoIdhuerto, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Maceta::getId))
                .toList();
    }
}
