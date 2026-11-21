package es.uah.huertojpa.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

/**
 * S4 – Todos los endpoints con la variable de ruta {@code {userName}} ({@code /maceta/userName/{userName}},
 * {@code /planta/userName/...}, {@code DELETE /persona/userName/...}, etc.) solo los puede usar ese mismo usuario o un
 * administrador. Antes cualquier usuario autenticado podía pedir los datos de otro cambiando la URL.
 */
@Component
public class PropietarioInterceptor implements HandlerInterceptor {
    private final AccesoService acceso;

    public PropietarioInterceptor(AccesoService acceso) {
        this.acceso = acceso;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Object vars = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (vars instanceof Map<?, ?> mapa && mapa.containsKey("userName")) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (!acceso.puedeAccederUsuario(auth, String.valueOf(mapa.get("userName")))) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                return false;
            }
        }
        return true;
    }
}
