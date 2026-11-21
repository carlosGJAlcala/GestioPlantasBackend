package es.uah.huertojpa.metricas;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.uah.huertojpa.alertas.Alerta;

import java.time.LocalDateTime;
import java.util.List;

/**
 * W9 – Todo lo que pinta el panel en una sola respuesta: una tarjeta por maceta visible para el usuario (con sus
 * métricas y su estado) y las alertas abiertas. Antes el panel hacía 1 + N + 1 peticiones por minuto (macetas,
 * métricas de cada maceta, alertas) y el depósito del huerto se calculaba una vez por maceta.
 *
 * @param generado hora local del huerto a la que se calculó (las métricas son «a esta hora»)
 * @param horas    ventana de las métricas
 * @param macetas  una entrada por maceta, ordenadas por huerto e id
 * @param alertas  alertas abiertas de esas macetas (todas si es administrador), las más recientes primero
 */
public record PanelUsuario(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime generado,
        int horas,
        List<Tarjeta> macetas,
        List<Alerta> alertas) {

    /**
     * Una maceta del panel. Lleva los mismos nombres de campo que {@code GET /maceta} ({@code id},
     * {@code huertoIdhuerto}, {@code posicionMacetaenElHuerto}) para que el front no tenga que traducirlos.
     *
     * @param plantas  nombres de las plantas de la maceta (vacío si no tiene)
     * @param metricas las de {@code GET /metricas/maceta/{id}}, con su {@code estado}
     */
    public record Tarjeta(Integer id, Integer huertoIdhuerto, String posicionMacetaenElHuerto, List<String> plantas,
                          MetricasMaceta metricas) {
    }
}
