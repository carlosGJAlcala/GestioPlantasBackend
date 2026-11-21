package es.uah.huertojpa.seguridad;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/** S2: BCrypt sobre el SHA-256 que manda el front, aceptando las filas antiguas. */
class ContrasenasTest {
    private final Contrasenas contrasenas = new Contrasenas(new BCryptPasswordEncoder(4));
    private final String sha = Contrasenas.sha256("tomates");

    @Test
    void sha256IgualQueElFront() {
        // printf '%s' 'tomates' | sha256sum (mismo resultado que security/sha256.js)
        assertEquals(64, sha.length());
        assertTrue(Contrasenas.pareceSha256(sha));
        assertFalse(Contrasenas.pareceSha256("tomates"));
        assertFalse(Contrasenas.pareceSha256(null));
    }

    @Test
    void cifraConSalYComprueba() {
        String a = contrasenas.cifrar(sha), b = contrasenas.cifrar(sha);
        assertTrue(Contrasenas.esBcrypt(a));
        assertNotEquals(a, b, "con sal, el mismo valor da hashes distintos");
        assertTrue(contrasenas.coincide(sha, a));
        assertTrue(contrasenas.coincide(sha, b));
        assertFalse(contrasenas.coincide(Contrasenas.sha256("otra"), a));
        // Lo guardado ya no sirve para entrar (antes el hash guardado era la credencial).
        assertFalse(contrasenas.coincide(a, a));
    }

    @Test
    void aceptaLasFilasAntiguas() {
        assertFalse(Contrasenas.esBcrypt(sha));
        assertTrue(contrasenas.coincide(sha, sha));
        assertFalse(contrasenas.coincide(Contrasenas.sha256("otra"), sha));
    }

    @Test
    void valoresVaciosNuncaCoinciden() {
        assertFalse(contrasenas.coincide(null, sha));
        assertFalse(contrasenas.coincide("", ""));
        assertFalse(contrasenas.coincide(sha, null), "usuario inexistente");
        assertFalse(contrasenas.coincide(sha, ""));
    }
}
