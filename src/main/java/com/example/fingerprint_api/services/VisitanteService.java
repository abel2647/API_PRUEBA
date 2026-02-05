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

    @Transactional
    public Map<String, Object> registrarVisitanteCompleto(VisitanteRegistroDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        VisitanteModel visitante;

        Optional<VisitanteModel> existente = visitanteRepository.findByNumTelefono(dto.getNumTelefono());

        if (existente.isPresent()) {
            visitante = existente.get();
            visitante.setPrimerNombre(dto.getPrimerNombre());
            visitante.setApellidoPaterno(dto.getApellidoPaterno());
            visitante.setApellidoMaterno(dto.getApellidoMaterno());
            visitante.setUpdateAt(now);
            if(visitante.getDeleted() != null && visitante.getDeleted() == 1) visitante.setDeleted(0);
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
            visitante.setCreatedAt(now);
            visitante.setUpdateAt(now);
            visitante.setDeleted(0);
            visitante = visitanteRepository.save(visitante);
        }

        CodigoTemporalModel codigo = new CodigoTemporalModel();
        codigo.setAsunto(dto.getAsunto());
        codigo.setNumeroAcompañantes(dto.getNumeroAcompañantes());
        codigo.setFechaExpiracion(now.plusHours(2));
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

    @Transactional(readOnly = true)
    public List<VisitanteResumenDTO> obtenerHistorialResumen() {
        try {
            List<VisitanteModel> visitantes = visitanteRepository.obtenerTodoElHistorial();
            List<VisitanteResumenDTO> resumen = new ArrayList<>();

            for (VisitanteModel v : visitantes) {
                int totalEntradas = 0;
                String ultimaPuerta = "Sin registro";
                LocalDateTime ultimaFecha = null;
                String ultimoAsunto = "General";
                LocalDateTime ultimaExpiracion = null;

                if (v.getCodigos() != null && !v.getCodigos().isEmpty()) {
                    CodigoTemporalModel ultimoCodigo = v.getCodigos().get(v.getCodigos().size() - 1);
                    ultimoAsunto = ultimoCodigo.getAsunto();
                    ultimaExpiracion = ultimoCodigo.getFechaExpiracion();

                    for (CodigoTemporalModel c : v.getCodigos()) {
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

                // NUEVO CONSTRUCTOR CON NOMBRES SEPARADOS
                resumen.add(new VisitanteResumenDTO(
                        v.getId_visitante(),
                        v.getPrimerNombre(),     // 1. Nombre
                        v.getApellidoPaterno(),  // 2. Paterno
                        v.getApellidoMaterno(),  // 3. Materno
                        ultimoAsunto,
                        v.getCreatedAt(),
                        ultimaFecha,
                        totalEntradas,
                        ultimaPuerta,
                        ultimaExpiracion
                ));
            }

            resumen.sort((a, b) -> {
                LocalDateTime f1 = a.getFechaCreacion() != null ? a.getFechaCreacion() : LocalDateTime.MIN;
                LocalDateTime f2 = b.getFechaCreacion() != null ? b.getFechaCreacion() : LocalDateTime.MIN;
                return f2.compareTo(f1);
            });

            return resumen;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    // 3. VALIDACIÓN (AQUÍ ESTÁ EL CAMBIO IMPORTANTE)
    // ... Imports y resto de la clase ...

    // SUSTITUYE EL MÉTODO validarPasePorUuid POR ESTE COMPLETO:
    public ValidacionResponseDTO validarPasePorUuid(String uuid, int numeroPuerta) {
        Optional<CodigoTemporalModel> codigoOpt = codigoTemporalRepository.findByUuid(uuid);

        if (codigoOpt.isEmpty()) {
            return new ValidacionResponseDTO(false, "Código no encontrado", null, null, 0, "N/A");
        }

        CodigoTemporalModel codigo = codigoOpt.get();
        VisitanteModel visitante = codigo.getVisitante();

        // 1. Verificar si el visitante está borrado
        if (visitante.getDeleted() != null && visitante.getDeleted() == 1) {
            return new ValidacionResponseDTO(false, "Visitante dado de baja", null, null, 0, "N/A");
        }

        // 2. VERIFICAR SI YA EXPIRÓ (Esto soluciona lo del pase de hace 3 días)
        if (codigo.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return new ValidacionResponseDTO(
                    false,
                    "PASE VENCIDO",
                    visitante.getPrimerNombre() + " " + visitante.getApellidoPaterno(),
                    codigo.getAsunto(),
                    0,
                    "N/A"
            );
        }

        // 3. Registrar entrada si todo está bien
        RegistroEntradaVisitanteModel entrada = new RegistroEntradaVisitanteModel();
        entrada.setFechaHora(LocalDateTime.now());
        entrada.setEntrada(numeroPuerta);
        entrada.setCodigoTemporal(codigo);
        registroEntradaRepository.save(entrada);

        int totalAccesos = registroEntradaRepository.contarTotalEntradas(visitante.getId_visitante());

        return new ValidacionResponseDTO(
                true,
                "Acceso Permitido",
                visitante.getPrimerNombre() + " " + visitante.getApellidoPaterno(),
                codigo.getAsunto(),
                totalAccesos,
                "Puerta " + numeroPuerta
        );
    }

    // 4. GESTIÓN
    public List<VisitanteModel> obtenerVisitantes() { return visitanteRepository.findByDeleted(0); }

    public boolean eliminarVisitanteSeguro(Integer id) {
        Optional<VisitanteModel> vOpt = visitanteRepository.findById(id);
        if(vOpt.isPresent()){
            VisitanteModel v = vOpt.get();
            v.setDeleted(1);
            v.setUpdateAt(LocalDateTime.now());
            visitanteRepository.save(v);
            return true;
        }
        return false;
    }

    public boolean restaurarVisitanteSeguro(Integer id) {
        Optional<VisitanteModel> vOpt = visitanteRepository.findById(id);
        if(vOpt.isPresent()){
            VisitanteModel v = vOpt.get();
            v.setDeleted(0);
            v.setUpdateAt(LocalDateTime.now());
            visitanteRepository.save(v);
            return true;
        }
        return false;
    }
}