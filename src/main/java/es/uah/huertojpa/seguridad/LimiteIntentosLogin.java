package es.uah.huertojpa.seguridad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * S6 – Límite de intentos de login. Antes {@code /auth/login} aceptaba intentos sin fin: con la web expuesta (o desde
 * cualquier equipo de la red) se podía probar un diccionario de contraseñas contra «admin» sin que nada lo frenara.
 * <p>
 * Se cuentan los fallos en dos claves:
 * <ul>
 *     <li><b>usuario + IP</b>: {@code login.intentos-maximos} (5) fallos en {@code login.ventana} (15 min) ⇒ ese
 *     usuario queda bloqueado <i>desde esa IP</i>. No se bloquea el usuario para todo el mundo: si no, cualquiera
 *     podría dejar sin entrar al dueño solo con equivocarse a propósito.</li>
 *     <li><b>IP</b>: {@code login.intentos-maximos-ip} (20) fallos, sea cual sea el usuario ⇒ esa IP queda bloqueada
 *     (frena que se prueben muchos usuarios con la misma contraseña).</li>
 * </ul>
 * El bloqueo dura {@code login.bloqueo} (15 min) y se <b>duplica</b> cada vez que la misma clave vuelve a bloquearse
 * sin un login correcto entre medias, hasta 24 h. Un login correcto borra el contador de usuario + IP; el
 * restablecimiento de contraseña por el admin (S8) borra los de ese usuario.
 * <p>
 * Todo en memoria: un reinicio del backend lo olvida (aceptable en una instalación casera; ver la entrega
 * 2026-11-14). La IP es {@code request.getRemoteAddr()}: detrás del nginx de la Raspberry hace falta
 * {@code server.forward-headers-strategy=native} para que sea la del navegador y no la del contenedor de nginx.
 */
@Component
public class LimiteIntentosLogin {
    private static final Logger log = LoggerFactory.getLogger(LimiteIntentosLogin.class);
    private static final Duration BLOQUEO_MAXIMO = Duration.ofHours(24);
    /** Con más claves que estas se limpian las caducadas (para que la memoria no crezca sin límite). */
    private static final int LIMPIAR_A_PARTIR_DE = 5_000;

    /** Estado de una clave. {@code bloqueos}: veces seguidas que se ha bloqueado (para duplicar la espera). */
    record Contador(int fallos, Instant primerFallo, Instant bloqueadoHasta, int bloqueos) {
        boolean bloqueado(Instant ahora) {
            return bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
        }
    }

    private final Map<String, Contador> contadores = new ConcurrentHashMap<>();
    private final int maximoUsuario;
    private final int maximoIp;
    private final Duration ventana;
    private final Duration bloqueo;

    public LimiteIntentosLogin(@Value("${login.intentos-maximos:5}") int maximoUsuario,
                               @Value("${login.intentos-maximos-ip:20}") int maximoIp,
                               @Value("${login.ventana:PT15M}") Duration ventana,
                               @Value("${login.bloqueo:PT15M}") Duration bloqueo) {
        this.maximoUsuario = Math.max(1, maximoUsuario);
        this.maximoIp = Math.max(1, maximoIp);
        this.ventana = ventana;
        this.bloqueo = bloqueo;
    }

    private static String normalizar(String usuario) {
        return usuario == null ? "" : usuario.trim().toLowerCase(Locale.ROOT);
    }

    private static String claveUsuario(String usuario, String ip) {
        return "u:" + normalizar(usuario) + "|" + (ip == null ? "?" : ip);
    }

    private static String claveIp(String ip) {
        return "ip:" + (ip == null ? "?" : ip);
    }

    /**
     * Tiempo que falta para poder intentarlo otra vez, o {@link Duration#ZERO} si se puede ya. Se mira <b>antes</b>
     * de comprobar la contraseña: mientras dura el bloqueo ni siquiera se sabe si la que se manda es buena.
     */
    public Duration esperaRestante(String usuario, String ip, Instant ahora) {
        Duration porUsuario = espera(contadores.get(claveUsuario(usuario, ip)), ahora);
        Duration porIp = espera(contadores.get(claveIp(ip)), ahora);
        return porUsuario.compareTo(porIp) >= 0 ? porUsuario : porIp;
    }

    private static Duration espera(Contador c, Instant ahora) {
        return c != null && c.bloqueado(ahora) ? Duration.between(ahora, c.bloqueadoHasta()) : Duration.ZERO;
    }

    /** Apunta un intento fallido. */
    public void fallo(String usuario, String ip, Instant ahora) {
        limpiarSiHaceFalta(ahora);
        sumar(claveUsuario(usuario, ip), maximoUsuario, ahora);
        sumar(claveIp(ip), maximoIp, ahora);
    }

    private void sumar(String clave, int maximo, Instant ahora) {
        Contador nuevo = contadores.compute(clave, (k, c) -> {
            int bloqueos = c == null ? 0 : c.bloqueos();
            boolean ventanaNueva = c == null || c.primerFallo() == null
                    || !ahora.isBefore(c.primerFallo().plus(ventana));
            int fallos = ventanaNueva ? 1 : c.fallos() + 1;
            Instant primero = ventanaNueva ? ahora : c.primerFallo();
            if (fallos >= maximo) {
                // 15 min, 30 min, 1 h… hasta 24 h. Tras el bloqueo se vuelve a contar desde cero.
                Duration espera = bloqueo.multipliedBy(1L << Math.min(bloqueos, 10));
                if (espera.compareTo(BLOQUEO_MAXIMO) > 0) espera = BLOQUEO_MAXIMO;
                return new Contador(0, null, ahora.plus(espera), bloqueos + 1);
            }
            return new Contador(fallos, primero, c == null ? null : c.bloqueadoHasta(), bloqueos);
        });
        if (nuevo.fallos() == 0 && nuevo.bloqueado(ahora)) {
            log.warn("Login bloqueado ({}) hasta {} tras {} intentos fallidos", clave, nuevo.bloqueadoHasta(), maximo);
        }
    }

    /** Login correcto: se olvidan los fallos de ese usuario desde esa IP (los de la IP no: siguen contando). */
    public void exito(String usuario, String ip) {
        contadores.remove(claveUsuario(usuario, ip));
    }

    /** S8: el admin ha restablecido la contraseña; se quitan los bloqueos de ese usuario desde cualquier IP. */
    public void desbloquearUsuario(String usuario) {
        String prefijo = "u:" + normalizar(usuario) + "|";
        contadores.keySet().removeIf(k -> k.startsWith(prefijo));
    }

    /** Para los tests (el contexto de Spring se reutiliza entre clases). */
    public void reiniciar() {
        contadores.clear();
    }

    private void limpiarSiHaceFalta(Instant ahora) {
        if (contadores.size() < LIMPIAR_A_PARTIR_DE) return;
        contadores.entrySet().removeIf(e -> {
            Contador c = e.getValue();
            boolean ventanaCaducada = c.primerFallo() == null || !ahora.isBefore(c.primerFallo().plus(ventana));
            return ventanaCaducada && !c.bloqueado(ahora);
        });
    }
}
