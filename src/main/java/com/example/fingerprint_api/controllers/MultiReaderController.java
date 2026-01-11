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

    @GetMapping("/identify-auto")
    public ResponseEntity<?> identificar() {
        AlumnoModel alumno = multiService.identificarDedoAutomatico();

        // 1. ÉXITO (Dedo reconocido)
        if (alumno != null && alumno.getPrimerNombre() != null) {
            return ResponseEntity.ok(alumno);
        }

        // 2. SILENCIO (Timeout / No pusieron el dedo)
        if (alumno != null && alumno.getPrimerNombre() == null) {
            return ResponseEntity.ok(alumno); // El front reintenta solo
        }

        // 3. ERROR (Dedo puesto pero desconocido)
        // ESTO es lo que le dice al Front: "¡Dispárame la ventana roja!"
        return ResponseEntity.status(404).build();
    }

    @PostMapping("/enroll-step/{studentId}")
    public ResponseEntity<?> enrolarPaso(@PathVariable Integer studentId, @RequestBody Map<String, Object> payload) {
        String readerId = (String) payload.get("readerId");
        Integer step = (Integer) payload.get("step");

        // AQUÍ corregimos el nombre para que use el de tu Service
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