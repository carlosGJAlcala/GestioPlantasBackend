package es.uah.huertojpa.seguridad;

import es.uah.huertojpa.alertas.AlertaRepository;
import es.uah.huertojpa.depositoAgua.dominio.DepositoaguaRepository;
import es.uah.huertojpa.huerto.dominio.repository.HuertoHasUsuarioRepository;
import es.uah.huertojpa.maceta.dominio.Maceta;
import es.uah.huertojpa.maceta.dominio.MacetaHasSensorRepository;
import es.uah.huertojpa.maceta.infrastructura.database.IMacetaDAO;
import es.uah.huertojpa.persona.dominio.entidades.Persona;
import es.uah.huertojpa.persona.infrastructura.database.IAdministradorDAO;
import es.uah.huertojpa.planta.dominio.repository.PlantaRepository;
import es.uah.huertojpa.sensores.dominio.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * S4 / S4b – Reglas de autorización sobre los datos.
 * <ul>
 *     <li>Administrador: la persona tiene fila en la tabla {@code administrador} (mismo criterio que el login, que
 *     devuelve {@code permisoAdmin}). No se usa la columna {@code role} porque no se rellena de forma fiable.</li>
 *     <li>Usuario: solo accede a sus datos ({@code userName}/id propio) y a lo que cuelga de sus huertos
 *     ({@code huerto_has_usuario}): macetas, plantas, depósitos y sensores asociados.</li>
 *     <li>Dispositivo (ESP32 con clave): puede leer/escribir datos de macetas.</li>
 * </ul>
 * Recurso inexistente ⇒ {@code false} (403) para no revelar qué ids existen; el administrador sí recibe el 404/"false"
 * propio del endpoint.
 */
@Service("acceso")
public class AccesoService {
    private final IAdministradorDAO administradorDAO;
    private final IMacetaDAO macetaDAO;
    private final HuertoHasUsuarioRepository huertoHasUsuarioRepository;
    private final PlantaRepository plantaRepository;
    private final DepositoaguaRepository depositoRepository;
    private final MacetaHasSensorRepository macetaHasSensorRepository;
    private final AlertaRepository alertaRepository;
    private final SensorRepository sensorRepository;

    @Autowired
    public AccesoService(IAdministradorDAO administradorDAO, IMacetaDAO macetaDAO,
                         HuertoHasUsuarioRepository huertoHasUsuarioRepository, PlantaRepository plantaRepository,
                         DepositoaguaRepository depositoRepository, MacetaHasSensorRepository macetaHasSensorRepository,
                         AlertaRepository alertaRepository, SensorRepository sensorRepository) {
        this.administradorDAO = administradorDAO;
        this.macetaDAO = macetaDAO;
        this.huertoHasUsuarioRepository = huertoHasUsuarioRepository;
        this.plantaRepository = plantaRepository;
        this.depositoRepository = depositoRepository;
        this.macetaHasSensorRepository = macetaHasSensorRepository;
        this.alertaRepository = alertaRepository;
        this.sensorRepository = sensorRepository;
    }

    /** Persona del JWT, o {@code null} si la petición es anónima o del dispositivo. */
    public static Persona persona(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        return auth.getPrincipal() instanceof Persona p ? p : null;
    }

