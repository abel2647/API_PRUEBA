package com.example.fingerprint_api.services;

import com.example.fingerprint_api.models.Visitante.VisitanteModel;
import com.example.fingerprint_api.repositories.VisitanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VisitanteService {

    @Autowired
    VisitanteRepository visitanteRepository;

    // Obtener todos los visitantes
    public ArrayList<VisitanteModel> obtenerVisitantes(){
        return (ArrayList<VisitanteModel>) visitanteRepository.findAll();
    }

    // Registrar un nuevo visitante (POST)
    public VisitanteModel registrarVisitante(VisitanteModel visitante){
        // Establecer campos automáticos
        visitante.setCreatedAt(LocalDateTime.now());
        visitante.setUpdateAt(LocalDateTime.now());
        visitante.setUuid(UUID.randomUUID().toString());
        visitante.setVersion(1);
        visitante.setDeleted(0);

        // ELIMINADO: La lógica para establecer 'activo' por defecto

        return visitanteRepository.save(visitante);
    }

    // Obtener por ID y UUID (Seguridad)
    public Optional<VisitanteModel> obtenerVisitantePorIdYUuid(Integer id, String uuid) {
        Optional<VisitanteModel> visitante = visitanteRepository.findById(id);
        if (visitante.isPresent() && visitante.get().getUuid().equals(uuid)) {
            return visitante;
        }
        return Optional.empty();
    }

    // Buscar por nombre
    public ArrayList<VisitanteModel> buscarVisitantesPorNombre(String nombre){
        return visitanteRepository.findByPrimerNombreContainingIgnoreCase(nombre);
    }

    // ELIMINADAS: obtenerVisitantesPorProcedencia, obtenerVisitantesActivos

    // Obtener visitantes eliminados (soft delete)
    public ArrayList<VisitanteModel> obtenerVisitantesEliminados(){
        return visitanteRepository.findByDeleted(1);
    }

    // Contar total de visitantes
    public long contarTotalVisitantes(){
        return visitanteRepository.count();
    }

    // Actualizar por ID y UUID (Seguridad)
    public VisitanteModel actualizarVisitanteSeguro(Integer id, String uuid, VisitanteModel detalles) {
        Optional<VisitanteModel> visitanteOptional = visitanteRepository.findById(id);
        if (visitanteOptional.isPresent() && visitanteOptional.get().getUuid().equals(uuid)) {
            VisitanteModel visitante = visitanteOptional.get();

            // Actualizar campos (Mismos que antes + el nuevo campo)
            if(detalles.getPrimerNombre() != null) visitante.setPrimerNombre(detalles.getPrimerNombre());
            // ... (resto de campos)

            // Nuevo campo
            if(detalles.getNumeroAcompañantes() != null) visitante.setNumeroAcompañantes(detalles.getNumeroAcompañantes());

            // ... (resto de campos automáticos)
            visitante.setUpdateAt(LocalDateTime.now());
            visitante.setVersion(visitante.getVersion() + 1);

            return visitanteRepository.save(visitante);
        }
        return null;
    }

    // ELIMINADO: generarQrTemporalSeguro
    // ELIMINADO: validarQrTemporal


    // Restaurar visitante por ID y UUID
    public boolean restaurarVisitanteSeguro(Integer id, String uuid) {
        // ... (el resto del código de soft delete y restauración es igual) ...
        try{
            Optional<VisitanteModel> visitanteOptional = visitanteRepository.findById(id);
            if(visitanteOptional.isPresent() && visitanteOptional.get().getUuid().equals(uuid)){
                VisitanteModel visitante = visitanteOptional.get();
                visitante.setDeleted(0);
                visitante.setUpdateAt(LocalDateTime.now());
                visitanteRepository.save(visitante);
                return true;
            }
            return false;
        }catch(Exception err){
            return false;
        }
    }

    // Eliminación física por ID y UUID
    public boolean eliminarVisitanteSeguro(Integer id, String uuid) {
        // ... (el resto del código es igual) ...
        try{
            Optional<VisitanteModel> visitanteOptional = visitanteRepository.findById(id);
            if(visitanteOptional.isPresent() && visitanteOptional.get().getUuid().equals(uuid)){
                visitanteRepository.deleteById(id);
                return true;
            }
            return false;
        }catch(Exception err){
            return false;
        }
    }

    // Eliminación suave (soft delete) por ID y UUID
    public boolean eliminarVisitanteSuaveSeguro(Integer id, String uuid) {
        // ... (el resto del código es igual) ...
        try{
            Optional<VisitanteModel> visitanteOptional = visitanteRepository.findById(id);
            if(visitanteOptional.isPresent() && visitanteOptional.get().getUuid().equals(uuid)){
                VisitanteModel visitante = visitanteOptional.get();
                visitante.setDeleted(1);
                visitante.setUpdateAt(LocalDateTime.now());
                visitanteRepository.save(visitante);
                return true;
            }
            return false;
        }catch(Exception err){
            return false;
        }
    }
}