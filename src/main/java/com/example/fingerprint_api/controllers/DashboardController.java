package com.example.fingerprint_api.controllers;

import com.example.fingerprint_api.dtos.DashboardDTO;
import com.example.fingerprint_api.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
// Permite peticiones desde cualquier origen (ajusta esto si ya tienes una config global de CORS)
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Endpoint para obtener todas las estadísticas del dashboard.
     * URL: GET http://localhost:8080/api/dashboard/kpis
     */
   /* @GetMapping("/kpis")
    public ResponseEntity<DashboardDTO> obtenerEstadisticasGeneral() {
        try {
            DashboardDTO stats = dashboardService.obtenerEstadisticas();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            // Manejo básico de errores para no romper el frontend
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    */
    @GetMapping("/filtrado")
    public ResponseEntity<DashboardDTO> obtenerEstadisticasConFiltros(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String horaInicio,
            @RequestParam(required = false) String horaFin,
            @RequestParam(required = false, defaultValue = "TODOS") String tipo
    ) {
        return ResponseEntity.ok(dashboardService.obtenerEstadisticasFiltradas(fecha, horaInicio, horaFin, tipo));
    }
}