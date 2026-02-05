package com.example.fingerprint_api.controllers;

import com.example.fingerprint_api.dtos.DashboardDTO;
import com.example.fingerprint_api.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/filtrado")
    public ResponseEntity<DashboardDTO> obtenerEstadisticasConFiltros(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String horaInicio,
            @RequestParam(required = false) String horaFin,
            @RequestParam(required = false, defaultValue = "TODOS") String tipo,
            @RequestParam(required = false) Integer puerta // Nuevo parámetro opcional
    ) {
        return ResponseEntity.ok(dashboardService.obtenerEstadisticasFiltradas(fecha, horaInicio, horaFin, tipo, puerta));
    }

    @GetMapping("/exportar-excel")
    public ResponseEntity<InputStreamResource> descargarReporteExcel(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String horaInicio,
            @RequestParam(required = false) String horaFin,
            @RequestParam(required = false, defaultValue = "TODOS") String tipo,
            @RequestParam(required = false) Integer puerta // Nuevo parámetro opcional
    ) {
        ByteArrayInputStream in = dashboardService.generarReporteExcel(fecha, horaInicio, horaFin, tipo, puerta);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte_asistencia.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}