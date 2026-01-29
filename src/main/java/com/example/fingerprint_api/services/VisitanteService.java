package com.example.fingerprint_api.services;

import com.example.fingerprint_api.dtos.VisitanteRegistroDTO;
import com.example.fingerprint_api.dtos.ValidacionResponseDTO;
import com.example.fingerprint_api.models.Visitante.CodigoTemporalModel;
import com.example.fingerprint_api.models.Visitante.RegistroEntradaVisitanteModel;
import com.example.fingerprint_api.models.Visitante.VisitanteModel;
import com.example.fingerprint_api.repositories.CodigoTemporalRepository;
import com.example.fingerprint_api.repositories.RegistroEntradaVisitanteRepository;
import com.example.fingerprint_api.repositories.VisitanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class VisitanteService {

    @Autowired
    VisitanteRepository visitanteRepository;

    @Autowired
    CodigoTemporalRepository codigoTemporalRepository;

    @Autowired
    RegistroEntradaVisitanteRepository registroEntradaRepository;


    // ==========================================
    // 1. REGISTRO INTELIGENTE (DTO -> 2 TABLAS)
    // ==========================================
    // 1. En registrarVisitanteCompleto (Asegura que nazca activo)
    @Transactional
    public Map<String, Object> registrarVisitanteCompleto(VisitanteRegistroDTO dto) {
        // 1. DEFINIMOS LA VARIABLE 'now' QUE FALTABA
        LocalDateTime now = LocalDateTime.now();

        VisitanteModel visitante;

        // PASO A: Verificar si la persona ya existe
        Optional<VisitanteModel> existente = visitanteRepository.findByNumTelefono(dto.getNumTelefono());

        if (existente.isPresent()) {
            visitante = existente.get();
            visitante.setUpdateAt(now); // Usamos 'now'
            visitanteRepository.save(visitante);
        } else {
            visitante = new VisitanteModel();
            visitante.setPrimerNombre(dto.getPrimerNombre());
            visitante.setApellidoPaterno(dto.getApellidoPaterno());
            visitante.setApellidoMaterno(dto.getApellidoMaterno());
            visitante.setSexo(dto.getSexo());
            visitante.setEdad(dto.getEdad());
            visitante.setNumTelefono(dto.getNumTelefono());
            visitante.setUsuario(dto.getUsuario());
            visitante.setCreatedAt(now); // Usamos 'now'
            visitante.setUpdateAt(now);  // Usamos 'now'
            visitante.setDeleted(0);

            visitante = visitanteRepository.save(visitante);
        }

        // PASO B: Crear el Pase (Código Temporal)
        CodigoTemporalModel codigo = new CodigoTemporalModel();
        codigo.setAsunto(dto.getAsunto());
        codigo.setNumeroAcompañantes(dto.getNumeroAcompañantes());

        // Aquí es donde fallaba antes si no tenías 'now' definido
        codigo.setFechaExpiracion(now.plusHours(2));

        // IMPORTANTE: Recuerda que debes haber agregado el campo 'activo' en tu modelo CodigoTemporalModel
        codigo.setActivo(1);

        String safeUuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        codigo.setUuid(safeUuid);
        codigo.setVisitante(visitante);

        codigoTemporalRepository.save(codigo);

        // PASO C: Respuesta
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("id_visitante", visitante.getId_visitante());
        respuesta.put("primerNombre", visitante.getPrimerNombre());
        respuesta.put("apellidoPaterno", visitante.getApellidoPaterno());
        respuesta.put("uuid", safeUuid);

        return respuesta;
    }

    // 2. El método de validación CON LÓGICA PEREZOSA
    public ValidacionResponseDTO validarPasePorUuid(String uuid, Integer numeroEntrada) {
        Optional<CodigoTemporalModel> codigoOpt = codigoTemporalRepository.findByUuid(uuid);
        ValidacionResponseDTO respuesta = new ValidacionResponseDTO();

        if (codigoOpt.isPresent()) {
            CodigoTemporalModel codigo = codigoOpt.get();
            VisitanteModel visitante = codigo.getVisitante();
            LocalDateTime ahora = LocalDateTime.now();

            // Llenar datos de respuesta
            respuesta.setPrimerNombre(visitante.getPrimerNombre());
            respuesta.setApellidoPaterno(visitante.getApellidoPaterno());
            respuesta.setAsunto(codigo.getAsunto());
            respuesta.setNumeroAcompañantes(codigo.getNumeroAcompañantes());
            respuesta.setUuid(codigo.getUuid());
            respuesta.setFechaExpiracion(codigo.getFechaExpiracion());

            // === VALIDACIÓN ROBUSTA ===

            // 1. Chequeo de seguridad: ¿El pase ya fue desactivado manualmente o por uso previo?
            if (codigo.getActivo() != null && codigo.getActivo() == 0) {
                respuesta.setEsValido(false);
                respuesta.setMensaje("El pase ha sido desactivado o ya expiró.");
                return respuesta;
            }

            // 2. Chequeo de Fecha (Lazy Validation)
            // Si la fecha ya pasó, actualizamos la BD a activo = 0 para bloquearlo para siempre
            if (codigo.getFechaExpiracion().isBefore(ahora)) {
                codigo.setActivo(0);
                codigoTemporalRepository.save(codigo); // <--- AQUÍ ACTUALIZAMOS LA BD

                respuesta.setEsValido(false);
                respuesta.setMensaje("El pase ha expirado (Fecha límite: " + codigo.getFechaExpiracion() + ")");
                return respuesta;
            }

            // 3. Chequeo de Visitante Borrado
            if (visitante.getDeleted() == 1) {
                respuesta.setEsValido(false);
                respuesta.setMensaje("El visitante fue eliminado del sistema.");
                return respuesta;
            }

            // SI PASA TODO LO ANTERIOR -> ACCESO CONCEDIDO
            respuesta.setEsValido(true);
            respuesta.setMensaje("Acceso Autorizado");

            try {
                RegistroEntradaVisitanteModel registro = new RegistroEntradaVisitanteModel();
                registro.setCodigoTemporal(codigo);
                registro.setEntrada(numeroEntrada);
                registro.setFechaHora(ahora);
                registroEntradaRepository.save(registro);
                System.out.println("Entrada registrada para UUID: " + uuid);
            } catch (Exception e) {
                System.out.println("Error log: " + e.getMessage());
            }

            return respuesta;
        } else {
            return null;
        }
    }

    // ==========================================
    // 2. MÉTODOS DE LECTURA (Adaptados)
    // ==========================================

    public ArrayList<VisitanteModel> obtenerVisitantes(){
        return (ArrayList<VisitanteModel>) visitanteRepository.findAll();
    }

    public long contarTotalVisitantes() {
        return visitanteRepository.count();
    }

    public ArrayList<VisitanteModel> obtenerVisitantesEliminados() {
        return visitanteRepository.findByDeleted(1);
    }

    public ArrayList<VisitanteModel> buscarVisitantesPorNombre(String nombre) {
        return visitanteRepository.findByPrimerNombreContainingIgnoreCase(nombre);
    }

    // Buscar visitante a través del código QR (UUID)
    // Útil para validar en la entrada o eliminar/editar basado en el QR actual
    public Optional<VisitanteModel> obtenerVisitantePorIdYUuid(Integer id, String uuid) {
        Optional<CodigoTemporalModel> codigoOpt = codigoTemporalRepository.findByUuid(uuid);

        if (codigoOpt.isPresent()) {
            VisitanteModel v = codigoOpt.get().getVisitante();
            if (v.getId_visitante().equals(id)) {
                return Optional.of(v);
            }
        }
        return Optional.empty();
    }

    // Solo validar QR (para el guardia)
    public Optional<VisitanteModel> obtenerVisitantePorUuid(String uuid) {
        return codigoTemporalRepository.findByUuid(uuid)
                .map(CodigoTemporalModel::getVisitante);
    }

    // ==========================================
    // 3. EDICIÓN Y BORRADO (Adaptados)
    // ==========================================

    public VisitanteModel actualizarVisitanteSeguro(Integer id, String uuid, VisitanteModel datosNuevos) {
        Optional<VisitanteModel> vOpt = obtenerVisitantePorIdYUuid(id, uuid);
        if (vOpt.isPresent()) {
            VisitanteModel v = vOpt.get();
            v.setPrimerNombre(datosNuevos.getPrimerNombre());
            v.setApellidoPaterno(datosNuevos.getApellidoPaterno());
            v.setApellidoMaterno(datosNuevos.getApellidoMaterno());
            v.setNumTelefono(datosNuevos.getNumTelefono());
            v.setSexo(datosNuevos.getSexo());
            v.setEdad(datosNuevos.getEdad());
            v.setUpdateAt(LocalDateTime.now());
            return visitanteRepository.save(v);
        }
        return null;
    }

    public boolean eliminarVisitanteSuaveSeguro(Integer id, String uuid) {
        Optional<VisitanteModel> vOpt = obtenerVisitantePorIdYUuid(id, uuid);
        if(vOpt.isPresent()){
            VisitanteModel v = vOpt.get();
            v.setDeleted(1);
            v.setUpdateAt(LocalDateTime.now());
            visitanteRepository.save(v);
            return true;
        }
        return false;
    }

    public boolean restaurarVisitanteSeguro(Integer id, String uuid) {
        Optional<VisitanteModel> vOpt = obtenerVisitantePorIdYUuid(id, uuid);
        if(vOpt.isPresent()){
            VisitanteModel v = vOpt.get();
            v.setDeleted(0);
            v.setUpdateAt(LocalDateTime.now());
            visitanteRepository.save(v);
            return true;
        }
        return false;
    }

    public boolean eliminarVisitanteSeguro(Integer id, String uuid) {
        // Para borrado físico, primero buscamos el código específico
        Optional<CodigoTemporalModel> codigoOpt = codigoTemporalRepository.findByUuid(uuid);
        if(codigoOpt.isPresent()){
            CodigoTemporalModel codigo = codigoOpt.get();
            if(codigo.getVisitante().getId_visitante().equals(id)){
                // Borramos el código primero para liberar la FK
                codigoTemporalRepository.delete(codigo);
                // Borramos el visitante
                visitanteRepository.deleteById(id);
                return true;
            }
        }
        return false;
    }

}