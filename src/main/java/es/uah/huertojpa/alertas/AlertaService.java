package es.uah.huertojpa.alertas;

import es.uah.huertojpa.huerto.dominio.repository.HuertoHasUsuarioRepository;
import es.uah.huertojpa.maceta.dominio.Maceta;
import es.uah.huertojpa.maceta.dominio.MacetaRepository;
import es.uah.huertojpa.metricas.MetricasService;
import es.uah.huertojpa.metricas.NivelDeposito;
import es.uah.huertojpa.persona.dominio.entidades.Persona;
import es.uah.huertojpa.persona.dominio.repository.PersonaRepository;
import es.uah.huertojpa.planta.dominio.entities.Planta;
import es.uah.huertojpa.planta.dominio.repository.PlantaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Alertas de las macetas: en cada revisión calcula el {@link EstadoMaceta} (mismos criterios que el panel) y lo
 * contrasta con las alertas abiertas en BD.
 * <ul>
 *   <li>Problema nuevo ⇒ se abre una alerta. Problema que sigue ⇒ se actualiza su mensaje y {@code vista}.</li>
 *   <li>Problema que ya no aparece ⇒ se cierra cuando lleva {@code alertas.espera-cierre} sin verse (histéresis:
 *   una humedad que baila alrededor del mínimo no abre y cierra alertas cada 5 minutos).</li>
 *   <li>Aviso por correo <b>una sola vez por alerta</b>: las críticas en la primera revisión; los avisos cuando
 *   llevan {@code alertas.espera-aviso} abiertos (la tierra seca unos minutos antes de que el ESP32 riegue es
 *   normal y no merece un correo).</li>
 *   <li>A3: no se avisa de una alerta silenciada («Entendido» en el panel) ni a quien ha desactivado los avisos
 *   por correo en su perfil.</li>
 *   <li>A2/A5: «Depósito bajo» y «Posible fuga» son del huerto: se avisa una vez aunque lo vean varias macetas.</li>
 * </ul>
 */
@Service
public class AlertaService {
    private static final Logger log = LoggerFactory.getLogger(AlertaService.class);
    /** Ventana de las métricas que alimentan el estado (la misma que usa el panel). */
    static final int HORAS_METRICAS = 24;

    private final AlertaRepository alertas;
    private final MetricasService metricas;
    private final MacetaRepository macetas;
    private final PlantaRepository plantas;
    private final HuertoHasUsuarioRepository huertoUsuarios;
    private final PersonaRepository personas;
    private final NotificadorAlertas notificador;
    private final Duration esperaAviso;
    private final Duration esperaCierre;

    public AlertaService(AlertaRepository alertas, MetricasService metricas, MacetaRepository macetas,
                         PlantaRepository plantas, HuertoHasUsuarioRepository huertoUsuarios, PersonaRepository personas,
                         NotificadorAlertas notificador,
                         @Value("${alertas.espera-aviso:PT30M}") Duration esperaAviso,
                         @Value("${alertas.espera-cierre:PT30M}") Duration esperaCierre) {
        this.alertas = alertas;
        this.metricas = metricas;
        this.macetas = macetas;
        this.plantas = plantas;
        this.huertoUsuarios = huertoUsuarios;
        this.personas = personas;
        this.notificador = notificador;
        this.esperaAviso = esperaAviso;
        this.esperaCierre = esperaCierre;
    }

    /** Revisa todas las macetas. Devuelve cuántas alertas se han notificado en esta vuelta. */
    public int revisarTodas(LocalDateTime ahora) {
        int avisadas = 0;
        Map<Integer, NivelDeposito> depositos = new HashMap<>(); // W9: un cálculo de depósito por huerto
        for (Maceta m : macetas.findAll()) {
            try {
                avisadas += revisar(m, ahora, depositos);
            } catch (RuntimeException e) {
                // Una maceta con datos raros no debe impedir revisar las demás.
                log.error("Error revisando las alertas de la maceta {}", m.getId(), e);
            }
        }
        return avisadas;
    }

    int revisar(Maceta maceta, LocalDateTime ahora, Map<Integer, NivelDeposito> depositos) {
        Integer id = maceta.getId();
        EstadoMaceta estado = EstadoMaceta.de(metricas.calcular(id, HORAS_METRICAS, ahora, depositos));
        Map<TipoAlerta, Alerta> abiertas = alertas.findByMacetaIdAndFinIsNull(id).stream()
                .collect(Collectors.toMap(Alerta::getTipo, Function.identity(), (a, b) -> a));

        List<Alerta> presentes = new ArrayList<>();
        for (Condicion c : estado.condiciones()) {
            Alerta a = abiertas.remove(c.tipo());
            if (a == null) {
                a = new Alerta(id, c, ahora);
                log.info("Alerta abierta en la maceta {}: {}", id, c.mensaje());
            } else {
                a.actualizar(c, ahora);
            }
            presentes.add(alertas.save(a));
        }
        // Las que no han aparecido en esta revisión se cierran si llevan un rato sin verse.
        for (Alerta a : abiertas.values()) {
            if (!a.getVista().plus(esperaCierre).isAfter(ahora)) {
                a.setFin(ahora);
                alertas.save(a);
                log.info("Alerta cerrada en la maceta {}: {}", id, a.getTitulo());
            }
        }
        return notificarPendientes(maceta, presentes, ahora);
    }

