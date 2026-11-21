package es.uah.huertojpa.depositoAgua.infrastructura.restcontroller;

import es.uah.huertojpa.depositoAgua.aplicacion.service.IDepositoAguaService;
import es.uah.huertojpa.depositoAgua.aplicacion.dto.DepositoaguaDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import es.uah.huertojpa.seguridad.AccesoService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DepositoaguaController {
    @Autowired
    IDepositoAguaService service;
    @Autowired
    AccesoService acceso;
    @GetMapping("/depositoagua")
    public List<DepositoaguaDto> buscarTodos() {
        return service.buscarTodos();
    }

    @GetMapping("/depositoagua/{id}")
    public DepositoaguaDto buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorID(id);
    }
    @GetMapping("/depositoagua/userName/{userName}")
    public List<DepositoaguaDto> buscarPorId(@PathVariable("userName") String userName) {
        return service.buscarPorUserName(userName);
    }
    @GetMapping("/depositoagua/huerto/{id}")
    public DepositoaguaDto buscarporIdHuerto(@PathVariable("id") Integer id) {
        return service.buscarporIdHuerto(id);
    }


    @PostMapping(value= "/depositoagua", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> guardar(@RequestBody DepositoaguaDto depositoaguaDto, Authentication auth) {
        AccesoService.exigir(acceso.puedeAccederHuerto(auth, depositoaguaDto.getHuertoIdhuerto())); // S4b
        exigirSensor(depositoaguaDto, auth);
        String motivo = service.motivoNoValido(depositoaguaDto);
        if (motivo != null) return ResponseEntity.badRequest().body(motivo); // W12: el front enseña el motivo
        return ResponseEntity.ok(String.valueOf(service.guardar(depositoaguaDto)));
    }

    /**
     * W12 – Edita capacidad, altura, huerto y sensor (los campos a null no se tocan; {@code "sensorIdsensor":{"id":null}}
     * quita el sensor). Antes solo guardaba el estado y no devolvía nada (el formulario decía siempre «problema»).
     */
    @PutMapping(value= "/depositoagua", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> actualizar(@RequestBody DepositoaguaDto depositoaguaDto, Authentication auth) {
        AccesoService.exigir(acceso.puedeAccederDeposito(auth, depositoaguaDto.getId())
                && (depositoaguaDto.getHuertoIdhuerto() == null
                    || acceso.puedeAccederHuerto(auth, depositoaguaDto.getHuertoIdhuerto()))); // S4b
        exigirSensor(depositoaguaDto, auth);
        String motivo = service.motivoNoValido(depositoaguaDto);
        if (motivo != null) return ResponseEntity.badRequest().body(motivo);
        return ResponseEntity.ok(String.valueOf(service.actualizar(depositoaguaDto)));
    }

    /** El sensor que se asocia tiene que ser libre o del usuario (como al asociarlo a una maceta). */
    private void exigirSensor(DepositoaguaDto dto, Authentication auth) {
        Integer idSensor = dto.getSensorIdsensor() == null ? null : dto.getSensorIdsensor().getId();
        if (idSensor != null) AccesoService.exigir(acceso.puedeAccederSensor(auth, idSensor));
    }

    @DeleteMapping(value = "/depositoagua/{id}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String eliminar(@PathVariable("id") Integer id) {
        return String.valueOf(service.eliminar(id));
    }
}
