package com.example.fingerprint_api.controllers;

import com.example.fingerprint_api.dtos.ValidacionResponseDTO;
import com.example.fingerprint_api.dtos.VisitanteRegistroDTO;
import com.example.fingerprint_api.models.Visitante.CodigoTemporalModel;
import com.example.fingerprint_api.models.Visitante.VisitanteModel;
import com.example.fingerprint_api.repositories.CodigoTemporalRepository;
import com.example.fingerprint_api.services.VisitanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/visitante") // Singular, coincide con tu frontend
@CrossOrigin(origins = "http://localhost:3000")
public class VisitanteController {

    @Autowired
    VisitanteService visitanteService;

    @Autowired
    CodigoTemporalRepository codigoTemporalRepository;

    // --- GETs DE LECTURA ---
    @GetMapping
    public ArrayList<VisitanteModel> obtenerVisitantes(){
        return visitanteService.obtenerVisitantes();
    }

    @GetMapping("/{uuid}/{id}")
    public ResponseEntity<VisitanteModel> obtenerVisitante(@PathVariable String uuid, @PathVariable Integer id){
        Optional<VisitanteModel> visitante = visitanteService.obtenerVisitantePorIdYUuid(id, uuid);
        return visitante.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar/{nombre}")
    public ArrayList<VisitanteModel> buscarVisitantesPorNombre(@PathVariable String nombre){
        return visitanteService.buscarVisitantesPorNombre(nombre);
    }

    @GetMapping("/eliminados")
    public ArrayList<VisitanteModel> obtenerVisitantesEliminados(){
        return visitanteService.obtenerVisitantesEliminados();
    }

    @GetMapping("/contar")
    public long contarVisitantes(){
        return visitanteService.contarTotalVisitantes();
    }

    // --- VALIDACIÓN DE PASE (EL QUE USA TU FRONTEND) ---
    // Este método lee ?numeroEntrada=X de la URL
    @GetMapping("/validar/{uuid}")
    public ResponseEntity<?> validarPase(
            @PathVariable String uuid,
            @RequestParam(name = "numeroEntrada", defaultValue = "1") Integer numeroEntrada
    ) {
        try {
            System.out.println("Validando UUID: " + uuid + " en Puerta: " + numeroEntrada);

            ValidacionResponseDTO response = visitanteService.validarPasePorUuid(uuid, numeroEntrada);

            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(404).body(Map.of("mensaje", "Código QR no encontrado"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // --- HISTORIAL ---
    @GetMapping("/historial")
    public ResponseEntity<List<Map<String, Object>>> buscarHistorial(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String paterno,
            @RequestParam(required = false) String materno,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora
    ) {
        LocalDateTime inicio = null;
        LocalDateTime fin = null;

        if (fecha != null) {
            if (hora != null) {
                inicio = LocalDateTime.of(fecha, hora);
                fin = LocalDateTime.of(fecha, hora).plusHours(1).minusSeconds(1);
            } else {
                inicio = fecha.atStartOfDay();
                fin = fecha.atTime(LocalTime.MAX);
            }
        }

        List<CodigoTemporalModel> listaCodigos = codigoTemporalRepository.buscarHistorialSinPaginacion(
                nombre, paterno, materno, inicio, fin
        );

        List<Map<String, Object>> respuesta = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();

        for (CodigoTemporalModel codigo : listaCodigos) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", codigo.getId_codigo());

            String paternoStr = codigo.getVisitante().getApellidoPaterno() != null ? codigo.getVisitante().getApellidoPaterno() : "";
            String maternoStr = codigo.getVisitante().getApellidoMaterno() != null ? codigo.getVisitante().getApellidoMaterno() : "";

            String nombreCompleto = codigo.getVisitante().getPrimerNombre() + " " + paternoStr + " " + maternoStr;
            item.put("nombreCompleto", nombreCompleto.trim());

            item.put("asunto", codigo.getAsunto());
            item.put("fechaEntrada", codigo.getFechaExpiracion().minusHours(2));

            // Validación de estatus con el campo 'activo'
            boolean isActivo = (codigo.getActivo() == null || codigo.getActivo() == 1);
            if (codigo.getFechaExpiracion().isAfter(ahora) && isActivo) {
                item.put("estatus", "Vigente");
            } else {
                item.put("estatus", "Expirado");
            }
            respuesta.add(item);
        }
        return ResponseEntity.ok(respuesta);
    }

    // --- POST: REGISTRO ---
    @PostMapping
    public ResponseEntity<Map<String, Object>> registrarVisitante(@RequestBody VisitanteRegistroDTO visitanteDto){
        Map<String, Object> resultado = visitanteService.registrarVisitanteCompleto(visitanteDto);
        return ResponseEntity.ok(resultado);
    }

    // Mantenemos este por compatibilidad si algún otro componente lo usa, pero el Frontend usa el GET de arriba
    @PostMapping("/validar-pase/{uuid}")
    public ResponseEntity<?> validarPaseLegacy(@PathVariable String uuid, @RequestBody Map<String, Integer> body) {
        try {
            Integer puerta = body.getOrDefault("puerta", 1);
            ValidacionResponseDTO response = visitanteService.validarPasePorUuid(uuid, puerta);
            if (response != null) return ResponseEntity.ok(response);
            return ResponseEntity.status(404).body("Código no encontrado");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // --- PUT: ACTUALIZACIÓN ---
    @PutMapping("/{uuid}/{id}")
    public ResponseEntity<VisitanteModel> actualizarVisitante(@PathVariable String uuid, @PathVariable Integer id, @RequestBody VisitanteModel visitante){
        VisitanteModel actualizado = visitanteService.actualizarVisitanteSeguro(id, uuid, visitante);
        return actualizado != null ? ResponseEntity.ok(actualizado) : ResponseEntity.notFound().build();
    }

    @PutMapping("/restaurar/{uuid}/{id}")
    public ResponseEntity<String> restaurarVisitante(@PathVariable String uuid, @PathVariable Integer id){
        boolean restaurado = visitanteService.restaurarVisitanteSeguro(id, uuid);
        return restaurado ? ResponseEntity.ok("Restaurado") : ResponseEntity.notFound().build();
    }

    // --- DELETE: BORRADO ---
    @DeleteMapping("/{uuid}/{id}")
    public ResponseEntity<String> eliminarVisitante(@PathVariable String uuid, @PathVariable Integer id){
        boolean eliminado = visitanteService.eliminarVisitanteSeguro(id, uuid);
        return eliminado ? ResponseEntity.ok("Eliminado") : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/soft/{uuid}/{id}")
    public ResponseEntity<String> eliminarSuave(@PathVariable String uuid, @PathVariable Integer id){
        boolean eliminado = visitanteService.eliminarVisitanteSuaveSeguro(id, uuid);
        return eliminado ? ResponseEntity.ok("Eliminado (Soft)") : ResponseEntity.notFound().build();
    }
}