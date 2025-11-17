package com.example.fingerprint_api.controllers;

import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import com.example.fingerprint_api.services.AlumnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/alumno")
public class AlumnoController  {
    @Autowired
    AlumnoService alumnoService;
    // ========== ENDPOINTS PÚBLICOS ==========

    // ========== GET ==========

    @GetMapping
    public ArrayList<AlumnoModel> obtenerAlumnos(){
        return alumnoService.obtenerAlumnos();
    }

    @GetMapping("/{uuid}/{id}")
    public ResponseEntity<AlumnoModel> obtenerAlumno(@PathVariable String uuid, @PathVariable Integer id){
        Optional<AlumnoModel> alumno = alumnoService.obtenerAlumnoPorIdYUuid(id, uuid);
        return alumno.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar/{nombre}")
    public ArrayList<AlumnoModel> buscarAlumnosPorNombre(@PathVariable String nombre){
        return alumnoService.buscarAlumnosPorNombre(nombre);
    }

    @GetMapping("/carrera/{carreraClave}")
    public ArrayList<AlumnoModel> obtenerAlumnosPorCarrera(@PathVariable String carreraClave){
        return alumnoService.obtenerAlumnosPorCarrera(carreraClave);
    }

    @GetMapping("/activos")
    public ArrayList<AlumnoModel> obtenerAlumnosActivos(){
        return alumnoService.obtenerAlumnosActivos();
    }

    @GetMapping("/eliminados")
    public ArrayList<AlumnoModel> obtenerAlumnosEliminados(){
        return alumnoService.obtenerAlumnosEliminados();
    }

    @GetMapping("/contar")
    public long contarAlumnos(){
        return alumnoService.contarTotalAlumnos();
    }

    // ========== POST ==========

    @PostMapping
    public AlumnoModel registrarAlumno(@RequestBody AlumnoModel alumno){
        return alumnoService.registrarAlumno(alumno);
    }

    // ========== PUT ==========

    @PutMapping("/{uuid}/{id}")
    public ResponseEntity<AlumnoModel> actualizarAlumno(@PathVariable String uuid, @PathVariable Integer id, @RequestBody AlumnoModel alumno){
        AlumnoModel alumnoActualizado = alumnoService.actualizarAlumnoSeguro(id, uuid, alumno);
        return alumnoActualizado != null ? ResponseEntity.ok(alumnoActualizado) : ResponseEntity.notFound().build();
    }

    @PutMapping("/restaurar/{uuid}/{id}")
    public ResponseEntity<String> restaurarAlumno(@PathVariable String uuid, @PathVariable Integer id){
        boolean restaurado = alumnoService.restaurarAlumnoSeguro(id, uuid);
        return restaurado ? ResponseEntity.ok("Alumno restaurado") : ResponseEntity.notFound().build();
    }

    // ========== DELETE ==========

    @DeleteMapping("/{uuid}/{id}")
    public ResponseEntity<String> eliminarAlumno(@PathVariable String uuid, @PathVariable Integer id){
        boolean eliminado = alumnoService.eliminarAlumnoSeguro(id, uuid);
        return eliminado ? ResponseEntity.ok("Alumno eliminado") : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/soft/{uuid}/{id}")
    public ResponseEntity<String> eliminarAlumnoSuave(@PathVariable String uuid, @PathVariable Integer id){
        boolean eliminado = alumnoService.eliminarAlumnoSuaveSeguro(id, uuid);
        return eliminado ? ResponseEntity.ok("Alumno eliminado (soft)") : ResponseEntity.notFound().build();
    }

   /*
    ANTIGUa}AS RUTAS
    @GetMapping()
    public ArrayList<AlumnoModel> obtenerAlumnos(){
        return alumnoService.obtenerAlumnos();
    }

    @PostMapping()
    public AlumnoModel registrarAlumno(@RequestBody AlumnoModel alumno){
        return this.alumnoService.registrarAlumno(alumno);
    }

    @DeleteMapping(path = "/{id}")
    public String eliminarPorId(@PathVariable("id") Integer id){
        boolean ok = this.alumnoService.eliminarAlumno(id);
        if(ok){
            return "Alumno eliminado con id"+id;
        }else {
            return "Alumno no eliminado con id"+id;
        }
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<AlumnoModel> actualizarAlumno(
            @PathVariable("id") Integer id,
            @RequestBody AlumnoModel alumnoActualizado) {

        AlumnoModel alumno = this.alumnoService.actualizarAlumno(id, alumnoActualizado);

        if (alumno != null) {
            return ResponseEntity.ok(alumno);
        } else {
            return ResponseEntity.notFound().build();
        }
    }*/

}
