package com.example.fingerprint_api.controllers;

import com.example.fingerprint_api.dtos.HistorialDTO;
import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import com.example.fingerprint_api.models.Asistencia.RegistroAsistenciaModel;
import com.example.fingerprint_api.services.AlumnoService;
import com.example.fingerprint_api.services.AsistenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/alumnos")
@CrossOrigin(origins = "*")
public class AlumnoController {

    @Autowired
    private AlumnoService alumnoService;

    @Autowired
    private AsistenciaService asistenciaService;

    // ==========================================
    //              ENDPOINTS GET
    // ==========================================

    @GetMapping
    public ArrayList<AlumnoModel> obtenerAlumnos() {
        return alumnoService.obtenerAlumnos();
    }

    @GetMapping("/{uuid}/{id}")
    public ResponseEntity<AlumnoModel> obtenerAlumno(@PathVariable String uuid, @PathVariable Integer id) {
        Optional<AlumnoModel> alumno = alumnoService.obtenerAlumnoPorIdYUuid(id, uuid);
        return alumno.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/asistencia/historial")
    public ResponseEntity<List<HistorialDTO>> getHistorial(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String paterno,
            @RequestParam(required = false) String matricula,
            @RequestParam(required = false) String fecha) { // <--- Nuevo parámetro

        List<HistorialDTO> historial = asistenciaService.obtenerHistorialEstudiantes(nombre, paterno, matricula, fecha);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/buscar/{nombre}")
    public ArrayList<AlumnoModel> buscarAlumnosPorNombre(@PathVariable String nombre) {
        return alumnoService.buscarAlumnosPorNombre(nombre);
    }



    @GetMapping("/contar")
    public long contarAlumnos() {
        return alumnoService.contarTotalAlumnos();
    }

    // ==========================================
    //              ENDPOINTS POST
    // ==========================================

    @PostMapping
    public ResponseEntity<AlumnoModel> registrarAlumno(@RequestBody AlumnoModel alumno){
        try {
            AlumnoModel nuevo = alumnoService.registrarAlumno(alumno);
            return ResponseEntity.ok(nuevo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint que recibe el JSON del ValidationAlumnoModal (Frontend)
     * Payload esperado: { "numeroControl": "...", "idEntrada": 1 }
     */

    @PostMapping("/registrar-asistencia")
    @CrossOrigin(origins = "*") // Crucial para evitar el "Failed to fetch"
    public ResponseEntity<?> registrarAsistenciaManual(@RequestBody Map<String, Object> payload) {
        try {
            // Extraemos las llaves que manda el JSON del front
            String numeroControl = (String) payload.get("numeroControl");
            Object idObj = payload.get("idEntrada");
            Integer idEntrada = (idObj instanceof String) ? Integer.parseInt((String) idObj) : (Integer) idObj;

            RegistroAsistenciaModel registro = asistenciaService.registrarPorNumeroControl(numeroControl, idEntrada);
            return ResponseEntity.ok(registro);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ==========================================
    //              ENDPOINTS PUT/DELETE
    // ==========================================

    @PutMapping("/{uuid}/{id}")
    public ResponseEntity<?> actualizarAlumnoSeguro(
            @PathVariable String uuid,
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload) { // Usamos Map para recibir cualquier campo

        try {
            AlumnoModel alumno = alumnoService.actualizarAlumnoDesdeMapa(id, uuid, payload);
            if (alumno != null) {
                return ResponseEntity.ok(alumno);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Alumno o UUID no coinciden");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{uuid}/{id}")
    public ResponseEntity<String> eliminarAlumno(@PathVariable String uuid, @PathVariable Integer id) {
        boolean eliminado = alumnoService.eliminarAlumnoSeguro(id, uuid);
        return eliminado ? ResponseEntity.ok("Alumno eliminado") : ResponseEntity.notFound().build();
    }
}