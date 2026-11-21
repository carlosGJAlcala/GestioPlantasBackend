package es.uah.huertojpa.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * S3 – Autenticación del ESP32 con una clave de dispositivo (cabecera {@value #CABECERA}).
 * <p>
 * Solo actúa sobre los endpoints del dispositivo ({@code /auth/**} salvo login/register). Si la clave coincide con
 * {@code device.api-key}, la petición queda autenticada con la autoridad {@value #AUTORIDAD}. Si no coincide, la
 * petición sigue sin autenticar y será rechazada (401/403) salvo que traiga un JWT válido (el front de administración
 * también usa {@code PUT /auth/sensor}).
 * <p>
 * Si {@code device.api-key} está vacía se trabaja en <b>modo compatibilidad</b> (los endpoints del dispositivo siguen
 * abiertos, como antes) y se avisa en el log: así no se rompe un ESP32 ya desplegado con el firmware antiguo.
 */
@Component
public class DispositivoKeyFilter extends OncePerRequestFilter {
    public static final String CABECERA = "X-Device-Key";
    public static final String AUTORIDAD = "DEVICE";
    private static final Logger log = LoggerFactory.getLogger(DispositivoKeyFilter.class);

    private final byte[] clave;

    public DispositivoKeyFilter(@Value("${device.api-key:}") String clave) {
        this.clave = clave == null || clave.isBlank() ? null : clave.trim().getBytes(StandardCharsets.UTF_8);
        if (this.clave == null) {
            log.warn("device.api-key (DEVICE_API_KEY) no configurada: endpoints /auth/** del dispositivo ABIERTOS (modo compatibilidad)");
        }
    }

    public boolean modoCompatibilidad() {
        return clave == null;
    }

    static boolean esRutaDispositivo(String ruta) {
        return ruta.startsWith("/auth/") && !ruta.equals("/auth/login") && !ruta.equals("/auth/register");
    }

    boolean claveValida(String recibida) {
        return clave != null && recibida != null
                && MessageDigest.isEqual(clave, recibida.trim().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Ruta sin el context-path. No se usa {@code getServletPath()}: depende de cómo esté mapeado el servlet (vale
     * {@code ""} con MockMvc o con otro mapeo) y la clave del ESP32 dejaría de reconocerse.
     */
    static String ruta(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contexto = request.getContextPath();
        return contexto != null && !contexto.isEmpty() && uri.startsWith(contexto) ? uri.substring(contexto.length()) : uri;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (esRutaDispositivo(ruta(request))
                && SecurityContextHolder.getContext().getAuthentication() == null
                && claveValida(request.getHeader(CABECERA))) {
            var auth = new UsernamePasswordAuthenticationToken("dispositivo", null,
                    List.of(new SimpleGrantedAuthority(AUTORIDAD)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }
}
