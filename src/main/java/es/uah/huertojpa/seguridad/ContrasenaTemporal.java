package es.uah.huertojpa.seguridad;

import es.uah.huertojpa.persona.dominio.entidades.Persona;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * S8b – Reglas de la contraseña temporal que pone el administrador ({@code PUT /persona/{id}/contrasena}).
 * <ul>
 *   <li><b>Caduca</b>: si la persona no entra y la cambia en {@code contrasenas.temporal-validez} (3 días por
 *   defecto), ya no sirve para entrar y hay que pedir otra. Una temporal que se queda años en un chat o en un papel
 *   es una contraseña que no controla nadie.</li>
 *   <li><b>Solo sirve para cambiarla</b>: con ella el backend no deja hacer nada más (lo aplica
 *   {@link ContrasenaTemporalFilter}). Antes solo lo imponía el front, y con el token en la mano se podía usar todo
 *   el API sin cambiarla.</li>
 * </ul>
 */
@Component
public class ContrasenaTemporal {

    private final Duration validez;

    public ContrasenaTemporal(@Value("${contrasenas.temporal-validez:P3D}") Duration validez) {
        this.validez = validez;
    }

    public Duration validez() {
        return validez;
    }

    /** ¿Tiene una contraseña temporal pendiente de cambiar? */
    public boolean pendiente(Persona persona) {
        return persona != null && persona.tieneQueCambiarContrasena();
    }

    /**
     * ¿Tiene una contraseña temporal y ya ha pasado su plazo? Sin fecha del restablecimiento (no debería ocurrir:
     * lo pone siempre {@code restablecerContrasena}) no se puede saber y se da por buena.
     */
    public boolean caducada(Persona persona, LocalDateTime ahora) {
        return pendiente(persona) && persona.getContrasenaCambiada() != null
                && persona.getContrasenaCambiada().plus(validez).isBefore(ahora);
    }
}
