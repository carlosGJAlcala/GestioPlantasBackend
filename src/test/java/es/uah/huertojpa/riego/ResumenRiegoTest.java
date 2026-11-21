package es.uah.huertojpa.riego;

import es.uah.huertojpa.riego.aplicacion.ResumenRiego;
import es.uah.huertojpa.riego.dominio.MotivoRiego;
import es.uah.huertojpa.riego.dominio.ResultadoRiego;
import es.uah.huertojpa.riego.dominio.Riego;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResumenRiegoTest {
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 4, 12, 0);

    private static Riego riego(LocalDateTime ts, int pulsos, Float hAntes, Float hDespues,
                               Float nAntes, Float nDespues, ResultadoRiego resultado) {
        Riego r = new Riego();
        r.setMacetaId(1);
        r.setTs(ts);
        r.setPulsos(pulsos);
        r.setDuracionMs(pulsos * 4000);
        r.setHumedadAntes(hAntes);
        r.setHumedadDespues(hDespues);
        r.setNivelDepositoAntes(nAntes);
        r.setNivelDepositoDespues(nDespues);
        r.setMotivo(MotivoRiego.AUTO);
        r.setResultado(resultado);
        return r;
    }

    @Test
    void calculaMetricasBasicas() {
        Riego ok = riego(AHORA.minusHours(30), 2, 20f, 45f, 80f, 75f, ResultadoRiego.OK);
        Riego fallido = riego(AHORA.minusHours(6), 3, 20f, 21f, 75f, 75f, ResultadoRiego.LIMITE_PULSOS);
        Riego vacio = riego(AHORA.minusHours(2), 0, 18f, null, 8f, null, ResultadoRiego.DEPOSITO_VACIO);

        ResumenRiego r = ResumenRiego.calcular(List.of(ok, fallido, vacio), fallido, AHORA, 7, 20f);

        assertEquals(2, r.riegos());
        assertEquals(0.29, r.riegosPorDia());
        assertEquals(5, r.pulsosTotales());
        assertEquals(20.0, r.segundosBomba());
        assertEquals(13.0, r.incrementoHumedadMedio());
        assertEquals(1, r.riegosFallidos());
        assertEquals(1, r.riegosLimitePulsos());
        assertEquals(1, r.bloqueadosDepositoVacio());
        assertEquals(5.0, r.consumoDepositoPuntos());
        assertEquals(1.0, r.litrosEstimados()); // 5 % de 20 L
        assertEquals(6.0, r.horasDesdeUltimoRiego());
    }

    @Test
    void ignoraSubidasDeNivelYSinCapacidadNoHayLitros() {
        Riego ruido = riego(AHORA.minusHours(1), 1, 30f, 40f, 50f, 53f, ResultadoRiego.OK);
        ResumenRiego r = ResumenRiego.calcular(List.of(ruido), ruido, AHORA, 1, null);
        assertEquals(0.0, r.consumoDepositoPuntos());
        assertNull(r.litrosEstimados());
        assertEquals(0, r.riegosFallidos());
    }

    @Test
    void sinRiegos() {
        ResumenRiego r = ResumenRiego.calcular(List.of(), null, AHORA, 7, 20f);
        assertEquals(0, r.riegos());
        assertNull(r.incrementoHumedadMedio());
        assertNull(r.ultimoRiego());
        assertNull(r.horasDesdeUltimoRiego());
    }

    @Test
    void cuentaRiegosManualesYDescartados() {
        Riego manual = riego(AHORA.minusHours(3), 1, 40f, 46f, 70f, 68f, ResultadoRiego.OK);
        manual.setMotivo(MotivoRiego.MANUAL);
        Riego descartado = riego(AHORA.minusHours(1), 0, 80f, null, 68f, null, ResultadoRiego.HUMEDAD_MAXIMA);
        descartado.setMotivo(MotivoRiego.MANUAL);
        Riego auto = riego(AHORA.minusHours(20), 2, 20f, 45f, 75f, 70f, ResultadoRiego.OK);

        ResumenRiego r = ResumenRiego.calcular(List.of(auto, manual, descartado), manual, AHORA, 7, null);

        assertEquals(2, r.riegos(), "el descartado no activó la bomba");
        assertEquals(1, r.riegosManuales());
        assertEquals(1, r.manualesDescartados());
    }
}
