package com.example.fingerprint_api.controllers;

import com.example.fingerprint_api.services.MultiReaderFingerprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class FingerprintTestController {

    @Autowired
    private MultiReaderFingerprintService fingerprintService;

    // Prueba rápida del back: http://localhost:8080/api/test/enrolar/1
    @GetMapping("/enrolar/{id}")
    public String probarEnrolamiento(@PathVariable Integer id) {
        System.out.println("Iniciando captura manual de prueba para ID: " + id);

        // 1. Obtenemos los lectores disponibles
        List<String> lectores = fingerprintService.refreshConnectedReaders();

        if (lectores.isEmpty()) {
            return "Error: No hay ningún lector conectado al equipo.";
        }

        // 2. Usamos el primer lector que encuentre para la prueba
        String primerLector = lectores.get(0);

        // 3. Llamamos al método enrolarPaso (usando el paso 3 para que guarde directo)
        return fingerprintService.enrolarAlumnoEnBaseDeDatos(primerLector, id, 3);
    }
}