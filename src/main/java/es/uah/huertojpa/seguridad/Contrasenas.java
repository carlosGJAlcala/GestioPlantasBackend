package es.uah.huertojpa.seguridad;

import com.google.common.hash.Hashing;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

/**
 * S2 – Contraseñas con BCrypt (con sal y coste ajustable) en lugar de SHA-256 sin sal.
 * <p>
 * El front sigue mandando en el login el SHA-256 en hex de lo que escribe el usuario (así no cambia el contrato del
 * API ni viaja la contraseña en claro por el HTTP plano de la Raspberry). Lo que se guarda ahora es
 * {@code BCrypt(sha256)}: si alguien se lleva la tabla {@code persona}, ya no tiene directamente el valor con el que
 * se entra (antes el hash guardado <b>era</b> la credencial) ni puede usar tablas precalculadas.
 * <p>
 * Las filas antiguas (SHA-256 en hex) se aceptan y se convierten: en el primer login correcto
 * ({@code LoginServiceImpl}) y al arrancar ({@link MigracionContrasenas}).
 */
@Component
public class Contrasenas {
    private static final Pattern BCRYPT = Pattern.compile("^\\$2[aby]?\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    private static final Pattern SHA256_HEX = Pattern.compile("^[0-9a-fA-F]{64}$");

    private final PasswordEncoder encoder;
    /** Hash de algo que nadie conoce, para gastar el mismo tiempo cuando el usuario no existe. */
    private final String hashFicticio;

    public Contrasenas(PasswordEncoder encoder) {
        this.encoder = encoder;
        this.hashFicticio = encoder.encode("usuario-inexistente");
    }

    /** SHA-256 en hex, lo mismo que hace el front ({@code security/sha256.js}) antes de mandar la contraseña. */
    public static String sha256(String textoPlano) {
        return Hashing.sha256().hashString(textoPlano, StandardCharsets.UTF_8).toString();
    }

    public static boolean esBcrypt(String guardado) {
        return guardado != null && BCRYPT.matcher(guardado).matches();
    }

    /** Lo que manda el front: 64 caracteres hexadecimales. */
    public static boolean pareceSha256(String valor) {
        return valor != null && SHA256_HEX.matcher(valor).matches();
    }

    /** Valor a guardar en {@code persona.passwordsha256} a partir del SHA-256 que manda el front. */
    public String cifrar(String hashCliente) {
        return encoder.encode(hashCliente);
    }

    /**
     * ¿El valor que manda el front corresponde a lo guardado? Admite BCrypt y, para filas antiguas, el SHA-256 tal
     * cual (comparado en tiempo constante). Usuario inexistente ({@code guardado == null}) ⇒ {@code false}, tardando
     * lo mismo que una comprobación real para no revelar qué usuarios existen.
     */
    public boolean coincide(String hashCliente, String guardado) {
        if (hashCliente == null || hashCliente.isEmpty()) {
            return false;
        }
        if (guardado == null || guardado.isEmpty()) {
            encoder.matches(hashCliente, hashFicticio);
            return false;
        }
        if (esBcrypt(guardado)) {
            return encoder.matches(hashCliente, guardado);
        }
        return MessageDigest.isEqual(hashCliente.getBytes(StandardCharsets.UTF_8),
                guardado.getBytes(StandardCharsets.UTF_8));
    }
}
