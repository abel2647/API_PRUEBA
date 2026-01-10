package com.example.fingerprint_api.controllers;

import com.example.fingerprint_api.components.FingerprintDeviceManager;
import com.example.fingerprint_api.services.AlumnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/huella")
public class BiometricController {

    @Autowired
    private FingerprintDeviceManager deviceManager;

    @Autowired
    private AlumnoService alumnoService;

    /**
     * Endpoint para decirle al servidor: "El siguiente dedo que se ponga en el lector
     * es para el alumno con ID X".
     */
    @PostMapping("/iniciar-registro/{id}")
    public ResponseEntity<String> iniciarRegistro(@PathVariable Integer id) {
        if (!alumnoService.existeAlumno(id)) {
            return ResponseEntity.badRequest().body("El alumno no existe");
        }

        // Ponemos el hardware en modo registro
        deviceManager.iniciarRegistro(id);

        return ResponseEntity.ok("Lector en modo REGISTRO. Por favor, solicite al alumno (ID "+id+") colocar su huella.");
    }

    /**
     * Endpoint para cancelar registro y volver a modo lectura normal
     */
    @PostMapping("/cancelar-registro")
    public ResponseEntity<String> cancelarRegistro() {
        deviceManager.setMode(FingerprintDeviceManager.DeviceMode.IDENTIFICATION, null);
        return ResponseEntity.ok("Lector devuelto a modo IDENTIFICACIÓN.");
    }
}