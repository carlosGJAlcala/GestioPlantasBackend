package es.uah.huertojpa.seguridad;

import es.uah.huertojpa.huerto.dominio.entidades.HuertoHasUsuario;
import es.uah.huertojpa.huerto.dominio.repository.HuertoHasUsuarioRepository;
import es.uah.huertojpa.jwt.JwtService;
import es.uah.huertojpa.depositoAgua.dominio.DepositoAgua;
import es.uah.huertojpa.depositoAgua.dominio.DepositoaguaRepository;
import es.uah.huertojpa.maceta.dominio.Maceta;
import es.uah.huertojpa.maceta.dominio.MacetaHasSensor;
import es.uah.huertojpa.maceta.dominio.MacetaHasSensorId;
import es.uah.huertojpa.maceta.dominio.MacetaHasSensorRepository;
import es.uah.huertojpa.planta.dominio.entities.Planta;
import es.uah.huertojpa.planta.dominio.repository.PlantaRepository;
import es.uah.huertojpa.maceta.infrastructura.database.IMacetaDAO;
import es.uah.huertojpa.persona.dominio.entidades.Administrador;
import es.uah.huertojpa.persona.dominio.entidades.Persona;
import es.uah.huertojpa.persona.infrastructura.database.IAdministradorDAO;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class SeguridadTest {

    private static Persona persona(int id, String user) {
        Persona p = new Persona();
        p.setId(id);
        p.setUsuario(user);
        return p;
    }

    private static Authentication auth(Persona p) {
        return new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()); // role null ⇒ USER, sin NPE
    }

    private AccesoService acceso() {
        IAdministradorDAO admins = mock(IAdministradorDAO.class);
        when(admins.buscarPorId(1)).thenReturn(new Administrador());
        IMacetaDAO macetas = mock(IMacetaDAO.class);
        Maceta m = new Maceta();
        m.setId(10);
        m.setHuertoIdhuerto(5);
        when(macetas.buscarPorID(10)).thenReturn(m);
        HuertoHasUsuarioRepository huertos = mock(HuertoHasUsuarioRepository.class);
        when(huertos.findById_HuertoIdhuertoAndId_UsuarioPersonaId(anyInt(), anyInt())).thenReturn(Optional.empty());
        when(huertos.findById_HuertoIdhuertoAndId_UsuarioPersonaId(5, 2)).thenReturn(Optional.of(new HuertoHasUsuario()));
        PlantaRepository plantas = mock(PlantaRepository.class);
        Planta tomate = new Planta();
        tomate.setId(7);
        tomate.setMacetaIdmaceta(10);
        when(plantas.findById(7)).thenReturn(Optional.of(tomate));
        DepositoaguaRepository depositos = mock(DepositoaguaRepository.class);
        DepositoAgua deposito = new DepositoAgua();
        deposito.setId(3);
        deposito.setHuertoIdhuerto(5);
        when(depositos.findById(3)).thenReturn(Optional.of(deposito));
        when(depositos.findBySensorIdsensor_Id(anyInt())).thenReturn(Optional.empty());
        MacetaHasSensorRepository sensores = mock(MacetaHasSensorRepository.class);
        MacetaHasSensorId enlace = new MacetaHasSensorId();
        enlace.setMacetaIdmaceta(10);
        enlace.setSensorIdsensor(20);
        MacetaHasSensor ms = new MacetaHasSensor();
        ms.setId(enlace);
        when(sensores.findById_SensorIdsensor(20)).thenReturn(List.of(ms));
        // W12b: 21 es un sensor libre antiguo (sin dueño); 22, uno libre que dio de alta Ana (persona 2).
        var repoSensores = mock(es.uah.huertojpa.sensores.dominio.repository.SensorRepository.class);
        var antiguo = new es.uah.huertojpa.sensores.dominio.entidades.Sensor();
        antiguo.setId(21);
        var deAna = new es.uah.huertojpa.sensores.dominio.entidades.Sensor();
        deAna.setId(22);
        deAna.setCreadoPor(2);
        when(repoSensores.findById(21)).thenReturn(Optional.of(antiguo));
        when(repoSensores.findById(22)).thenReturn(Optional.of(deAna));
        return new AccesoService(admins, macetas, huertos, plantas, depositos, sensores,
                mock(es.uah.huertojpa.alertas.AlertaRepository.class), repoSensores);
    }

    @Test
    void propiedadDePlantasDepositosYSensores() {
        AccesoService a = acceso();
        Authentication duena = auth(persona(2, "ana"));
        Authentication otro = auth(persona(3, "luis"));
        assertTrue(a.puedeAccederHuerto(duena, 5));
        assertFalse(a.puedeAccederHuerto(otro, 5));
        assertTrue(a.puedeAccederPlanta(duena, 7));
        assertFalse(a.puedeAccederPlanta(otro, 7));
        assertFalse(a.puedeAccederPlanta(duena, 99), "planta inexistente");
        assertTrue(a.puedeAccederDeposito(duena, 3));
        assertFalse(a.puedeAccederDeposito(otro, 3));
        assertTrue(a.puedeAccederSensor(duena, 20), "sensor de una maceta suya");
        assertFalse(a.puedeAccederSensor(otro, 20));
        assertTrue(a.puedeAccederSensor(otro, 21), "sensor sin asociar y sin dueño: libre para asociarlo");
        assertTrue(a.puedeAccederSensor(duena, 22), "sensor libre que dio de alta ella");
        assertFalse(a.puedeAccederSensor(otro, 22), "sensor libre de otra persona");
        assertFalse(a.puedeAccederSensor(otro, 99), "sensor inexistente");
        assertFalse(a.puedeAccederSensor(null, 21));
        assertTrue(a.puedeAccederPersona(duena, 2));
        assertFalse(a.puedeAccederPersona(duena, 3));
        assertTrue(a.puedeAccederPersona(auth(persona(1, "admin")), 3));
    }

    @Test
    void propiedadDeMacetas() {
        AccesoService a = acceso();
        Authentication admin = auth(persona(1, "admin"));
        Authentication duena = auth(persona(2, "ana"));
        Authentication otro = auth(persona(3, "luis"));
        Authentication esp32 = new UsernamePasswordAuthenticationToken("dispositivo", null,
                List.of(new SimpleGrantedAuthority(DispositivoKeyFilter.AUTORIDAD)));

        assertTrue(a.puedeAccederMaceta(duena, 10));
        assertFalse(a.puedeAccederMaceta(otro, 10), "un usuario no ve macetas de huertos ajenos");
        assertFalse(a.puedeAccederMaceta(duena, 99), "maceta inexistente");
        assertTrue(a.puedeAccederMaceta(admin, 10));
        assertTrue(a.puedeAccederMaceta(esp32, 10));
        assertFalse(a.puedeAccederMaceta(null, 10));
    }

    @Test
    void propiedadDeUsuario() {
        AccesoService a = acceso();
        assertTrue(a.puedeAccederUsuario(auth(persona(2, "ana")), "ana"));
        assertFalse(a.puedeAccederUsuario(auth(persona(2, "ana")), "luis"));
        assertTrue(a.puedeAccederUsuario(auth(persona(1, "admin")), "luis"));
        assertFalse(a.esAdmin(auth(persona(2, "ana"))));
    }

    @Test
    void claveDeDispositivo() {
        DispositivoKeyFilter sinClave = new DispositivoKeyFilter("");
        assertTrue(sinClave.modoCompatibilidad());
        assertFalse(sinClave.claveValida("loquesea"));

        DispositivoKeyFilter conClave = new DispositivoKeyFilter("abc123");
        assertFalse(conClave.modoCompatibilidad());
        assertTrue(conClave.claveValida("abc123"));
        assertFalse(conClave.claveValida("abc124"));
        assertFalse(conClave.claveValida(null));

        assertTrue(DispositivoKeyFilter.esRutaDispositivo("/auth/riego"));
        assertTrue(DispositivoKeyFilter.esRutaDispositivo("/auth/maceta/3/orden"));
        assertFalse(DispositivoKeyFilter.esRutaDispositivo("/auth/login"));
        assertFalse(DispositivoKeyFilter.esRutaDispositivo("/maceta/3/riego"));
    }

    @Test
    void jwtConSecretoConfiguradoYCorto() {
        String secreto = java.util.Base64.getEncoder().encodeToString(new byte[48]);
        JwtService a = new JwtService(secreto, 1);
        JwtService b = new JwtService(secreto, 1);
        Persona ana = persona(2, "ana");
        String token = a.getToken(ana);
        assertEquals("ana", b.getUsernameFromToken(token), "misma clave ⇒ el token sobrevive a un reinicio");
        assertTrue(b.isTokenValid(token, ana));

        JwtService aleatoria = new JwtService("", 1);
        assertThrows(io.jsonwebtoken.JwtException.class, () -> aleatoria.getUsernameFromToken(token));
        assertThrows(IllegalStateException.class,
                () -> new JwtService(java.util.Base64.getEncoder().encodeToString(new byte[8]), 1));
    }
}
