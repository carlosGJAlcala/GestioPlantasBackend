package es.uah.huertojpa.metricas;

import es.uah.huertojpa.metricas.MetricasMaceta.Rango;
import es.uah.huertojpa.registro.dominio.entities.Lectura;
import es.uah.huertojpa.riego.dominio.EstadoOrden;
import es.uah.huertojpa.riego.dominio.MotivoRiego;
import es.uah.huertojpa.riego.dominio.OrdenRiego;
import es.uah.huertojpa.riego.dominio.ResultadoRiego;
import es.uah.huertojpa.riego.dominio.Riego;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MetricasMacetaTest {
    static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 6, 12, 0);
    static final Rango HUMEDAD = new Rango(40f, 70f);

    static Lectura l(LocalDateTime ts, float v) {
        return new Lectura(1, 1, "HUMEDAD", v, ts);
    }

    /** Una lectura cada 10 min durante {@code horas} horas, perdiendo {@code tasa} puntos por hora. */
    static List<Lectura> secado(float inicial, double tasa, int horas) {
        List<Lectura> r = new ArrayList<>();
        LocalDateTime t0 = AHORA.minusHours(horas);
        for (int m = 0; m <= horas * 60; m += 10) {
            r.add(l(t0.plusMinutes(m), (float) (inicial - tasa * m / 60.0)));
        }
        return r;
    }

    static MetricasMaceta calcular(List<Lectura> humedad, List<Riego> riegos, List<OrdenRiego> ordenes) {
        LocalDateTime ultima = humedad.isEmpty() ? null : humedad.get(humedad.size() - 1).getTs();
        return MetricasMaceta.calcular(24, AHORA, humedad, List.of(), List.of(), HUMEDAD, null, null,
                riegos, ordenes, ultima);
    }

    @Test
    void estimaTasaDeSecadoYProximoRiego() {
        // 70 % → pierde 1 pt/h durante 10 h → ahora 60 %; faltan 20 pts hasta el mínimo (40) ⇒ 20 h.
        MetricasMaceta m = calcular(secado(70, 1.0, 10), List.of(), List.of());
        assertEquals(1.0, m.tasaSecadoPorHora(), 0.01);
        assertEquals(60f, m.humedadActual(), 0.01);
        assertEquals(20.0, m.horasHastaProximoRiego(), 0.1);
        assertEquals(AHORA.plusHours(20), m.proximoRiegoEstimado().withSecond(0).withNano(0));
        assertEquals(100.0, m.porcentajeHumedadEnRango());
        assertTrue(m.placaConectada());
    }

    @Test
    void ignoraLasLecturasAnterioresAlUltimoRiego() {
        // Antes del riego (hace 5 h) la tierra subía; después baja 2 pts/h. Solo debe contar el tramo posterior.
        List<Lectura> h = new ArrayList<>();
        for (int i = 10; i > 5; i--) h.add(l(AHORA.minusHours(i), 20 + (10 - i) * 5));
        h.addAll(secado(75, 2.0, 4));
        Riego riego = new Riego();
        riego.setTs(AHORA.minusHours(5));
        riego.setPulsos(3);
        riego.setDuracionMs(3000);
        riego.setMotivo(MotivoRiego.AUTO);
        riego.setResultado(ResultadoRiego.OK);
        MetricasMaceta m = calcular(h, List.of(riego), List.of());
        assertEquals(2.0, m.tasaSecadoPorHora(), 0.05);
    }

    @Test
    void sinTasaSiLaHumedadNoBaja() {
        MetricasMaceta m = calcular(secado(55, 0.0, 6), List.of(), List.of());
        assertNull(m.tasaSecadoPorHora());
        assertNull(m.proximoRiegoEstimado());
    }

    @Test
    void porDebajoDelMinimoTocaRegarYa() {
        MetricasMaceta m = calcular(secado(45, 1.0, 8), List.of(), List.of());
        assertEquals(37f, m.humedadActual(), 0.01);
        assertEquals(AHORA, m.proximoRiegoEstimado());
        assertTrue(m.horasEstresHidrico() > 0);
    }

    @Test
    void losHuecosDeLaPlacaNoCuentanComoTiempo() {
        // Dos lecturas separadas 6 h: la primera solo vale 30 min (MAX_HUECO); la última, hasta ahora (30 min).
        List<Lectura> h = List.of(l(AHORA.minusHours(6).minusMinutes(30), 30), l(AHORA.minusMinutes(30), 50));
        MetricasMaceta m = calcular(h, List.of(), List.of());
        assertEquals(1.0, m.horasConDatos(), 0.01);
        assertEquals(0.5, m.horasEstresHidrico(), 0.01);
        assertEquals(50.0, m.porcentajeHumedadEnRango());
        assertEquals(30L, m.minutosDesdeUltimaLectura());
        assertTrue(m.placaConectada());
    }

    @Test
    void latenciaYCaducidadDeOrdenes() {
        OrdenRiego entregada = new OrdenRiego();
        entregada.setCreada(AHORA.minusHours(2));
        entregada.setEntregada(AHORA.minusHours(2).plusSeconds(40));
        entregada.setEstado(EstadoOrden.ENTREGADA);
        OrdenRiego caducada = new OrdenRiego();
        caducada.setCreada(AHORA.minusHours(1));
        caducada.setEstado(EstadoOrden.CADUCADA);
        OrdenRiego pendiente = new OrdenRiego();
        pendiente.setCreada(AHORA.minusMinutes(1));
        pendiente.setEstado(EstadoOrden.PENDIENTE);

        MetricasMaceta m = calcular(List.of(), List.of(), List.of(entregada, caducada, pendiente));
        assertEquals(3, m.ordenesRiego());
        assertEquals(40.0, m.segundosMediosEntregaOrden());
        assertEquals(50.0, m.porcentajeOrdenesCaducadas());
        assertFalse(m.placaConectada());
        assertNull(m.porcentajeHumedadEnRango());
    }

    @Test
    void horasDeLuzPorDia() {
        // 24 h de datos: 10 h por encima del mínimo de luz (10000 lx).
        List<Lectura> luz = new ArrayList<>();
        LocalDateTime t0 = AHORA.minusHours(24);
        for (int m = 0; m < 24 * 60; m += 10) {
            luz.add(new Lectura(2, 1, "LUZ", m < 10 * 60 ? 20000f : 50f, t0.plusMinutes(m)));
        }
        MetricasMaceta r = MetricasMaceta.calcular(24, AHORA, List.of(), List.of(), luz,
                HUMEDAD, null, new Rango(10000f, null), List.of(), List.of(), null);
        assertEquals(10.0, r.horasLuzSuficientePorDia(), 0.01);
    }

    @Test
    void temperaturaConUnaSolaLecturaRecienLlegada() {
        List<Lectura> t = List.of(new Lectura(3, 1, "TEMPERATURA", 22.4f, AHORA));
        MetricasMaceta r = MetricasMaceta.calcular(24, AHORA, List.of(), t, List.of(),
                HUMEDAD, new Rango(15f, 30f), null, List.of(), List.of(), AHORA);
        assertEquals(22.4, r.temperaturaMedia(), 0.01);
    }
}
