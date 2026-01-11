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
    @Transactional // Si falla algo, revierte todo para no dejar datos basura
    public Map<String, Object> registrarVisitanteCompleto(VisitanteRegistroDTO dto){
        LocalDateTime now = LocalDateTime.now();
        VisitanteModel visitante;

        // PASO A: Verificar si la persona ya existe (por teléfono)
        // Esto evita tener 50 veces a "Juan Perez" en la base de datos
        Optional<VisitanteModel> existente = visitanteRepository.findByNumTelefono(dto.getNumTelefono());

        if(existente.isPresent()){
            visitante = existente.get();
            // Opcional: Actualizar datos si cambiaron (ej: edad)
            visitante.setUpdateAt(now);
            visitanteRepository.save(visitante);
        } else {
            // Si es nuevo, creamos el registro
            visitante = new VisitanteModel();
            visitante.setPrimerNombre(dto.getPrimerNombre());
            visitante.setApellidoPaterno(dto.getApellidoPaterno());
            visitante.setApellidoMaterno(dto.getApellidoMaterno());
            visitante.setSexo(dto.getSexo());
            visitante.setEdad(dto.getEdad());
            visitante.setNumTelefono(dto.getNumTelefono());
            visitante.setUsuario(dto.getUsuario());
            visitante.setCreatedAt(now);
            visitante.setUpdateAt(now);
            visitante.setDeleted(0);

            visitante = visitanteRepository.save(visitante);
        }

        // PASO B: Crear el Pase (Código Temporal)
        // Esto SIEMPRE se crea nuevo, cada visita es un pase distinto
        CodigoTemporalModel codigo = new CodigoTemporalModel();
        codigo.setAsunto(dto.getAsunto());
        codigo.setNumeroAcompañantes(dto.getNumeroAcompañantes());
        codigo.setFechaExpiracion(now.plusHours(2)); // El pase dura 1 hora

        // Generar UUID limpio
        String safeUuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        codigo.setUuid(safeUuid);

        // Relacionar el código con la persona
        codigo.setVisitante(visitante);

        codigoTemporalRepository.save(codigo);

        // PASO C: Preparar respuesta para React
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("id_visitante", visitante.getId_visitante());
        respuesta.put("primerNombre", visitante.getPrimerNombre());
        respuesta.put("apellidoPaterno", visitante.getApellidoPaterno());
        // ¡OJO! Enviamos el UUID del pase generado
        respuesta.put("uuid", safeUuid);

        return respuesta;
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

    /*
    public ValidacionResponseDTO validarPasePorUuid(String uuid) {
        // 1. Buscamos en la tabla de códigos (Pases)
        Optional<CodigoTemporalModel> codigoOpt = codigoTemporalRepository.findByUuid(uuid);

        ValidacionResponseDTO respuesta = new ValidacionResponseDTO();

        if (codigoOpt.isPresent()) {
            CodigoTemporalModel codigo = codigoOpt.get();
            VisitanteModel visitante = codigo.getVisitante();

            // 2. Llenamos datos PERSONALES (desde VisitanteModel)
            respuesta.setPrimerNombre(visitante.getPrimerNombre());
            respuesta.setApellidoPaterno(visitante.getApellidoPaterno());

            // 3. Llenamos datos del PASE (desde CodigoTemporalModel)
            // IMPORTANTE: Aquí corregimos el error. El asunto y acompañantes viven en el código, no en la persona.
            respuesta.setAsunto(codigo.getAsunto());
            respuesta.setNumeroAcompañantes(codigo.getNumeroAcompañantes());
            respuesta.setUuid(codigo.getUuid());
            respuesta.setFechaExpiracion(codigo.getFechaExpiracion()); // O getFechaExpiracion() según tu modelo

            // 4. Lógica de Validación (Verde o Rojo)
            LocalDateTime ahora = LocalDateTime.now();

            // Verificamos si expiró y si el usuario no ha sido borrado
            if (codigo.getFechaExpiracion().isAfter(ahora) && visitante.getDeleted() == 0) {
                respuesta.setEsValido(true);
                respuesta.setMensaje("Acceso Autorizado");
            } else {
                respuesta.setEsValido(false);
                if (visitante.getDeleted() == 1) {
                    respuesta.setMensaje("El visitante ha sido eliminado del sistema");
                } else {
                    respuesta.setMensaje("El pase ha expirado");
                }
            }
            return respuesta;
        } else {
            return null; // UUID no existe
        }
    }
    */
/*
    public ValidacionResponseDTO validarPasePorUuid(String uuid) {
        // 1. Buscamos en la tabla de códigos (Pases)
        Optional<CodigoTemporalModel> codigoOpt = codigoTemporalRepository.findByUuid(uuid);

        ValidacionResponseDTO respuesta = new ValidacionResponseDTO();

        if (codigoOpt.isPresent()) {
            CodigoTemporalModel codigo = codigoOpt.get();
            VisitanteModel visitante = codigo.getVisitante();
            LocalDateTime ahora = LocalDateTime.now();

            // 2. Llenamos datos PERSONALES
            respuesta.setPrimerNombre(visitante.getPrimerNombre());
            respuesta.setApellidoPaterno(visitante.getApellidoPaterno());

            // 3. Llenamos datos del PASE
            respuesta.setAsunto(codigo.getAsunto());
            respuesta.setNumeroAcompañantes(codigo.getNumeroAcompañantes());
            respuesta.setUuid(codigo.getUuid());
            respuesta.setFechaExpiracion(codigo.getFechaExpiracion());

            // 4. Lógica de Validación
            // Verificamos si expiró y si el usuario no ha sido borrado
            if (codigo.getFechaExpiracion().isAfter(ahora) && visitante.getDeleted() == 0) {
                respuesta.setEsValido(true);
                respuesta.setMensaje("Acceso Autorizado");

                // --- NUEVO: GUARDAR EN HISTORIAL DE ENTRADAS ---
                // Solo guardamos si es válido
                try {
                    com.example.fingerprint_api.models.RegistroEntradaVisitanteModel registro = new com.example.fingerprint_api.models.RegistroEntradaVisitanteModel();
                    registro.setCodigoTemporal(codigo); // Relación con el ID del código
                    registro.setEntrada("P1E1");        // Puerta 1 Entrada 1 (Hardcodeado como pediste)
                    registro.setFechaHora(ahora);       // Fecha y hora actual

                    registroEntradaRepository.save(registro);
                } catch (Exception e) {
                    System.out.println("Error al guardar el registro de entrada: " + e.getMessage());
                    // No detenemos la validación si falla el log, pero es bueno saberlo
                }
                // -----------------------------------------------

            } else {
                respuesta.setEsValido(false);
                if (visitante.getDeleted() == 1) {
                    respuesta.setMensaje("El visitante ha sido eliminado del sistema");
                } else {
                    respuesta.setMensaje("El pase ha expirado");
                }
            }
            return respuesta;
        } else {
            return null; // UUID no existe
        }
    }
    */

    //Este es el bueno, pero sin elegir puerta
    /*
    public ValidacionResponseDTO validarPasePorUuid(String uuid) {
        // 1. El frontend manda el texto (UUID), aquí buscamos el objeto completo (que tiene el ID)
        Optional<CodigoTemporalModel> codigoOpt = codigoTemporalRepository.findByUuid(uuid);

        ValidacionResponseDTO respuesta = new ValidacionResponseDTO();

        if (codigoOpt.isPresent()) {
            CodigoTemporalModel codigo = codigoOpt.get(); // <- ESTE OBJETO YA TIENE EL ID (id_codigo)
            VisitanteModel visitante = codigo.getVisitante();
            LocalDateTime ahora = LocalDateTime.now();

            // Llenar datos de respuesta
            respuesta.setPrimerNombre(visitante.getPrimerNombre());
            respuesta.setApellidoPaterno(visitante.getApellidoPaterno());
            respuesta.setAsunto(codigo.getAsunto());
            respuesta.setNumeroAcompañantes(codigo.getNumeroAcompañantes());
            respuesta.setUuid(codigo.getUuid());
            respuesta.setFechaExpiracion(codigo.getFechaExpiracion());

            // 2. VALIDACIÓN
            if (codigo.getFechaExpiracion().isAfter(ahora) && visitante.getDeleted() == 0) {
                respuesta.setEsValido(true);
                respuesta.setMensaje("Acceso Autorizado");

                // --- AQUÍ GUARDAMOS EL REGISTRO ---
                try {
                    // Ya no usamos el nombre largo "com.example...", solo el nombre de la clase
                    RegistroEntradaVisitanteModel registro = new RegistroEntradaVisitanteModel();

                    registro.setCodigoTemporal(codigo);
                    registro.setEntrada(1);
                    registro.setFechaHora(ahora);

                    registroEntradaRepository.save(registro);

                    System.out.println("Registro guardado para UUID: " + uuid);
                } catch (Exception e) {
                    System.out.println("Error al guardar entrada: " + e.getMessage());
                }
                // ----------------------------------
            } else {
                respuesta.setEsValido(false);
                if (visitante.getDeleted() == 1) {
                    respuesta.setMensaje("El visitante ha sido eliminado del sistema");
                } else {
                    respuesta.setMensaje("El pase ha expirado");
                }
            }
            return respuesta;
        } else {
            return null; // UUID no encontrado
        }
    }

     */

    // Modifica la definición del método para recibir "Integer numeroEntrada"
    public ValidacionResponseDTO validarPasePorUuid(String uuid, Integer numeroEntrada) {

        Optional<CodigoTemporalModel> codigoOpt = codigoTemporalRepository.findByUuid(uuid);
        ValidacionResponseDTO respuesta = new ValidacionResponseDTO();

        if (codigoOpt.isPresent()) {
            CodigoTemporalModel codigo = codigoOpt.get();
            VisitanteModel visitante = codigo.getVisitante();
            LocalDateTime ahora = LocalDateTime.now();

            // Llenar datos de respuesta (Igual que antes...)
            respuesta.setPrimerNombre(visitante.getPrimerNombre());
            respuesta.setApellidoPaterno(visitante.getApellidoPaterno());
            respuesta.setAsunto(codigo.getAsunto());
            respuesta.setNumeroAcompañantes(codigo.getNumeroAcompañantes());
            respuesta.setUuid(codigo.getUuid());
            respuesta.setFechaExpiracion(codigo.getFechaExpiracion());

            // VALIDACIÓN
            if (codigo.getFechaExpiracion().isAfter(ahora) && visitante.getDeleted() == 0) {
                respuesta.setEsValido(true);
                respuesta.setMensaje("Acceso Autorizado");

                // --- AQUÍ GUARDAMOS EL REGISTRO ---
                try {
                    RegistroEntradaVisitanteModel registro = new RegistroEntradaVisitanteModel();

                    registro.setCodigoTemporal(codigo);

                    // USAMOS LA VARIABLE QUE RECIBIMOS COMO PARÁMETRO
                    registro.setEntrada(numeroEntrada);

                    registro.setFechaHora(ahora);

                    registroEntradaRepository.save(registro);
                    System.out.println("Entrada registrada en Puerta " + numeroEntrada + " para UUID: " + uuid);
                } catch (Exception e) {
                    System.out.println("Error al guardar entrada: " + e.getMessage());
                }
                // ----------------------------------
            } else {
                respuesta.setEsValido(false);
                // ... lógica de error (igual que antes)
            }
            return respuesta;
        } else {
            return null;
        }
    }

}