package es.uah.huertojpa.maceta.infrastructura.restcontroller;

import es.uah.huertojpa.maceta.aplicacion.IMacetaHasSensorService;
import es.uah.huertojpa.depositoAgua.dominio.DepositoaguaRepository;
import es.uah.huertojpa.maceta.dominio.MacetaHasSensor;
import es.uah.huertojpa.maceta.dominio.MacetaRepository;
import es.uah.huertojpa.sensores.dominio.repository.SensorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import es.uah.huertojpa.seguridad.AccesoService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MacetaHasSensorController {

    @Autowired
    IMacetaHasSensorService service;
    @Autowired
    AccesoService acceso;
    @Autowired
    DepositoaguaRepository depositoRepository;
    @Autowired
    SensorRepository sensorRepository;
    @Autowired
    MacetaRepository macetaRepository;

    /** S4b: solo se asocian/desasocian sensores accesibles a macetas propias. */
    private void comprobar(MacetaHasSensor m, Authentication auth) {
        var id = m == null ? null : m.getId();
        AccesoService.exigir(id != null && acceso.puedeAccederMaceta(auth, id.getMacetaIdmaceta())
                && acceso.puedeAccederSensor(auth, id.getSensorIdsensor()));
    }
    @GetMapping("/macetahassensor")
    public List<MacetaHasSensor> buscarTodos() {
        return service.buscarTodos();
    }

    @GetMapping("/macetahassensor/maceta/{id}")
    public List<MacetaHasSensor> buscarPorIDMaceta(@PathVariable("id") Integer id) {
        return service.buscarPorIDMaceta(id);
    }
    @GetMapping("/macetahassensor/sensor/{id}")
    public List<MacetaHasSensor> buscarPorIDSensor(@PathVariable("id") Integer id) {
        return service.buscarPorIDSensor(id);
    }


    /**
     * W12b: el sensor de un depósito no puede medir además una maceta (sus distancias se tomarían como humedad), y la
     * maceta y el sensor tienen que existir (400 con el motivo; antes cualquier alta daba 500).
     */
    @PostMapping(value= "/macetahassensor", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> guardar(@RequestBody MacetaHasSensor enlace, Authentication auth) {
        comprobar(enlace, auth);
        var id = enlace.getId();
        if (depositoRepository.findBySensorIdsensor_Id(id.getSensorIdsensor()).isPresent()) {
            return ResponseEntity.badRequest().body("Ese sensor mide un depósito: no puede medir también una maceta.");
        }
        if (!sensorRepository.existsById(id.getSensorIdsensor()) || !macetaRepository.existsById(id.getMacetaIdmaceta())) {
            return ResponseEntity.badRequest().body("No existe esa maceta o ese sensor.");
        }
        service.guardar(enlace);
        return ResponseEntity.ok("true");
    }



    @DeleteMapping(value = "/macetahassensor",produces = MediaType.TEXT_PLAIN_VALUE)
    public String eliminar(@RequestBody MacetaHasSensor macetaHasSensor, Authentication auth) {
        comprobar(macetaHasSensor, auth);
        service.eliminar(macetaHasSensor);
    return "";
    }
}
