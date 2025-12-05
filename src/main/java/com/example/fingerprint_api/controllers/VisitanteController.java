package com.example.fingerprint_api.controllers;

import com.example.fingerprint_api.models.Visitante.VisitanteModel;
import com.example.fingerprint_api.services.VisitanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/visitante")
@CrossOrigin(origins = "http://localhost:3000") // <-- SOLUCIÓN CORS (error con fetch por puertos)
public class VisitanteController {
    @Autowired
    VisitanteService visitanteService;

    // ========== GET ==========

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

    // ELIMINADOS: /procedencia, /activos

    // ========== POST ==========

    @PostMapping
    public VisitanteModel registrarVisitante(@RequestBody VisitanteModel visitante){
        return visitanteService.registrarVisitante(visitante);
    }



    // ELIMINADO: /qr/validar

    // ========== PUT ==========

    @PutMapping("/{uuid}/{id}")
    public ResponseEntity<VisitanteModel> actualizarVisitante(@PathVariable String uuid, @PathVariable Integer id, @RequestBody VisitanteModel visitante){
        VisitanteModel visitanteActualizado = visitanteService.actualizarVisitanteSeguro(id, uuid, visitante);
        return visitanteActualizado != null ? ResponseEntity.ok(visitanteActualizado) : ResponseEntity.notFound().build();
    }

    @PutMapping("/restaurar/{uuid}/{id}")
    public ResponseEntity<String> restaurarVisitante(@PathVariable String uuid, @PathVariable Integer id){
        boolean restaurado = visitanteService.restaurarVisitanteSeguro(id, uuid);
        return restaurado ? ResponseEntity.ok("Visitante restaurado") : ResponseEntity.notFound().build();
    }

    // ELIMINADO: /qr/generar

    // ========== DELETE ==========

    @DeleteMapping("/{uuid}/{id}")
    public ResponseEntity<String> eliminarVisitante(@PathVariable String uuid, @PathVariable Integer id){
        boolean eliminado = visitanteService.eliminarVisitanteSeguro(id, uuid);
        return eliminado ? ResponseEntity.ok("Visitante eliminado") : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/soft/{uuid}/{id}")
    public ResponseEntity<String> eliminarAlumnoSuave(@PathVariable String uuid, @PathVariable Integer id){
        boolean eliminado = visitanteService.eliminarVisitanteSuaveSeguro(id, uuid);
        return eliminado ? ResponseEntity.ok("Visitante eliminado (soft)") : ResponseEntity.notFound().build();
    }

}