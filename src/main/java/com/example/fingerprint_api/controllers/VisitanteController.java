package com.example.fingerprint_api.controllers;

import com.example.fingerprint_api.dtos.VisitanteRegistroDTO;
import com.example.fingerprint_api.dtos.ValidacionResponseDTO;
import com.example.fingerprint_api.dtos.VisitanteResumenDTO;
import com.example.fingerprint_api.models.Visitante.VisitanteModel;
import com.example.fingerprint_api.services.VisitanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visitante")
@CrossOrigin(origins = "*") // Permite conexión desde cualquier Frontend
public class VisitanteController {

    @Autowired
    VisitanteService visitanteService;

    // 1. OBTENER LISTA SIMPLE (GET)
    @GetMapping
    public List<VisitanteModel> obtenerVisitantes() {
        return visitanteService.obtenerVisitantes();
    }

    // 2. REGISTRAR VISITANTE (POST)
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, Object>> registrarVisitante(@RequestBody VisitanteRegistroDTO dto) {
        try {
            Map<String, Object> resultado = visitanteService.registrarVisitanteCompleto(dto);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al registrar: " + e.getMessage()));
        }
    }
    @PostMapping("/salida")
    public ResponseEntity<ValidacionResponseDTO> registrarSalida(@RequestBody Map<String, String> body) {
        String uuid = body.get("uuid");
        ValidacionResponseDTO respuesta = visitanteService.registrarSalidaPorUuid(uuid);
        return ResponseEntity.ok(respuesta);
    }

    // 3. HISTORIAL COMPLETO (GET) - Para la tabla nueva
    @GetMapping("/historial")
    public List<VisitanteResumenDTO> obtenerHistorial() {
        return visitanteService.obtenerHistorialResumen();
    }

    // 4. VALIDAR / ESCANEAR CÓDIGO (POST)
    @PostMapping("/validar")
    public ResponseEntity<ValidacionResponseDTO> validarPase(@RequestBody Map<String, Object> payload) {
        String uuid = (String) payload.get("uuid");
        // Leemos la puerta, si no viene, asumimos puerta 1
        int puerta = payload.containsKey("puerta") ? Integer.parseInt(payload.get("puerta").toString()) : 1;

        ValidacionResponseDTO response = visitanteService.validarPasePorUuid(uuid, puerta);
        return ResponseEntity.ok(response);
    }

    // 5. ELIMINAR (DELETE) - Baja lógica
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarVisitante(@PathVariable Integer id) {
        boolean eliminado = visitanteService.eliminarVisitanteSeguro(id);
        if (eliminado) {
            return ResponseEntity.ok("Visitante eliminado correctamente");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 6. RESTAURAR (PUT) - Reactivar usuario
    @PutMapping("/restaurar/{id}")
    public ResponseEntity<String> restaurarVisitante(@PathVariable Integer id) {
        boolean restaurado = visitanteService.restaurarVisitanteSeguro(id);
        if (restaurado) {
            return ResponseEntity.ok("Visitante restaurado correctamente");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    // 2. ANTIGUO (Respaldo): Por si pierden el QR y necesitas cerrarlo por ID
    @PutMapping("/expirar/{id}")
    public ResponseEntity<?> expirarManualmente(@PathVariable Integer id) {
        boolean exito = visitanteService.forzarExpiracion(id);
        if (exito) {
            return ResponseEntity.ok(Map.of("mensaje", "Visita finalizada manualmente (Respaldo)"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "No se pudo finalizar"));
        }
    }
}