    public static boolean esDispositivo(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> DispositivoKeyFilter.AUTORIDAD.equals(a.getAuthority()));
    }

    /** Lanza 403 si no se cumple la regla. Para las comprobaciones que dependen del cuerpo de la petición. */
    public static void exigir(boolean permitido) {
        if (!permitido) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    public boolean esAdmin(Authentication auth) {
        Persona p = persona(auth);
        return p != null && p.getId() != null && administradorDAO.buscarPorId(p.getId()) != null;
    }

    /** El usuario del token es {@code userName} o es administrador. */
    public boolean puedeAccederUsuario(Authentication auth, String userName) {
        Persona p = persona(auth);
        if (p == null) return false;
        return (userName != null && userName.equals(p.getUsername())) || esAdmin(auth);
    }

    /** La persona {@code idPersona} es la del token, o el token es de un administrador. */
    public boolean puedeAccederPersona(Authentication auth, Integer idPersona) {
        Persona p = persona(auth);
        if (p == null) return false;
        return (idPersona != null && idPersona.equals(p.getId())) || esAdmin(auth);
    }

    /** El huerto está asociado al usuario del token (o es administrador/dispositivo). */
    public boolean puedeAccederHuerto(Authentication auth, Integer idHuerto) {
        if (esDispositivo(auth)) return true;
        Persona p = persona(auth);
        if (p == null) return false;
        if (esAdmin(auth)) return true;
        return idHuerto != null && p.getId() != null
                && huertoHasUsuarioRepository.findById_HuertoIdhuertoAndId_UsuarioPersonaId(idHuerto, p.getId()).isPresent();
    }

    /** La maceta pertenece a un huerto del usuario, o es administrador/dispositivo. Maceta inexistente ⇒ false. */
    public boolean puedeAccederMaceta(Authentication auth, Integer idMaceta) {
        if (esDispositivo(auth)) return true;
        Persona p = persona(auth);
        if (p == null || idMaceta == null) return false;
        if (esAdmin(auth)) return true;
        Maceta m = macetaDAO.buscarPorID(idMaceta);
        return m != null && m.getHuertoIdhuerto() != null && puedeAccederHuerto(auth, m.getHuertoIdhuerto());
    }

    /** La planta está en una maceta accesible. */
    public boolean puedeAccederPlanta(Authentication auth, Integer idPlanta) {
        if (idPlanta == null) return false;
        if (esDispositivo(auth) || esAdmin(auth)) return true;
        return plantaRepository.findById(idPlanta)
                .map(pl -> puedeAccederMaceta(auth, pl.getMacetaIdmaceta()))
                .orElse(false);
    }

    /** A3: la alerta es de una maceta accesible (solo personas: la placa no gestiona alertas). Inexistente ⇒ false. */
    public boolean puedeAccederAlerta(Authentication auth, Integer idAlerta) {
        if (idAlerta == null || esDispositivo(auth) || persona(auth) == null) return false;
        return alertaRepository.findById(idAlerta.longValue())
                .map(a -> puedeAccederMaceta(auth, a.getMacetaId()))
                .orElse(false);
    }

    /** El depósito es de un huerto accesible. */
    public boolean puedeAccederDeposito(Authentication auth, Integer idDeposito) {
        if (idDeposito == null) return false;
        if (esDispositivo(auth) || esAdmin(auth)) return true;
        return depositoRepository.findById(idDeposito)
                .map(d -> puedeAccederHuerto(auth, d.getHuertoIdhuerto()))
                .orElse(false);
    }

    /**
     * El sensor está asociado a una maceta accesible o es el sensor del depósito de un huerto accesible.
     * Un sensor todavía <b>sin asociar</b> es de quien lo dio de alta (W12b, columna {@code creado_por}): solo él
     * puede asociarlo a una maceta o a su depósito. Los sensores libres antiguos, sin dueño, los puede usar cualquier
     * usuario, como antes. En cuanto se asocia, solo lo ve el dueño de esa maceta o de ese depósito.
     */
    public boolean puedeAccederSensor(Authentication auth, Integer idSensor) {
        if (idSensor == null) return false;
        if (esDispositivo(auth) || esAdmin(auth)) return true;
        if (persona(auth) == null) return false;
        var enMacetas = macetaHasSensorRepository.findById_SensorIdsensor(idSensor);
        var deposito = depositoRepository.findBySensorIdsensor_Id(idSensor);
        if (enMacetas.isEmpty() && deposito.isEmpty()) {
            Integer persona = persona(auth).getId();
            return sensorRepository.findById(idSensor)
                    .map(s -> s.getCreadoPor() == null || s.getCreadoPor().equals(persona))
                    .orElse(false);
        }
        return enMacetas.stream().anyMatch(ms -> puedeAccederMaceta(auth, ms.getId().getMacetaIdmaceta()))
                || deposito.map(d -> puedeAccederHuerto(auth, d.getHuertoIdhuerto())).orElse(false);
    }
}
