package com.example.fingerprint_api.services;

import com.example.fingerprint_api.models.Visitante.VisitanteModel;
import com.example.fingerprint_api.repositories.VisitanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        // Establecer campos automáticos (igual que AlumnoService)
        visitante.setCreatedAt(LocalDateTime.now());
        visitante.setUpdateAt(LocalDateTime.now());
        visitante.setUuid(UUID.randomUUID().toString());
        visitante.setVersion(1);
        visitante.setDeleted(0);

        if(visitante.getActivo() == null) {
            visitante.setActivo("1");
        }

        return visitanteRepository.save(visitante);
    }

    // Obtener por ID y UUID
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

    // Buscar por procedencia
    public ArrayList<VisitanteModel> obtenerVisitantesPorProcedencia(String procedencia){
        return visitanteRepository.findByProcedenciaContainingIgnoreCase(procedencia);
    }

    // Validar QR Temporal (Simula la lectura del escáner)
    public Optional<VisitanteModel> validarQrTemporal(String qrCode) {

        // 1. Buscar si existe un visitante con ese QR activo
        Optional<VisitanteModel> visitanteOptional = visitanteRepository.findByQrTemporal(qrCode);

        if (visitanteOptional.isPresent()) {
            VisitanteModel visitante = visitanteOptional.get();
            LocalDateTime ahora = LocalDateTime.now();

            // 2. Verificar que no haya expirado
            if (visitante.getQrExpiracion() == null || ahora.isAfter(visitante.getQrExpiracion())) {
                // El QR ha expirado o no tiene fecha de expiración
                return Optional.empty();
            }

            // 3. QR Válido: Limpiar y actualizar campos (Para que no se use dos veces)
            visitante.setQrTemporal(null);
            visitante.setQrExpiracion(null);
            visitante.setUpdateAt(ahora);
            visitante.setVersion(visitante.getVersion() + 1);

            // Guardar los cambios (limpiando el QR)
            visitanteRepository.save(visitante);

            return Optional.of(visitante);
        }

        // No se encontró ningún visitante con ese código QR
        return Optional.empty();
    }

    // Obtener visitantes activos
    public ArrayList<VisitanteModel> obtenerVisitantesActivos(){
        return visitanteRepository.findByActivo("1");
    }

    // Obtener visitantes eliminados (soft)
    public ArrayList<VisitanteModel> obtenerVisitantesEliminados(){
        return visitanteRepository.findByDeleted(1);
    }

    // Contar total de visitantes
    public long contarTotalVisitantes(){
        return visitanteRepository.count();
    }

    // Actualizar por ID y UUID
    public VisitanteModel actualizarVisitanteSeguro(Integer id, String uuid, VisitanteModel detalles) {
        Optional<VisitanteModel> visitanteOptional = visitanteRepository.findById(id);
        if (visitanteOptional.isPresent() && visitanteOptional.get().getUuid().equals(uuid)) {
            VisitanteModel visitante = visitanteOptional.get();

            // Actualizar campos
            if(detalles.getPrimerNombre() != null) visitante.setPrimerNombre(detalles.getPrimerNombre());
            if(detalles.getSegundoNombre() != null) visitante.setSegundoNombre(detalles.getSegundoNombre());
            if(detalles.getApellidoPaterno() != null) visitante.setApellidoPaterno(detalles.getApellidoPaterno());
            if(detalles.getApellidoMaterno() != null) visitante.setApellidoMaterno(detalles.getApellidoMaterno());
            if(detalles.getNumTelefono() != null) visitante.setNumTelefono(detalles.getNumTelefono());
            if(detalles.getActivo() != null) visitante.setActivo(detalles.getActivo());
            if(detalles.getUsuario() != null) visitante.setUsuario(detalles.getUsuario());
            if(detalles.getProcedencia() != null) visitante.setProcedencia(detalles.getProcedencia());
            if(detalles.getMotivoVisita() != null) visitante.setMotivoVisita(detalles.getMotivoVisita());
            if(detalles.getIdentificacion() != null) visitante.setIdentificacion(detalles.getIdentificacion());

            // Si se incluyen campos de QR en la actualización, también se actualizan
            if(detalles.getQrTemporal() != null) visitante.setQrTemporal(detalles.getQrTemporal());
            if(detalles.getQrExpiracion() != null) visitante.setQrExpiracion(detalles.getQrExpiracion());

            // Campos automáticos en actualización
            visitante.setUpdateAt(LocalDateTime.now());
            visitante.setVersion(visitante.getVersion() + 1);

            return visitanteRepository.save(visitante);
        }
        return null;
    }

    // Generar QR Temporal por ID y UUID
    public VisitanteModel generarQrTemporalSeguro(Integer id, String uuid) {
        Optional<VisitanteModel> visitanteOptional = visitanteRepository.findById(id);

        if (visitanteOptional.isPresent() && visitanteOptional.get().getUuid().equals(uuid)) {
            VisitanteModel visitante = visitanteOptional.get();

            //Generar código QR temporal
            String qrCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            //Definir la expiración
            LocalDateTime expiracion = LocalDateTime.now().plusMinutes(5);

            visitante.setQrTemporal(qrCode);
            visitante.setQrExpiracion(expiracion);

            // 3. Campos automáticos de actualización
            visitante.setUpdateAt(LocalDateTime.now());
            visitante.setVersion(visitante.getVersion() + 1);

            return visitanteRepository.save(visitante);
        }
        return null;
    }


    // Restaurar visitante por ID y UUID
    public boolean restaurarVisitanteSeguro(Integer id, String uuid) {
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