package es.uah.huertojpa.maceta;

import es.uah.huertojpa.maceta.dominio.Humedad;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Regresión del bug B2 (media de humedad mal calculada, entrega 2026-11-03). */
class HumedadTest {
    @Test
    void mediaDeTresPlantas() {
        Humedad h = new Humedad();
        h.addHumedadReqPorPlanta(40f, 60f);
        h.addHumedadReqPorPlanta(50f, 70f);
        h.addHumedadReqPorPlanta(60f, 80f);
        assertEquals(3, h.getNumeroPlantas());
        assertEquals(50f, h.getHumedadMediaMinimaRequerida(), 0.001);
        assertEquals(70f, h.getHumedadMediaMaximaRequerida(), 0.001);
    }

    @Test
    void ignoraTiposSinRango() {
        Humedad h = new Humedad();
        h.addHumedadReqPorPlanta(null, 60f);
        h.addHumedadReqPorPlanta(30f, 50f);
        assertEquals(1, h.getNumeroPlantas());
        assertEquals(30f, h.getHumedadMediaMinimaRequerida(), 0.001);
    }
}
