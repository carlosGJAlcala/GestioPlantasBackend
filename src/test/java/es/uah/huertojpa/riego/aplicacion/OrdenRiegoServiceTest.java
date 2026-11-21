package es.uah.huertojpa.riego.aplicacion;

import es.uah.huertojpa.maceta.dominio.Maceta;
import es.uah.huertojpa.maceta.infrastructura.database.IMacetaDAO;
import es.uah.huertojpa.riego.dominio.EstadoOrden;
import es.uah.huertojpa.riego.dominio.OrdenRiego;
import es.uah.huertojpa.riego.dominio.OrdenRiegoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrdenRiegoServiceTest {
    private static final ZoneId Z = ZoneId.of("Europe/Madrid");
    private final List<OrdenRiego> tabla = new ArrayList<>();
    private Instant ahora = LocalDateTime.of(2026, 11, 5, 10, 0).atZone(Z).toInstant();
    private OrdenRiegoService service;

    /** Reloj mutable para simular el paso del tiempo. */
    private final Clock reloj = new Clock() {
        public ZoneId getZone() { return Z; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return ahora; }
    };

    @BeforeEach
    void preparar() {
        OrdenRiegoRepository repo = mock(OrdenRiegoRepository.class);
        when(repo.save(any())).thenAnswer(inv -> {
            OrdenRiego o = inv.getArgument(0);
            if (o.getId() == null) { o.setId((long) tabla.size() + 1); tabla.add(o); }
            return o;
        });
        when(repo.findByMacetaIdAndEstadoOrderByCreadaAsc(anyInt(), any())).thenAnswer(inv -> tabla.stream()
                .filter(o -> o.getMacetaId().equals(inv.getArgument(0)) && o.getEstado() == inv.getArgument(1))
                .sorted(Comparator.comparing(OrdenRiego::getCreada)).toList());
        when(repo.findFirstByMacetaIdAndEstadoOrderByEntregadaDesc(anyInt(), any())).thenAnswer(inv -> tabla.stream()
                .filter(o -> o.getMacetaId().equals(inv.getArgument(0)) && o.getEstado() == inv.getArgument(1))
                .max(Comparator.comparing(OrdenRiego::getEntregada)));
        IMacetaDAO macetas = mock(IMacetaDAO.class);
        when(macetas.buscarPorID(1)).thenReturn(new Maceta());
        service = new OrdenRiegoService(repo, macetas, reloj, 15, 5);
    }

    private void pasan(int minutos) {
        ahora = ahora.plus(Duration.ofMinutes(minutos));
    }

    @Test
    void pedirDosVecesDevuelveLaMismaOrden() {
        var a = service.solicitar(1, "ana");
        var b = service.solicitar(1, "ana");
        assertEquals(OrdenRiegoService.Resultado.CREADA, a.resultado());
        assertEquals(OrdenRiegoService.Resultado.YA_PENDIENTE, b.resultado());
        assertEquals(a.orden().id(), b.orden().id());
        assertEquals(1, tabla.size());
    }

    @Test
    void elDispositivoLaRecogeUnaSolaVez() {
        service.solicitar(1, "ana");
        assertTrue(service.entregar(1).isPresent());
        assertTrue(service.entregar(1).isEmpty());
        assertEquals(EstadoOrden.ENTREGADA, tabla.get(0).getEstado());
    }

    @Test
    void ordenNoRecogidaCaduca() {
        service.solicitar(1, "ana");
        pasan(16);
        assertTrue(service.entregar(1).isEmpty(), "no debe regar 16 min después de pedirlo");
        assertEquals(EstadoOrden.CADUCADA, tabla.get(0).getEstado());
        assertEquals(OrdenRiegoService.Resultado.CREADA, service.solicitar(1, "ana").resultado());
    }

    @Test
    void esperaTrasUnRiegoManual() {
        service.solicitar(1, "ana");
        service.entregar(1);
        pasan(2);
        var s = service.solicitar(1, "ana");
        assertEquals(OrdenRiegoService.Resultado.EN_ESPERA, s.resultado());
        assertEquals(180, s.segundosEspera());
        pasan(4);
        assertEquals(OrdenRiegoService.Resultado.CREADA, service.solicitar(1, "ana").resultado());
    }

    @Test
    void cancelarYMacetaInexistente() {
        service.solicitar(1, "ana");
        assertTrue(service.cancelar(1));
        assertFalse(service.cancelar(1));
        assertTrue(service.entregar(1).isEmpty());
        assertEquals(OrdenRiegoService.Resultado.MACETA_NO_EXISTE, service.solicitar(99, "ana").resultado());
    }
}
