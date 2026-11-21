package es.uah.huertojpa.metricas;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SerieLecturasServiceTest {

    @Test
    void eligeElPasoRedondoMasFinoQueNoPasaDe200Puntos() {
        assertEquals(1, SerieLecturasService.elegirPaso(Duration.ofHours(3), null));    // 180 puntos
        assertEquals(10, SerieLecturasService.elegirPaso(Duration.ofHours(24), null));  // 144 puntos
        assertEquals(60, SerieLecturasService.elegirPaso(Duration.ofDays(7), null));    // 168 puntos
        assertEquals(360, SerieLecturasService.elegirPaso(Duration.ofDays(30), null));  // 120 puntos
        assertEquals(720, SerieLecturasService.elegirPaso(Duration.ofDays(90), null));  // 180 puntos
    }

    @Test
    void respetaElPasoPedidoSalvoQueDeDemasiadosPuntos() {
        assertEquals(15, SerieLecturasService.elegirPaso(Duration.ofDays(7), 15));
        // 30 días con paso de 1 min serían 43 200 puntos: se sube al mínimo que da ≤ 2000.
        assertEquals(22, SerieLecturasService.elegirPaso(Duration.ofDays(30), 1));
    }
}
