package es.uah.huertojpa.registro.aplicacion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LecturaRegistradorTest {

    @Test
    void valorNumerico() {
        assertEquals(42.5f, LecturaRegistrador.valor("42.5"));
        assertEquals(42.5f, LecturaRegistrador.valor(" 42,5 "));
        assertNull(LecturaRegistrador.valor("nan"));
        assertNull(LecturaRegistrador.valor("NaN"));
        assertNull(LecturaRegistrador.valor(""));
        assertNull(LecturaRegistrador.valor(null));
    }

    @Test
    void magnitudNormalizada() {
        assertEquals("LUZ", LecturaRegistrador.normalizarMagnitud("Luminosidad"));
        assertEquals("HUMEDAD", LecturaRegistrador.normalizarMagnitud("humedad"));
        assertEquals("DESCONOCIDA", LecturaRegistrador.normalizarMagnitud(" "));
    }
}
