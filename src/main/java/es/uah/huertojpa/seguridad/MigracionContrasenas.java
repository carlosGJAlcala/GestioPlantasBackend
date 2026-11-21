package es.uah.huertojpa.seguridad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * S2 – Al arrancar, convierte a BCrypt las contraseñas que siguen guardadas como SHA-256 sin sal (las de antes de
 * esta versión y las que se metan a mano con {@code bd/02-crear-admin.sql}). Como lo guardado era justo el SHA-256 que
 * manda el front, basta con {@code BCrypt(valor guardado)}: nadie tiene que volver a poner su contraseña.
 * <p>
 * Es idempotente (solo toca filas que no son BCrypt) y se puede desactivar con {@code CONTRASENAS_MIGRAR=false}.
 * Se hace con JDBC y no con Flyway porque PostgreSQL no trae BCrypt sin la extensión {@code pgcrypto}.
 */
@Component
@ConditionalOnProperty(name = "contrasenas.migrar", havingValue = "true", matchIfMissing = true)
public class MigracionContrasenas implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(MigracionContrasenas.class);

    private final JdbcTemplate jdbc;
    private final Contrasenas contrasenas;

    public MigracionContrasenas(JdbcTemplate jdbc, Contrasenas contrasenas) {
        this.jdbc = jdbc;
        this.contrasenas = contrasenas;
    }

    @Override
    public void run(ApplicationArguments args) {
        migrar();
    }

    /** Devuelve cuántas contraseñas ha convertido. */
    public int migrar() {
        List<Map<String, Object>> filas = jdbc.queryForList(
                "SELECT id, passwordsha256 FROM persona WHERE passwordsha256 IS NOT NULL AND passwordsha256 <> ''");
        int convertidas = 0;
        for (Map<String, Object> fila : filas) {
            String guardado = (String) fila.get("passwordsha256");
            if (Contrasenas.esBcrypt(guardado)) {
                continue;
            }
            // «AND passwordsha256 = ?»: si a la vez alguien ha entrado y ya se ha convertido, no se pisa.
            convertidas += jdbc.update("UPDATE persona SET passwordsha256 = ? WHERE id = ? AND passwordsha256 = ?",
                    contrasenas.cifrar(guardado), fila.get("id"), guardado);
        }
        if (convertidas > 0) {
            log.info("S2: {} contraseña(s) convertidas de SHA-256 sin sal a BCrypt", convertidas);
        }
        return convertidas;
    }
}
