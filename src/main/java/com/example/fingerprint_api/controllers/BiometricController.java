package com.example.fingerprint_api.controllers;

import com.example.fingerprint_api.components.FingerprintDeviceManager;
import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import com.example.fingerprint_api.services.AlumnoService;
import com.example.fingerprint_api.services.MultiReaderFingerprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/huella")
public class BiometricController {

    @Autowired
    private FingerprintDeviceManager deviceManager;

    @Autowired
    private AlumnoService alumnoService;

    // INYECCIÓN FALTANTE
    @Autowired
    private MultiReaderFingerprintService multiReaderService;

    // --- NUEVO ENDPOINT PARA IDENTIFICACIÓN ---
    // Este es el que llama tu frontend cuando pulsas "Escanear Huella"
    @PostMapping("/identify")
    public ResponseEntity<?> identificarHuella(
            @RequestParam(name = "numeroEntrada", defaultValue = "1") Integer numeroEntrada
    ) {
        try {
            // Pasamos la puerta al servicio
            AlumnoModel alumno = multiReaderService.identificarDedoAutomatico(numeroEntrada);

            if (alumno != null && alumno.getId() != null) {
                return ResponseEntity.ok(Map.of(
                        "mensaje", "Asistencia registrada",
                        "alumno", alumno.getPrimerNombre() + " " + alumno.getApellidoPaterno(),
                        "puerta", numeroEntrada
                ));
            } else if (alumno != null) {
                // Caso de timeout o lectura vacía (objeto no nulo pero sin ID)
                return ResponseEntity.status(408).body("Tiempo de espera agotado o lectura fallida");
            } else {
                return ResponseEntity.status(404).body("Huella no encontrada");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    // --- ENROLLMENT (EXISTENTE) ---
    @PostMapping("/iniciar-registro/{id}")
    public ResponseEntity<String> iniciarRegistro(@PathVariable Integer id) {
        if (!alumnoService.existeAlumno(id)) {
            return ResponseEntity.badRequest().body("El alumno no existe");
        }
        deviceManager.iniciarRegistro(id);
        return ResponseEntity.ok("Lector en modo REGISTRO. Por favor, solicite al alumno colocar su huella.");
    }

    @PostMapping("/cancelar-registro")
    public ResponseEntity<String> cancelarRegistro() {
        deviceManager.setMode(FingerprintDeviceManager.DeviceMode.IDENTIFICATION, null);
        return ResponseEntity.ok("Lector devuelto a modo IDENTIFICACIÓN.");
    }
}