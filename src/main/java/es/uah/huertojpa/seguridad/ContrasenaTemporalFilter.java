package es.uah.huertojpa.seguridad;

import es.uah.huertojpa.persona.dominio.entidades.Persona;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * S8b – Con una contraseña temporal (puesta por el admin) el JWT solo sirve para cambiarla.
 * <p>
 * Va justo después de {@code JwtAuthenticationFilter}: si la persona del token tiene la marca
 * {@code debe_cambiar_contrasena}, cualquier petición que no sea {@code PUT /persona/contrasena} (o leer su propia
 * ficha) recibe <b>403</b> con {@code {"mensaje":…,"debeCambiarContrasena":true}}. Si además la temporal ha caducado
 * ({@link ContrasenaTemporal}), <b>401</b>: la sesión ya no vale y el login tampoco la dejará entrar.
 * <p>
 * Las peticiones sin JWT (ESP32 con su clave, login) no se tocan.
 */
@Component
public class ContrasenaTemporalFilter extends OncePerRequestFilter {

    static final String MENSAJE = "Tienes que cambiar la contraseña temporal que te ha puesto el administrador antes de seguir.";

    private final ContrasenaTemporal temporal;

    public ContrasenaTemporalFilter(ContrasenaTemporal temporal) {
        this.temporal = temporal;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // El login nunca se bloquea: el front manda el token guardado en todas las peticiones, también al volver a entrar.
        if (auth != null && auth.getPrincipal() instanceof Persona persona && temporal.pendiente(persona)
                && !esLogin(DispositivoKeyFilter.ruta(request))) {
            if (temporal.caducada(persona, LocalDateTime.now())) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            if (!permitida(request, persona)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.getWriter().write("{\"mensaje\":\"" + MENSAJE + "\",\"debeCambiarContrasena\":true}");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    static boolean esLogin(String ruta) {
        return ruta.equals("/auth/login") || ruta.equals("/auth/register");
    }

    /** Lo único que se puede hacer con la temporal: cambiarla, leer la propia ficha y las páginas de error. */
    static boolean permitida(HttpServletRequest request, Persona persona) {
        String ruta = DispositivoKeyFilter.ruta(request);
        String metodo = request.getMethod();
        if (HttpMethod.OPTIONS.matches(metodo) || ruta.equals("/error")) return true;
        if (HttpMethod.PUT.matches(metodo) && ruta.equals("/persona/contrasena")) return true;
        return HttpMethod.GET.matches(metodo) && persona.getUsuario() != null
                && ruta.equals("/persona/userName/" + persona.getUsuario());
    }
}
