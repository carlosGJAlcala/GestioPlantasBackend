package es.uah.huertojpa.alertas;

import es.uah.huertojpa.metricas.MetricasMaceta;
import es.uah.huertojpa.metricas.MetricasMaceta.Rango;
import es.uah.huertojpa.metricas.NivelDeposito;
import es.uah.huertojpa.registro.dominio.entities.Lectura;
import es.uah.huertojpa.riego.dominio.ResultadoRiego;
import es.uah.huertojpa.riego.dominio.Riego;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Criterios del estado global de la maceta (los mismos para el panel y para las alertas). */
class EstadoMacetaTest {
    static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 12, 0);
    static final Rango HUMEDAD = new Rango(60f, 80f), TEMPERATURA = new Rango(15f, 30f);

    static Lectura l(String magnitud, float v, int minutosAtras) {
        return new Lectura(1, 1, magnitud, v, AHORA.minusMinutes(minutosAtras));
    }

    static Riego riego(ResultadoRiego resultado, int pulsos, Float antes, Float despues) {
        Riego r = new Riego();
        r.setTs(AHORA.minusHours(1));
        r.setPulsos(pulsos);
        r.setResultado(resultado);
        r.setHumedadAntes(antes);
        r.setHumedadDespues(despues);
        return r;
    }

    static EstadoMaceta estado(float humedad, float temperatura, int minutosAtras, List<Riego> riegos) {
        List<Lectura> h = List.of(l("HUMEDAD", humedad, minutosAtras));
        List<Lectura> t = List.of(l("TEMPERATURA", temperatura, minutosAtras));
        MetricasMaceta m = MetricasMaceta.calcular(24, AHORA, h, t, List.of(), HUMEDAD, TEMPERATURA, null,
                riegos, List.of(), AHORA.minusMinutes(minutosAtras));
        return m.estado();
    }

    static List<TipoAlerta> tipos(EstadoMaceta e) {
        return e.condiciones().stream().map(Condicion::tipo).toList();
    }

    @Test
    void todoBienSinCondiciones() {
        EstadoMaceta e = estado(70, 20, 1, List.of(riego(ResultadoRiego.OK, 3, 55f, 68f)));
        assertEquals(NivelAlerta.OK, e.nivel());
        assertEquals("Todo bien", e.texto());
        assertTrue(e.condiciones().isEmpty());
    }

    @Test
    void sinLecturasEsNeutro() {
        MetricasMaceta m = MetricasMaceta.calcular(24, AHORA, List.of(), List.of(), List.of(), HUMEDAD, null, null,
                List.of(), List.of(), null);
        assertEquals(NivelAlerta.NEUTRO, m.estado().nivel());
    }

    @Test
    void humedadYTemperaturaFueraDeRango() {
        EstadoMaceta e = estado(45, 35, 1, List.of());
        assertEquals(List.of(TipoAlerta.TIERRA_SECA, TipoAlerta.TEMPERATURA_FUERA_RANGO), tipos(e));
        assertEquals(NivelAlerta.AVISO, e.nivel());
        assertEquals("Tierra seca", e.texto()); // empate de nivel: gana el de más prioridad
        assertTrue(e.condiciones().get(0).mensaje().contains("45 %"), e.condiciones().get(0).mensaje());

        assertEquals(List.of(TipoAlerta.EXCESO_AGUA), tipos(estado(90, 20, 1, List.of())));
    }

    @Test
    void placaDesconectadaNoEvaluaLecturasViejas() {
        EstadoMaceta e = estado(10, 50, 45, List.of());
        assertEquals(List.of(TipoAlerta.PLACA_DESCONECTADA), tipos(e));
        assertEquals(NivelAlerta.CRITICO, e.nivel());
        assertTrue(e.condiciones().get(0).mensaje().contains("45 min"));
    }

    @Test
    void riegoFallidoSegunElResultadoDelEsp32() {
        EstadoMaceta vacio = estado(70, 20, 1, List.of(riego(ResultadoRiego.DEPOSITO_VACIO, 0, 58f, 58f)));
        assertEquals(List.of(TipoAlerta.RIEGO_FALLIDO), tipos(vacio));
        assertEquals(NivelAlerta.CRITICO, vacio.nivel());
        assertTrue(vacio.condiciones().get(0).mensaje().contains("depósito"));

        assertEquals(NivelAlerta.AVISO, estado(70, 20, 1, List.of(riego(ResultadoRiego.LIMITE_PULSOS, 5, 40f, 50f))).nivel());
        // OK según la placa pero la humedad apenas subió: bomba sin agua o tubo suelto.
        assertEquals(List.of(TipoAlerta.RIEGO_FALLIDO), tipos(estado(70, 20, 1, List.of(riego(ResultadoRiego.OK, 3, 55f, 56f)))));
        // «Regar ahora» descartado por humedad máxima no es un fallo.
        assertTrue(estado(70, 20, 1, List.of(riego(ResultadoRiego.HUMEDAD_MAXIMA, 0, 79f, 79f))).condiciones().isEmpty());
    }

    @Test
    void cuentaSoloElUltimoRiego() {
        Riego fallido = riego(ResultadoRiego.DEPOSITO_VACIO, 0, 58f, 58f);
        fallido.setTs(AHORA.minusHours(3));
        Riego bueno = riego(ResultadoRiego.OK, 3, 55f, 68f); // después de rellenar el depósito
        assertTrue(estado(70, 20, 1, List.of(fallido, bueno)).condiciones().isEmpty());
    }

    @Test
    void criticoGanaAAviso() {
        EstadoMaceta e = estado(40, 20, 1, List.of(riego(ResultadoRiego.DEPOSITO_VACIO, 0, 41f, 41f)));
        assertEquals(List.of(TipoAlerta.RIEGO_FALLIDO, TipoAlerta.TIERRA_SECA), tipos(e));
        assertEquals("Riego fallido", e.texto());
        assertEquals(NivelAlerta.CRITICO, e.nivel());
    }

    static EstadoMaceta conDeposito(NivelDeposito d) {
        List<Lectura> h = List.of(l("HUMEDAD", 70, 1));
        return MetricasMaceta.calcular(24, AHORA, h, List.of(), List.of(), HUMEDAD, null, null,
                List.of(), List.of(), AHORA.minusMinutes(1), d).estado();
    }

    @Test
    void depositoBajoAvisaConLosDiasQueQuedan() {
        EstadoMaceta e = conDeposito(new NivelDeposito(18f, AHORA.minusMinutes(2), 2L, 4.0, 2.0));
        assertEquals(List.of(TipoAlerta.DEPOSITO_BAJO), tipos(e));
        assertEquals(NivelAlerta.AVISO, e.nivel());
        assertEquals("Depósito bajo", e.texto());
        assertTrue(e.condiciones().get(0).mensaje().contains("18 %"));
        assertTrue(e.condiciones().get(0).mensaje().contains("unos 2 días"));
    }

    @Test
    void depositoEnElMinimoEsCritico() {
        EstadoMaceta e = conDeposito(new NivelDeposito(9f, AHORA.minusMinutes(2), 2L, null, null));
        assertEquals(NivelAlerta.CRITICO, e.nivel());
        assertTrue(e.condiciones().get(0).mensaje().contains("ya no riega"));
    }

    @Test
    void depositoConAguaOLecturaViejaNoAvisa() {
        assertTrue(conDeposito(new NivelDeposito(50f, AHORA.minusMinutes(2), 2L, 4.0, 10.0)).condiciones().isEmpty());
        assertTrue(conDeposito(new NivelDeposito(5f, AHORA.minusHours(2), 120L, null, null)).condiciones().isEmpty());
        assertTrue(conDeposito(NivelDeposito.SIN_DATOS).condiciones().isEmpty());
    }

    // --- A5 ---

    static NivelDeposito fuga(double perdida, long minutos, Float capacidad) {
        return new NivelDeposito(60f, AHORA.minusMinutes(minutos), minutos, null, null, capacidad, null, null,
                perdida, 4.0);
    }

    @Test
    void fugaDelDepositoAvisaConLitros() {
        EstadoMaceta e = conDeposito(fuga(12, 2, 5f));
        assertEquals(List.of(TipoAlerta.FUGA_DEPOSITO), tipos(e));
        assertEquals(NivelAlerta.AVISO, e.nivel());
        assertEquals("Posible fuga en el depósito", e.texto());
        String msg = e.condiciones().get(0).mensaje();
        assertTrue(msg.contains("12 puntos"), msg);
        assertTrue(msg.contains("0,6 L"), msg);
        assertTrue(msg.contains("4 h"), msg);
        assertFalse(conDeposito(fuga(12, 2, null)).condiciones().get(0).mensaje().contains(" L)"));
    }

    @Test
    void pocaBajadaOLecturaViejaNoEsFuga() {
        assertTrue(conDeposito(fuga(5, 2, 5f)).condiciones().isEmpty());
        assertTrue(conDeposito(fuga(20, 120, 5f)).condiciones().isEmpty());
    }
}
