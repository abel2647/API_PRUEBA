package com.example.fingerprint_api.controllers;

import com.example.fingerprint_api.dtos.ValidacionResponseDTO;
import com.example.fingerprint_api.dtos.VisitanteRegistroDTO;
import com.example.fingerprint_api.models.Visitante.CodigoTemporalModel;
import com.example.fingerprint_api.models.Visitante.VisitanteModel;
import com.example.fingerprint_api.repositories.CodigoTemporalRepository;
import com.example.fingerprint_api.services.VisitanteService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visitante")
@CrossOrigin(origins = "http://localhost:3000")
public class VisitanteController {

    @Autowired
    VisitanteService visitanteService;

    // --- GETs ---
    @GetMapping
    public ArrayList<VisitanteModel> obtenerVisitantes(){
        return visitanteService.obtenerVisitantes();
    }

    @GetMapping("/{uuid}/{id}")
    public ResponseEntity<VisitanteModel> obtenerVisitante(@PathVariable String uuid, @PathVariable Integer id){
        Optional<VisitanteModel> visitante = visitanteService.obtenerVisitantePorIdYUuid(id, uuid);
        return visitante.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

   /* @GetMapping("/validar/{uuid}")
    public ResponseEntity<VisitanteModel> validarQr(@PathVariable String uuid) {
        Optional<VisitanteModel> visitante = visitanteService.obtenerVisitantePorUuid(uuid);
        return visitante.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }*/

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

    // Este es el viejo
    /*
    @GetMapping("/validar/{uuid}")
    public ResponseEntity<?> validarQr(@PathVariable String uuid) {
        ValidacionResponseDTO resultado = visitanteService.validarPasePorUuid(uuid);

        if (resultado != null) {
            return ResponseEntity.ok(resultado);
        } else {
            // Devolvemos un JSON simple de error
            return ResponseEntity.status(404).body(Map.of("mensaje", "Código QR no encontrado"));
        }
    }

     */

    //Este es la segunda version estable
    /*
    @GetMapping("/validar/{uuid}")
    public ResponseEntity<?> validarQr(
            @PathVariable String uuid,
            //@RequestParam(required = false, defaultValue = "1") Integer entrada // <--- NUEVO PARÁMETRO
            @RequestParam(required = false) Integer entrada
    ) {
        // Pasamos la variable 'entrada' al servicio
        ValidacionResponseDTO resultado = visitanteService.validarPasePorUuid(uuid, entrada);

        if (resultado != null) {
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.status(404).body(Map.of("mensaje", "Código QR no encontrado"));
        }
    }
     */

    // --- VALIDACIÓN DE PASE (NUEVO MÉTODO) ---
    @GetMapping("/validar/{uuid}")
    public ResponseEntity<ValidacionResponseDTO> validarPase(
            @PathVariable String uuid,
            @RequestParam(required = false) Integer numeroEntrada
    ) {
        // 1. Si por alguna razón no llega el número, asignamos la puerta 1 por defecto
        if (numeroEntrada == null) {
            numeroEntrada = 1;
        }

        // 2. Llamamos al servicio pasando ambos datos
        ValidacionResponseDTO respuesta = visitanteService.validarPasePorUuid(uuid, numeroEntrada);

        // 3. Devolvemos la respuesta
        if (respuesta != null) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Autowired
    CodigoTemporalRepository codigoTemporalRepository;

    @GetMapping("/historial")
    public ResponseEntity<List<Map<String, Object>>> buscarHistorial(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String paterno,
            @RequestParam(required = false) String materno,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora
    ) {
        // 1. Lógica de fechas (igual que antes)
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

        // 2. Llamada al repositorio
        List<CodigoTemporalModel> listaCodigos = codigoTemporalRepository.buscarHistorialSinPaginacion(
                nombre, paterno, materno, inicio, fin
        );

        // 3. Transformación con cálculo de ESTATUS
        List<Map<String, Object>> respuesta = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now(); // Hora actual del servidor

        for (CodigoTemporalModel codigo : listaCodigos) {
            Map<String, Object> item = new HashMap<>();

            item.put("id", codigo.getId_codigo());

            String nombreCompleto = codigo.getVisitante().getPrimerNombre() + " " +
                    codigo.getVisitante().getApellidoPaterno() + " " +
                    (codigo.getVisitante().getApellidoMaterno() != null ? codigo.getVisitante().getApellidoMaterno() : "");
            item.put("nombreCompleto", nombreCompleto.trim());

            item.put("asunto", codigo.getAsunto());

            // Fecha de entrada (Expiración - 2 horas)
            item.put("fechaEntrada", codigo.getFechaExpiracion().minusHours(2));

            // --- NUEVO: CÁLCULO DE ESTATUS ---
            // Si la fecha de expiración es POSTERIOR a ahora, es Vigente.
            if (codigo.getFechaExpiracion().isAfter(ahora)) {
                item.put("estatus", "Vigente");
            } else {
                item.put("estatus", "Expirado");
            }

            respuesta.add(item);
        }

        return ResponseEntity.ok(respuesta);
    }


    // --- POST (El más importante) ---
    @PostMapping
    public ResponseEntity<Map<String, Object>> registrarVisitante(@RequestBody VisitanteRegistroDTO visitanteDto){
        Map<String, Object> resultado = visitanteService.registrarVisitanteCompleto(visitanteDto);
        return ResponseEntity.ok(resultado);
    }

    // --- PUT ---
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

    // --- DELETE ---
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