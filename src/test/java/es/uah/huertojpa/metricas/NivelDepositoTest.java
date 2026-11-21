package es.uah.huertojpa.metricas;

import es.uah.huertojpa.registro.dominio.entities.Lectura;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** A2 – Nivel y consumo del depósito a partir del ultrasonidos. */
class NivelDepositoTest {
    static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 13, 12, 0);

    static Lectura n(float nivel, int minutosAtras) {
        return new Lectura(1, null, "DEPOSITO", nivel, AHORA.minusMinutes(minutosAtras));
    }

    @Test
    void porcentajeComoElFirmware() {
        assertEquals(100f, NivelDeposito.porcentaje(0f, 20f));
        assertEquals(25f, NivelDeposito.porcentaje(15f, 20f));
        assertEquals(0f, NivelDeposito.porcentaje(22f, 20f));     // algo más que la altura: vacío
        assertNull(NivelDeposito.porcentaje(-1f, 20f));           // sin eco
        assertNull(NivelDeposito.porcentaje(30f, 20f));           // > 1,2 × altura: lectura imposible
        assertNull(NivelDeposito.porcentaje(5f, null));           // depósito sin altura configurada
    }

    @Test
    void sinLecturasNoHayDatos() {
        assertSame(NivelDeposito.SIN_DATOS, NivelDeposito.calcular(List.of(), AHORA));
    }

    @Test
    void elNivelEsLaMedianaYNoSeFiaDeUnPico() {
        NivelDeposito d = NivelDeposito.calcular(List.of(n(40, 4), n(41, 3), n(95, 2), n(39, 1), n(40, 0)), AHORA);
        assertEquals(40f, d.nivel());
        assertEquals(0L, d.minutosDesdeLectura());
        assertNull(d.consumoPorDia()); // menos de 6 horas de datos
    }

    @Test
    void consumoDesdeElUltimoRellenado() {
        List<Lectura> l = new ArrayList<>();
        // Antes del rellenado bajaba muy deprisa (no debe contar)…
        for (int h = 40; h > 24; h--) l.add(n(60 - (40 - h) * 3, h * 60));
        // …se rellena hace 24 h hasta el 90 % y baja 1 punto cada 2 h (12 puntos/día).
        for (int h = 24; h >= 0; h--) l.add(n(90 - (24 - h) * 0.5f, h * 60));
        NivelDeposito d = NivelDeposito.calcular(l, AHORA);
        assertEquals(78f, d.nivel());
        assertEquals(12.0, d.consumoPorDia(), 0.01);
        assertEquals((78 - 10) / 12.0, d.diasHastaVacio(), 0.01);
    }

    @Test
    void sinConsumoSiNoBaja() {
        List<Lectura> l = new ArrayList<>();
        for (int h = 24; h >= 0; h--) l.add(n(h % 2 == 0 ? 70 : 71, h * 60));
        NivelDeposito d = NivelDeposito.calcular(l, AHORA);
        assertNull(d.consumoPorDia());
        assertNull(d.diasHastaVacio());
    }

    @Test
    void pordebajoDelMinimoNoQuedanDias() {
        List<Lectura> l = new ArrayList<>();
        for (int h = 12; h >= 0; h--) l.add(n(20 - (12 - h), h * 60));
        NivelDeposito d = NivelDeposito.calcular(l, AHORA);
        assertEquals(8f, d.nivel());
        assertEquals(24.0, d.consumoPorDia(), 0.01);
        assertEquals(0.0, d.diasHastaVacio());
    }

    // --- A4: litros ---

    @Test
    void litrosConLaCapacidad() {
        List<Lectura> l = new ArrayList<>();
        for (int h = 12; h >= 0; h--) l.add(n(20 - (12 - h), h * 60));   // 24 puntos/día
        NivelDeposito d = NivelDeposito.calcular(l, AHORA, 5f, AHORA.minusMinutes(5));
        assertEquals(5f, d.capacidadLitros());
        assertEquals(0.4, d.litros(), 0.001);              // 8 % de 5 L
        assertEquals(1.2, d.consumoLitrosPorDia(), 0.001); // 24 % de 5 L
        // Sin capacidad (o a 0) no se inventan litros.
        assertNull(NivelDeposito.calcular(l, AHORA, null, null).litros());
        assertNull(NivelDeposito.calcular(l, AHORA, 0f, null).capacidadLitros());
    }

    // --- A5: bajada sin riegos ---

    /** Lecturas cada 5 min durante las últimas `horas` horas, bajando `puntosPorHora` desde `inicio`. */
    static List<Lectura> bajando(float inicio, float puntosPorHora, int horas) {
        List<Lectura> l = new ArrayList<>();
        for (int m = horas * 60; m >= 0; m -= 5) l.add(n(inicio - puntosPorHora * (horas * 60 - m) / 60f, m));
        return l;
    }

    @Test
    void bajaSinRiegosEsPosibleFuga() {
        NivelDeposito d = NivelDeposito.calcular(bajando(80, 3, 6), AHORA, 10f, AHORA.minusHours(20));
        assertTrue(d.posibleFuga());
        // De 0 a 5 h 50 min (los 10 min finales no cuentan): medias en el min 27,5 y en el 325 ⇒ 4,96 h.
        assertEquals(4.96, d.horasSinRiego(), 0.01);
        assertEquals(14.88, d.perdidaSinRiego(), 0.05);          // 3 pts/h × 4,96 h
    }

    @Test
    void siHaRegadoSoloCuentaDesdeElRiego() {
        // Mismo descenso, pero hubo un riego hace 2 h: quedan menos de 3 h para medir.
        NivelDeposito d = NivelDeposito.calcular(bajando(80, 3, 6), AHORA, 10f, AHORA.minusHours(2));
        assertNull(d.perdidaSinRiego());
        assertFalse(d.posibleFuga());
        // Riego hace 5 h: se mide desde el riego + 15 min y se ve la bajada posterior (≈ 3,8 h).
        NivelDeposito d2 = NivelDeposito.calcular(bajando(80, 3, 6), AHORA, 10f, AHORA.minusHours(5));
        assertNotNull(d2.perdidaSinRiego());
        assertTrue(d2.horasSinRiego() >= 3);
    }

    @Test
    void nivelEstableORellenadoNoEsFuga() {
        List<Lectura> estable = new ArrayList<>();
        for (int m = 360; m >= 0; m -= 5) estable.add(n(m % 10 == 0 ? 60 : 62, m)); // ruido de ±1 cm
        NivelDeposito d = NivelDeposito.calcular(estable, AHORA, 10f, null);
        assertNotNull(d.perdidaSinRiego());
        assertTrue(d.perdidaSinRiego() < 1);
        assertFalse(d.posibleFuga());

        // Bajaba deprisa, pero lo rellenaron hace 2 h: lo de antes no cuenta y lo de después es poco.
        List<Lectura> l = new ArrayList<>();
        for (int m = 360; m > 120; m -= 5) l.add(n(40 - (360 - m) / 20f, m));
        for (int m = 120; m >= 0; m -= 5) l.add(n(95, m));
        assertNull(NivelDeposito.calcular(l, AHORA, 10f, null).perdidaSinRiego());
    }
}
