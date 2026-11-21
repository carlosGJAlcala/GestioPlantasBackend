package es.uah.huertojpa.seguridad;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/** S6: cálculo del límite de intentos de login, con el reloj a mano. */
class LimiteIntentosLoginTest {
    private static final Instant T0 = Instant.parse("2026-11-14T10:00:00Z");
    private static final String IP = "192.168.1.50", OTRA_IP = "192.168.1.51";

    private final LimiteIntentosLogin limite =
            new LimiteIntentosLogin(5, 20, Duration.ofMinutes(15), Duration.ofMinutes(15));

    private void fallos(String usuario, String ip, int n, Instant cuando) {
        for (int i = 0; i < n; i++) limite.fallo(usuario, ip, cuando);
    }

    @Test
    void cincoFallosBloqueanAlUsuarioSoloDesdeEsaIp() {
        fallos("ana", IP, 4, T0);
        assertTrue(limite.esperaRestante("ana", IP, T0).isZero(), "con 4 todavía se puede");
        limite.fallo("ana", IP, T0);
        assertEquals(Duration.ofMinutes(15), limite.esperaRestante("ana", IP, T0));
        assertEquals(Duration.ofMinutes(5), limite.esperaRestante("ANA ", IP, T0.plus(Duration.ofMinutes(10))),
                "el usuario se compara sin mayúsculas ni espacios");
        // El dueño puede entrar desde otro equipo y otro usuario desde el mismo: no se puede bloquear a nadie a distancia.
        assertTrue(limite.esperaRestante("ana", OTRA_IP, T0).isZero());
        assertTrue(limite.esperaRestante("luis", IP, T0).isZero());
        assertTrue(limite.esperaRestante("ana", IP, T0.plus(Duration.ofMinutes(15))).isZero(), "el bloqueo caduca");
    }

    @Test
    void losFallosViejosNoCuentan() {
        fallos("ana", IP, 4, T0);
        // Fuera de la ventana de 15 min se empieza a contar de nuevo.
        fallos("ana", IP, 4, T0.plus(Duration.ofMinutes(16)));
        assertTrue(limite.esperaRestante("ana", IP, T0.plus(Duration.ofMinutes(16))).isZero());
    }

    @Test
    void unLoginCorrectoBorraLosFallos() {
        fallos("ana", IP, 4, T0);
        limite.exito("ana", IP);
        fallos("ana", IP, 4, T0);
        assertTrue(limite.esperaRestante("ana", IP, T0).isZero());
    }

    @Test
    void elBloqueoSeDuplicaSiSeRepite() {
        fallos("ana", IP, 5, T0);
        Instant t1 = T0.plus(Duration.ofMinutes(15));
        fallos("ana", IP, 5, t1);
        assertEquals(Duration.ofMinutes(30), limite.esperaRestante("ana", IP, t1));
        Instant t2 = t1.plus(Duration.ofMinutes(30));
        fallos("ana", IP, 5, t2);
        assertEquals(Duration.ofHours(1), limite.esperaRestante("ana", IP, t2));
        // Tras un login correcto se vuelve a los 15 min.
        limite.exito("ana", IP);
        Instant t3 = t2.plus(Duration.ofHours(1));
        fallos("ana", IP, 5, t3);
        assertEquals(Duration.ofMinutes(15), limite.esperaRestante("ana", IP, t3));
    }

    @Test
    void elBloqueoNoPasaDeUnDia() {
        Instant t = T0;
        for (int i = 0; i < 15; i++) {
            fallos("ana", IP, 5, t);
            Duration espera = limite.esperaRestante("ana", IP, t);
            assertTrue(espera.compareTo(Duration.ofHours(24)) <= 0, espera.toString());
            t = t.plus(espera);
        }
        fallos("ana", IP, 5, t);
        assertEquals(Duration.ofHours(24), limite.esperaRestante("ana", IP, t));
    }

    @Test
    void muchosUsuariosDesdeLaMismaIpBloqueanLaIp() {
        for (int i = 0; i < 20; i++) limite.fallo("usuario" + i, IP, T0);
        assertFalse(limite.esperaRestante("otro", IP, T0).isZero(), "la IP queda bloqueada para cualquier usuario");
        assertTrue(limite.esperaRestante("otro", OTRA_IP, T0).isZero());
    }

    @Test
    void elAdminDesbloqueaAlUsuario() {
        fallos("ana", IP, 5, T0);
        fallos("ana", OTRA_IP, 5, T0);
        fallos("anabel", IP, 5, T0);
        limite.desbloquearUsuario("Ana");
        assertTrue(limite.esperaRestante("ana", IP, T0).isZero());
        assertTrue(limite.esperaRestante("ana", OTRA_IP, T0).isZero());
        assertFalse(limite.esperaRestante("anabel", IP, T0).isZero(), "solo ese usuario");
    }
}
