package es.uah.huertojpa.sensores.infrastructura.restcontroller;

import es.uah.huertojpa.persona.dominio.entidades.Persona;
import es.uah.huertojpa.sensores.aplicacion.ISensorService;
import es.uah.huertojpa.sensores.aplicacion.UsoSensorService;
import es.uah.huertojpa.sensores.dominio.entidades.SensorUso;
import org.springframework.http.ResponseEntity;
import es.uah.huertojpa.sensores.dominio.entidades.SensorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import es.uah.huertojpa.seguridad.AccesoService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

public class SensorController {
    @Autowired
    ISensorService service;
    @Autowired
    AccesoService acceso;
    @Autowired
    UsoSensorService usoSensores;

    /**
     * S4b: {@code PUT /auth/sensor*} lo usan el ESP32 (clave de dispositivo, o sin nada en modo compatibilidad) y la
     * web (JWT). Si viene de un usuario con JWT, el sensor tiene que ser suyo.
     */
    private void comprobarUsuario(SensorDto sensor, Authentication auth) {
        if (AccesoService.persona(auth) != null) {
            AccesoService.exigir(sensor != null && acceso.puedeAccederSensor(auth, sensor.getId()));
        }
    }

    @GetMapping("/sensor")
    public List<SensorDto> buscarTodos() {
        return service.buscarTodos();
    }

    @GetMapping("/sensor/{id}")
    public SensorDto buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorID(id);
    }
    @GetMapping("/sensor/userName/{userName}")
    public List<SensorDto>  buscarporUserName(@PathVariable("userName") String userName) {
        return service.buscarByUserName(userName);
    }

    @GetMapping("/sensor/nombre/{nombre}")
    public List<SensorDto>  buscarporNombre(@PathVariable("nombre") String nombre) {
        return service.buscarPorNombre(nombre);
    }
    /**
     * W12b – Sensores con su uso (macetas, depósito, libre) para la pantalla de tarjetas: el administrador ve todos;
     * el usuario, los de «Mis sensores» ({@code /sensor/userName/{u}}).
     */
    @GetMapping("/sensor/uso")
    public List<SensorUso> buscarConUso(Authentication auth) {
        Persona p = AccesoService.persona(auth);
        AccesoService.exigir(p != null);
        return usoSensores.conUso(acceso.esAdmin(auth) ? service.buscarTodos() : service.buscarByUserName(p.getUsername()));
    }

    /** W12b: el sensor queda a nombre de quien lo da de alta (es suyo mientras esté libre). */
    @PostMapping(value= "/sensor")
    public String guardar(@RequestBody SensorDto sensor, Authentication auth) {
        Persona p = AccesoService.persona(auth);
        return String.valueOf(service.guardar(sensor, p == null ? null : p.getId()));
    }

    /**
     * W12b – Editar nombre, magnitud y unidades desde la web. Antes el formulario «Actualizar sensor» usaba
     * {@code PUT /auth/sensor}, que es el de las lecturas del ESP32: solo cambiaba la medida y la apuntaba como una
     * lectura más (con su riego y sus alertas).
     */
    @PutMapping(value = "/sensor", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> actualizarDatos(@RequestBody SensorDto sensor, Authentication auth) {
        AccesoService.exigir(sensor != null && sensor.getId() != null && acceso.puedeAccederSensor(auth, sensor.getId()));
        if (sensor.getNombreSensor() == null || sensor.getNombreSensor().isBlank()) {
            return ResponseEntity.badRequest().body("El sensor necesita un nombre.");
        }
        return ResponseEntity.ok(String.valueOf(service.actualizarDatos(sensor)));
    }

    @PutMapping(value= "/auth/sensor")
    public boolean actualizar(@RequestBody SensorDto sensor, Authentication auth) {
        comprobarUsuario(sensor, auth);
        return service.actualizar(sensor);
    }
    @PutMapping(value= "/auth/sensorDepositoAgua")
    public boolean actualizarDepositoAgua(@RequestBody SensorDto sensor, Authentication auth) {
        comprobarUsuario(sensor, auth);
        return service.actualizarDepositoAgua(sensor);
    }
    /** W12b: un sensor en uso no se borra (400 con el motivo); antes desaparecía de su maceta o de su depósito. */
    @DeleteMapping(value = "/sensor/{id}",produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> eliminar(@PathVariable("id") Integer id) {
        String motivo = usoSensores.motivoNoBorrable(id);
        if (motivo != null) return ResponseEntity.badRequest().body(motivo);
        return ResponseEntity.ok(String.valueOf(service.eliminar(id)));
    }
}
