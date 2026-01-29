package com.example.fingerprint_api.controllers;

import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import com.example.fingerprint_api.repositories.AlumnoRepository;
import com.example.fingerprint_api.services.MultiReaderFingerprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/multi-fingerprint")
@CrossOrigin(origins = "http://localhost:3000")
public class MultiReaderController {

    @Autowired
    private MultiReaderFingerprintService multiService;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @GetMapping("/auto-select")
    public List<String> getReaders() {
        return multiService.refreshConnectedReaders();
    }

    // ==========================================
    // MÉTODO RESTAURADO CON TU LÓGICA ORIGINAL
    // ==========================================
    @GetMapping("/identify-auto")
    public ResponseEntity<?> identificar(
            // Agregamos esto para recibir la puerta, si no llega usa 1
            @RequestParam(name = "numeroEntrada", defaultValue = "1") Integer numeroEntrada
    ) {
        // 1. Llamamos al servicio pasando la puerta
        AlumnoModel alumno = multiService.identificarDedoAutomatico(numeroEntrada);

        // --- TU LÓGICA ORIGINAL ---

        // CASO 1: ÉXITO (Dedo reconocido y alumno encontrado)
        if (alumno != null && alumno.getPrimerNombre() != null) {
            // Devolvemos el objeto completo como te gusta
            return ResponseEntity.ok(alumno);
        }

        // CASO 2: SILENCIO (Timeout o espera agotada sin poner dedo)
        // Devolvemos un 200 OK con objeto vacío para que el Front reintente sin error
        if (alumno != null && alumno.getPrimerNombre() == null) {
            return ResponseEntity.ok(alumno);
        }

        // CASO 3: ERROR REAL (Puso el dedo pero no existe en BD)
        // Esto dispara la alerta roja en el frontend
        return ResponseEntity.status(404).build();
    }
    // ==========================================

    @PostMapping("/enroll-step/{studentId}")
    public ResponseEntity<?> enrolarPaso(@PathVariable Integer studentId, @RequestBody Map<String, Object> payload) {
        String readerId = (String) payload.get("readerId");
        Integer step = (Integer) payload.get("step");
        String result = multiService.enrolarAlumnoEnBaseDeDatos(readerId, studentId, step);
        return result.contains("Exito") ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/pre-registrar")
    public ResponseEntity<AlumnoModel> preRegistrar(@RequestBody AlumnoModel alumno) {
        alumno.setId(null);
        alumno.setActivo(1);
        alumno.setDeleted(0);
        alumno.setVersion(1);
        alumno.setUuid(java.util.UUID.randomUUID().toString());
        alumno.setCreatedAt(java.time.LocalDateTime.now());
        AlumnoModel nuevo = alumnoRepository.save(alumno);
        return ResponseEntity.ok(nuevo);
    }

    @PutMapping("/completar-registro/{id}")
    public ResponseEntity<?> completarRegistro(@PathVariable Integer id, @RequestBody AlumnoModel datos) {
        return alumnoRepository.findById(id).map(alumno -> {
            alumno.setPrimerNombre(datos.getPrimerNombre());
            alumno.setSegundoNombre(datos.getSegundoNombre());
            alumno.setApellidoPaterno(datos.getApellidoPaterno());
            alumno.setApellidoMaterno(datos.getApellidoMaterno());
            alumno.setNumeroControl(datos.getNumeroControl());
            alumno.setNumTelefono(datos.getNumTelefono());
            alumno.setCarreraClave(datos.getCarreraClave());
            alumno.setUsuario(datos.getUsuario() != null ? datos.getUsuario() : "ADMIN_SISTEMA");
            alumno.setActivo(1);
            alumno.setDeleted(0);
            alumno.setVersion(datos.getVersion() != null ? datos.getVersion() + 1 : 1);
            alumno.setUpdateAt(java.time.LocalDateTime.now());
            alumnoRepository.saveAndFlush(alumno);
            return ResponseEntity.ok("Registro guardado exitosamente");
        }).orElse(ResponseEntity.status(404).body("Error: No se encontró el registro."));
    }
}