    /** Solo se avisa de problemas presentes ahora: una alerta que ya no se ve (a punto de cerrarse) no se avisa. */
    private int notificarPendientes(Maceta maceta, List<Alerta> presentes, LocalDateTime ahora) {
        List<Alerta> pendientes = new ArrayList<>(presentes.stream()
                .filter(a -> a.getNotificada() == null && a.getSilenciada() == null)
                .filter(a -> a.getNivel() == NivelAlerta.CRITICO || !a.getInicio().plus(esperaAviso).isAfter(ahora))
                .toList());
        // A2/A5: si otra maceta del mismo huerto ya avisó (o silenció) lo del depósito, esta no repite el correo.
        pendientes.removeIf(a -> {
            if (!a.getTipo().esDelHuerto() || !depositoYaAtendido(maceta, a.getTipo())) return false;
            a.setNotificada(ahora);
            alertas.save(a);
            return true;
        });
        if (pendientes.isEmpty()) return 0;

        boolean enviada = notificador.notificar(descripcion(maceta), correosDuenos(maceta), pendientes);
        // Se marca aunque no haya salido el correo (sin configurar, sin destinatarios o error, que queda en el log):
        // reintentar cada 5 minutos solo llenaría el log, y la alerta sigue visible en el panel.
        for (Alerta a : pendientes) {
            a.setNotificada(ahora);
            alertas.save(a);
        }
        return enviada ? pendientes.size() : 0;
    }

    private boolean depositoYaAtendido(Maceta maceta, TipoAlerta tipo) {
        if (maceta.getHuertoIdhuerto() == null) return false;
        List<Integer> otras = macetas.findByHuertoIdHuerto(maceta.getHuertoIdhuerto()).stream()
                .map(Maceta::getId).filter(i -> !i.equals(maceta.getId())).toList();
        return !otras.isEmpty() && alertas.yaAtendida(tipo, otras);
    }

    /**
     * A3 – «Entendido»: la alerta sigue abierta (y en el panel) pero no se avisará por correo. Si ya estaba cerrada
     * o silenciada no cambia nada. Vacío si no existe.
     */
    public java.util.Optional<Alerta> silenciar(Long id, LocalDateTime ahora) {
        return alertas.findById(id).map(a -> {
            if (a.isAbierta() && a.getSilenciada() == null) {
                a.setSilenciada(ahora);
                a = alertas.save(a);
                log.info("Alerta {} silenciada en la maceta {}: {}", id, a.getMacetaId(), a.getTitulo());
            }
            return a;
        });
    }

    /** «Tomate, Albahaca (balcón)» o «Maceta 7» si no tiene plantas. */
    String descripcion(Maceta maceta) {
        String nombres = plantas.findByMacetaIdmaceta(maceta.getId()).stream()
                .map(Planta::getNombrePlanta).filter(Objects::nonNull).collect(Collectors.joining(", "));
        String base = nombres.isBlank() ? "Maceta " + maceta.getId() : nombres;
        String pos = maceta.getPosicionMacetaenElHuerto();
        return pos == null || pos.isBlank() ? base : base + " (" + pos + ")";
    }

    /** Correos de los usuarios del huerto de la maceta que no han desactivado los avisos (A3). */
    List<String> correosDuenos(Maceta maceta) {
        if (maceta.getHuertoIdhuerto() == null) return List.of();
        List<String> correos = new ArrayList<>();
        huertoUsuarios.findById_HuertoIdhuerto(maceta.getHuertoIdhuerto()).forEach(hu ->
                personas.findById(hu.getId().getUsuarioPersonaId())
                        .filter(Persona::quiereAvisosPorCorreo)
                        .map(Persona::getCorreoElectronico)
                        .filter(c -> c != null && c.contains("@")).ifPresent(correos::add));
        return correos.stream().distinct().toList();
    }

    /** Historial de una maceta: abiertas y cerradas en los últimos {@code dias} días. */
    public List<Alerta> historial(Integer idMaceta, int dias) {
        dias = Math.max(1, Math.min(dias, 90));
        return alertas.historial(idMaceta, metricas.ahora().minusDays(dias));
    }

    /** Alertas abiertas de las macetas de una persona (todas si {@code persona} es null, caso del admin). */
    public List<Alerta> abiertas(Integer idPersona) {
        if (idPersona == null) return alertas.findByFinIsNullOrderByInicioDesc();
        List<Integer> ids = huertoUsuarios.findById_UsuarioPersonaId(idPersona).stream()
                .flatMap(hu -> macetas.findByHuertoIdHuerto(hu.getId().getHuertoIdhuerto()).stream())
                .map(Maceta::getId).distinct().toList();
        return ids.isEmpty() ? List.of() : alertas.findByFinIsNullAndMacetaIdInOrderByInicioDesc(ids);
    }
}
