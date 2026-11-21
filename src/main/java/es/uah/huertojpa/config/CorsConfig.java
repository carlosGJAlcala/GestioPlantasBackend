package es.uah.huertojpa.config;

import es.uah.huertojpa.seguridad.PropietarioInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
// S5 (entrega 2026-11-09): sin @EnableWebMvc, que desactivaba la autoconfiguración de Spring MVC y hacía que las
// fechas salieran como arrays [2026,11,8,10,0]. Ahora Jackson lo configura Spring Boot (ver JacksonConfig).
public class CorsConfig implements WebMvcConfigurer {
    private final PropietarioInterceptor propietarioInterceptor;
    private final String[] origenes;

    public CorsConfig(PropietarioInterceptor propietarioInterceptor,
                      @Value("${cors.allowed-origins:*}") String[] origenes) {
        this.propietarioInterceptor = propietarioInterceptor;
        this.origenes = origenes;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins(origenes).allowedMethods("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(propietarioInterceptor);
    }
}
