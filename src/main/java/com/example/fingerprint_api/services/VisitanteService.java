package com.example.fingerprint_api.services;

import com.example.fingerprint_api.dtos.VisitanteRegistroDTO;
import com.example.fingerprint_api.dtos.ValidacionResponseDTO;
import com.example.fingerprint_api.dtos.VisitanteResumenDTO;
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
    // 1. REGISTRO (Crea o Actualiza)
    // ==========================================
    @Transactional
    public Map<String, Object> registrarVisitanteCompleto(VisitanteRegistroDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        VisitanteModel visitante;

        // Buscamos por teléfono para evitar duplicados
        Optional<VisitanteModel> existente = visitanteRepository.findByNumTelefono(dto.getNumTelefono());

        if (existente.isPresent()) {
            // ACTUALIZAR EXISTENTE
            visitante = existente.get();
            visitante.setPrimerNombre(dto.getPrimerNombre());
            visitante.setApellidoPaterno(dto.getApellidoPaterno());
            visitante.setApellidoMaterno(dto.getApellidoMaterno());
            visitante.setUpdateAt(now);
            // Si estaba eliminado, lo reactivamos
            if (visitante.getDeleted() != null && visitante.getDeleted() == 1) {
                visitante.setDeleted(0);
            }
            visitanteRepository.save(visitante);
        } else {
            // CREAR NUEVO
            visitante = new VisitanteModel();
            visitante.setPrimerNombre(dto.getPrimerNombre());
            visitante.setApellidoPaterno(dto.getApellidoPaterno());
            visitante.setApellidoMaterno(dto.getApellidoMaterno());
            visitante.setSexo(dto.getSexo());
            visitante.setEdad(dto.getEdad());
            visitante.setNumTelefono(dto.getNumTelefono());
            visitante.setUsuario(dto.getUsuario());

            // Datos de auditoría
            visitante.setCreatedAt(now);
            visitante.setUpdateAt(now);
            visitante.setDeleted(0);

            visitante = visitanteRepository.save(visitante);
        }

        // GENERAR PASE (CÓDIGO)
        CodigoTemporalModel codigo = new CodigoTemporalModel();
        codigo.setAsunto(dto.getAsunto());
        codigo.setNumeroAcompañantes(dto.getNumeroAcompañantes());
        codigo.setFechaExpiracion(now.plusHours(24));
        codigo.setActivo(1);

        String safeUuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        codigo.setUuid(safeUuid);
        codigo.setVisitante(visitante);

        codigoTemporalRepository.save(codigo);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("id_visitante", visitante.getId_visitante());
        respuesta.put("nombreCompleto", visitante.getPrimerNombre() + " " + visitante.getApellidoPaterno());
        respuesta.put("uuid", safeUuid);

        return respuesta;
    }

    // ==========================================
    // 2. HISTORIAL DE CONSULTAS (Vital para la tabla)
    // ==========================================
    @Transactional(readOnly = true) // VITAL: Mantiene la conexión abierta para cargar las entradas (Lazy)
    public List<VisitanteResumenDTO> obtenerHistorialResumen() {
        try {
            // Usamos la consulta optimizada que SOLO trae Visitante + Códigos
            // (Asegúrate de que VisitanteRepository tenga el @Query correcto sin el segundo FETCH)
            List<VisitanteModel> visitantes = visitanteRepository.obtenerTodoElHistorial();

            List<VisitanteResumenDTO> resumen = new ArrayList<>();

            for (VisitanteModel v : visitantes) {
                int totalEntradas = 0;
                String ultimaPuerta = "Sin registro";
                LocalDateTime ultimaFecha = null;

                if (v.getCodigos() != null) {
                    for (CodigoTemporalModel c : v.getCodigos()) {
                        // AQUÍ ES DONDE SE CARGAN LAS ENTRADAS (LAZY LOADING)
                        // Gracias al @Transactional, esto no falla.
                        if (c.getEntradas() != null) {
                            totalEntradas += c.getEntradas().size();

                            for (RegistroEntradaVisitanteModel r : c.getEntradas()) {
                                if (ultimaFecha == null || r.getFechaHora().isAfter(ultimaFecha)) {
                                    ultimaFecha = r.getFechaHora();
                                    ultimaPuerta = "Puerta " + r.getEntrada();
                                }
                            }
                        }
                    }
                }

                resumen.add(new VisitanteResumenDTO(
                        v.getId_visitante(),
                        v.getPrimerNombre() + " " + v.getApellidoPaterno(),
                        (v.getCodigos() != null && !v.getCodigos().isEmpty()) ? v.getCodigos().get(v.getCodigos().size()-1).getAsunto() : "General",
                        v.getCreatedAt(),
                        ultimaFecha,
                        totalEntradas,
                        ultimaPuerta
                ));
            }

            // Ordenamos por fecha de creación descendente
            resumen.sort((a, b) -> {
                LocalDateTime f1 = a.getFechaCreacion() != null ? a.getFechaCreacion() : LocalDateTime.MIN;
                LocalDateTime f2 = b.getFechaCreacion() != null ? b.getFechaCreacion() : LocalDateTime.MIN;
                return f2.compareTo(f1);
            });

            return resumen;
        } catch (Exception e) {
            System.err.println("Error generando historial: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ==========================================
    // 3. VALIDACIÓN (ESCÁNER)
    // ==========================================
    public ValidacionResponseDTO validarPasePorUuid(String uuid, int numeroPuerta) {
        Optional<CodigoTemporalModel> codigoOpt = codigoTemporalRepository.findByUuid(uuid);

        if (codigoOpt.isEmpty()) {
            return new ValidacionResponseDTO(false, "Código no encontrado", null, null);
        }

        CodigoTemporalModel codigo = codigoOpt.get();
        VisitanteModel visitante = codigo.getVisitante();

        if (visitante.getDeleted() != null && visitante.getDeleted() == 1) {
            return new ValidacionResponseDTO(false, "Visitante dado de baja", null, null);
        }

        // Registrar entrada
        RegistroEntradaVisitanteModel entrada = new RegistroEntradaVisitanteModel();
        entrada.setFechaHora(LocalDateTime.now());
        entrada.setEntrada(numeroPuerta);
        entrada.setCodigoTemporal(codigo);
        registroEntradaRepository.save(entrada);

        return new ValidacionResponseDTO(true, "Acceso Permitido",
                visitante.getPrimerNombre() + " " + visitante.getApellidoPaterno(),
                codigo.getAsunto());
    }

    // ==========================================
    // 4. GESTIÓN (CRUD COMPLETO)
    // ==========================================

    // Obtener lista simple de activos
    public List<VisitanteModel> obtenerVisitantes() {
        return visitanteRepository.findByDeleted(0);
    }

    // Buscar por ID (Auxiliar)
    public Optional<VisitanteModel> obtenerVisitantePorId(Integer id) {
        return visitanteRepository.findById(id);
    }

    // Eliminar (Soft Delete)
    public boolean eliminarVisitanteSeguro(Integer id) {
        Optional<VisitanteModel> vOpt = visitanteRepository.findById(id);
        if(vOpt.isPresent()){
            VisitanteModel v = vOpt.get();
            v.setDeleted(1); // Marcado como borrado
            v.setUpdateAt(LocalDateTime.now());
            visitanteRepository.save(v);
            return true;
        }
        return false;
    }

    // Restaurar (Undo Delete)
    public boolean restaurarVisitanteSeguro(Integer id) {
        Optional<VisitanteModel> vOpt = visitanteRepository.findById(id);
        if(vOpt.isPresent()){
            VisitanteModel v = vOpt.get();
            v.setDeleted(0); // Marcado como activo
            v.setUpdateAt(LocalDateTime.now());
            visitanteRepository.save(v);
            return true;
        }
        return false;
    }
}