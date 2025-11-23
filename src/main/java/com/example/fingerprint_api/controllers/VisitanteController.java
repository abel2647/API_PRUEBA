package com.example.fingerprint_api.controllers;

import com.example.fingerprint_api.models.Visitante.VisitanteModel;
import com.example.fingerprint_api.services.VisitanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/visitante")
public class VisitanteController {
    @Autowired
    VisitanteService visitanteService;

    //Get

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

    @GetMapping("/procedencia/{procedencia}")
    public ArrayList<VisitanteModel> obtenerVisitantesPorProcedencia(@PathVariable String procedencia){
        return visitanteService.obtenerVisitantesPorProcedencia(procedencia);
    }

    @GetMapping("/activos")
    public ArrayList<VisitanteModel> obtenerVisitantesActivos(){
        return visitanteService.obtenerVisitantesActivos();
    }

    @GetMapping("/eliminados")
    public ArrayList<VisitanteModel> obtenerVisitantesEliminados(){
        return visitanteService.obtenerVisitantesEliminados();
    }

    @GetMapping("/contar")
    public long contarVisitantes(){
        return visitanteService.contarTotalVisitantes();
    }

    //Post

    @PostMapping
    public VisitanteModel registrarVisitante(@RequestBody VisitanteModel visitante){
        return visitanteService.registrarVisitante(visitante);
    }

    @PostMapping("/qr/validar")
    public ResponseEntity<VisitanteModel> validarQrTemporal(@RequestBody String qrTemporal){
        // El @RequestBody String qrTemporal recibirá el código puro en el cuerpo de la petición
        Optional<VisitanteModel> visitante = visitanteService.validarQrTemporal(qrTemporal.trim());

        // Si el QR es válido, devuelve el visitante (200 OK)
        return visitante.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    //Put

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

    @PutMapping("/qr/generar/{uuid}/{id}")
    public ResponseEntity<VisitanteModel> generarQrTemporal(@PathVariable String uuid, @PathVariable Integer id){
        VisitanteModel visitanteActualizado = visitanteService.generarQrTemporalSeguro(id, uuid);
        return visitanteActualizado != null ? ResponseEntity.ok(visitanteActualizado) : ResponseEntity.notFound().build();
    }

    // ========== DELETE ==========

    @DeleteMapping("/{uuid}/{id}")
    public ResponseEntity<String> eliminarVisitante(@PathVariable String uuid, @PathVariable Integer id){
        boolean eliminado = visitanteService.eliminarVisitanteSeguro(id, uuid);
        return eliminado ? ResponseEntity.ok("Visitante eliminado") : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/soft/{uuid}/{id}")
    public ResponseEntity<String> eliminarVisitanteSuave(@PathVariable String uuid, @PathVariable Integer id){
        boolean eliminado = visitanteService.eliminarVisitanteSuaveSeguro(id, uuid);
        return eliminado ? ResponseEntity.ok("Visitante eliminado (soft)") : ResponseEntity.notFound().build();
    }
}