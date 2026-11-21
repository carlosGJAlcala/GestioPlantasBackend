package es.uah.huertojpa.config;

import es.uah.huertojpa.jwt.JwtAuthenticationFilter;
import es.uah.huertojpa.seguridad.AccesoService;
import es.uah.huertojpa.seguridad.ContrasenaTemporalFilter;
import es.uah.huertojpa.seguridad.DispositivoKeyFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.util.function.BiPredicate;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final DispositivoKeyFilter dispositivoKeyFilter;
    private final ContrasenaTemporalFilter contrasenaTemporalFilter;
    private final AuthenticationProvider authProvider;
    private final AccesoService acceso;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, DispositivoKeyFilter dispositivoKeyFilter,
                          ContrasenaTemporalFilter contrasenaTemporalFilter,
                          AuthenticationProvider authProvider, AccesoService acceso) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.dispositivoKeyFilter = dispositivoKeyFilter;
        this.contrasenaTemporalFilter = contrasenaTemporalFilter;
        this.authProvider = authProvider;
        this.acceso = acceso;
    }

    /** S4: administrador = persona con fila en la tabla administrador (mismo criterio que el login). */
    private AuthorizationManager<RequestAuthorizationContext> soloAdmin() {
        return (auth, ctx) -> new AuthorizationDecision(acceso.esAdmin(auth.get()));
    }

    /**
     * S4b: la variable de ruta {@code var} es el id de un recurso que debe pertenecer al usuario del token
     * (o ser administrador). Id no numérico ⇒ denegado.
     */
    private AuthorizationManager<RequestAuthorizationContext> propietario(String var,
                                                                          BiPredicate<Authentication, Integer> regla) {
        return (auth, ctx) -> {
            Integer id;
            try {
                id = Integer.valueOf(ctx.getVariables().get(var));
            } catch (RuntimeException e) {
                id = null;
            }
            return new AuthorizationDecision(id != null && regla.test(auth.get(), id));
        };
    }

    @Bean
    public SecurityFilterChain securityFilterCahin(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authRequest -> {
                    authRequest.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .requestMatchers("/auth/login", "/auth/register").permitAll();
                    if (dispositivoKeyFilter.modoCompatibilidad()) {
                        // Sin DEVICE_API_KEY: comportamiento anterior (endpoints del ESP32 abiertos).
                        authRequest.requestMatchers("/auth/**").permitAll();
                    } else {
                        // S3: ESP32 con X-Device-Key válida, o usuario con JWT (el front usa PUT /auth/sensor).
                        authRequest.requestMatchers("/auth/**").authenticated();
                    }
                    // Página de error de Spring (sendError de un 403/404): sin esto el usuario recibiría un 401 engañoso.
                    authRequest.requestMatchers("/error").permitAll();
                    // S4: listados globales de personas y gestión de usuarios/administradores, solo administradores.
                    authRequest.requestMatchers("/administrador", "/administrador/**", "/usuario", "/usuario/**").access(soloAdmin())
                            .requestMatchers(HttpMethod.GET, "/persona", "/persona/{id}").access(soloAdmin())
                            .requestMatchers(HttpMethod.DELETE, "/persona/{id}").access(soloAdmin())
                            // S8: restablecer la contraseña de otra persona (la propia se cambia en /persona/contrasena).
                            .requestMatchers(HttpMethod.PUT, "/persona/{id}/contrasena").access(soloAdmin())
                            // S9: cerrar las sesiones de otra persona (las propias: POST /persona/sesiones/cerrar).
                            .requestMatchers(HttpMethod.POST, "/persona/{id}/sesiones/cerrar").access(soloAdmin())
                            // Alta de personas desde la web: solo admin (el alta pública es /auth/register).
                            .requestMatchers(HttpMethod.POST, "/persona").access(soloAdmin());
                    // S4b: listados de TODOS los recursos (pantallas de administración del front), solo administradores.
                    authRequest.requestMatchers(HttpMethod.GET, "/maceta", "/huerto", "/planta", "/depositoagua", "/sensor",
                                    "/macetahassensor", "/macetahassensor/sensor/**", "/planta/estado/**",
                                    "/sensor/nombre/**", "/fechahora", "/fechahora/**").access(soloAdmin())
                            .requestMatchers("/huertoHasUsuario", "/huertoHasUsuario/**").access(soloAdmin())
                            .requestMatchers(HttpMethod.POST, "/fechahorahassensor").access(soloAdmin())
                            // El catálogo de tipos de planta es común a todos: lo lee cualquiera, lo cambia el admin.
                            .requestMatchers(HttpMethod.POST, "/tipoPlanta", "/tipoPlanta/**").access(soloAdmin())
                            .requestMatchers(HttpMethod.PUT, "/tipoPlanta", "/tipoPlanta/**").access(soloAdmin())
                            .requestMatchers(HttpMethod.DELETE, "/tipoPlanta", "/tipoPlanta/**").access(soloAdmin());
                    // S4b: recursos por id, solo si cuelgan de un huerto del usuario. Las escrituras con el id en el
                    // cuerpo (POST/PUT) se comprueban en cada controlador con AccesoService.
                    authRequest.requestMatchers("/maceta/{id}").access(propietario("id", acceso::puedeAccederMaceta))
                            .requestMatchers("/maceta/huerto/{id}").access(propietario("id", acceso::puedeAccederHuerto))
                            .requestMatchers("/macetahassensor/maceta/{id}").access(propietario("id", acceso::puedeAccederMaceta))
                            .requestMatchers("/huerto/{id}").access(propietario("id", acceso::puedeAccederHuerto))
                            .requestMatchers("/depositoagua/huerto/{id}").access(propietario("id", acceso::puedeAccederHuerto))
                            .requestMatchers("/depositoagua/{id}").access(propietario("id", acceso::puedeAccederDeposito))
                            .requestMatchers("/planta/macetaid/{macetaid}").access(propietario("macetaid", acceso::puedeAccederMaceta))
                            .requestMatchers("/planta/{id}").access(propietario("id", acceso::puedeAccederPlanta))
                            // W12b: «/sensor/uso» encaja con «/sensor/{id}»; va antes y filtra por usuario en el controlador.
                            .requestMatchers(HttpMethod.GET, "/sensor/uso").authenticated()
                            .requestMatchers("/sensor/{id}").access(propietario("id", acceso::puedeAccederSensor))
                            .requestMatchers("/lectura/maceta/{id}").access(propietario("id", acceso::puedeAccederMaceta))
                            .requestMatchers("/lectura/deposito/huerto/{id}").access(propietario("id", acceso::puedeAccederHuerto))
                            .requestMatchers("/alertas/maceta/{id}").access(propietario("id", acceso::puedeAccederMaceta))
                            .requestMatchers("/alertas/{id}/silenciar").access(propietario("id", acceso::puedeAccederAlerta))
                            .anyRequest().authenticated();
                })
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Sin token (o token caducado) ⇒ 401 en lugar del 403 genérico: el front puede volver al login.
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authenticationProvider(authProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(dispositivoKeyFilter, JwtAuthenticationFilter.class)
                // S8b: con una contraseña temporal pendiente, el JWT solo sirve para cambiarla.
                .addFilterAfter(contrasenaTemporalFilter, DispositivoKeyFilter.class)
                .build();
    }
}
