package com.example.fingerprint_api.controllers; // Verifica tu paquete

import com.digitalpersona.uareu.UareUException;
import com.example.fingerprint_api.dto.ReaderRequest;
import com.example.fingerprint_api.services.MultiReaderFingerprintService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/multi-fingerprint")
@CrossOrigin(origins = "*")
public class MultiReaderController {

    private static final Logger logger = LoggerFactory.getLogger(MultiReaderController.class);

    @Autowired
    private MultiReaderFingerprintService multiService;

    // --- 1. GESTIÓN DE HARDWARE ---

    @GetMapping("/auto-select")
    public ResponseEntity<?> autoSelect() {
        try {
            List<String> readers = multiService.refreshConnectedReaders();
            return ResponseEntity.ok(readers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error detectando lectores: " + e.getMessage());
        }
    }

    @GetMapping("/readers")
    public ResponseEntity<?> getReaders() {
        Set<String> readers = multiService.getAvailableReaderNames();
        return ResponseEntity.ok(readers);
    }

    // --- 2. GESTIÓN DE USO (RESERVAR/LIBERAR) ---

    @PostMapping("/reserve")
    public ResponseEntity<?> reserveReader(@RequestBody ReaderRequest request) {
        boolean ok = multiService.reserveReaderByInstance(request.readerId(), request.instanceId());
        return ok ? ResponseEntity.ok("Reservado") : ResponseEntity.badRequest().body("Ocupado o no encontrado");
    }

    @PostMapping("/release")
    public ResponseEntity<?> releaseReader(@RequestBody Map<String, String> body) {
        multiService.releaseReaderByInstance(body.get("instanceId"));
        return ResponseEntity.ok("Liberado");
    }

    // --- 3. FUNCIONALIDAD PRINCIPAL ---

    /**
     * MODO CHECADOR: Busca alumnos continuamente y registra asistencia.
     */
    @PostMapping("/start-clock-mode")
    public ResponseEntity<?> startClockMode(@RequestBody ReaderRequest request) {
        boolean ok = multiService.startContinuousCapture(request.readerId(), request.instanceId());
        return ok ? ResponseEntity.ok("Modo búsqueda iniciado") : ResponseEntity.badRequest().body("Error al iniciar (¿Reservado?)");
    }

    /**
     * MODO ENROLAMIENTO: Registra/Edita la huella de un alumno (ID).
     */
    @PostMapping("/enroll/alumno/{id}")
    public ResponseEntity<?> registrarHuella(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String readerId = body.get("readerId");
        if(readerId == null) return ResponseEntity.badRequest().body("Falta readerId");

        String resultado = multiService.registrarHuellaParaAlumno(readerId, id);

        if ("Exito".equals(resultado)) {
            return ResponseEntity.ok("Huella guardada correctamente.");
        } else {
            return ResponseEntity.badRequest().body(resultado);
        }
    }
}