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

import java.time.Duration;
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

    // CONFIGURACIÓN: Tiempo de espera para volver a entrar (Anti-Spam)
    private static final int MINUTOS_ESPERA_REINGRESO = 15;

    // --- 1. REGISTRO (DURACIÓN 24 HORAS) ---
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

        // CAMBIO: 24 horas de validez por defecto (hasta que marquen salida)
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

    // --- 2. HISTORIAL ---
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

                resumen.add(new VisitanteResumenDTO(
                        v.getId_visitante(),
                        v.getPrimerNombre(),
                        v.getApellidoPaterno(),
                        v.getApellidoMaterno(),
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

    // --- 3. VALIDACIÓN DE ENTRADA (CON ANTI-SPAM) ---
    public ValidacionResponseDTO validarPasePorUuid(String uuid, int numeroPuerta) {
        Optional<CodigoTemporalModel> codigoOpt = codigoTemporalRepository.findByUuid(uuid);

        if (codigoOpt.isEmpty()) {
            return new ValidacionResponseDTO(false, "Código no encontrado", null, null, 0, "N/A");
        }

        CodigoTemporalModel codigo = codigoOpt.get();
        VisitanteModel visitante = codigo.getVisitante();

        if (visitante.getDeleted() != null && visitante.getDeleted() == 1) {
            return new ValidacionResponseDTO(false, "Visitante dado de baja", null, null, 0, "N/A");
        }

        if (codigo.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return new ValidacionResponseDTO(
                    false, "PASE VENCIDO / FINALIZADO", visitante.getPrimerNombre() + " " + visitante.getApellidoPaterno(), codigo.getAsunto(), 0, "N/A"
            );
        }

        // --- VALIDACIÓN ANTI-SPAM (15 MINUTOS) ---
        if (codigo.getEntradas() != null && !codigo.getEntradas().isEmpty()) {
            RegistroEntradaVisitanteModel ultimaEntrada = codigo.getEntradas().get(codigo.getEntradas().size() - 1);
            LocalDateTime horaUltimaEntrada = ultimaEntrada.getFechaHora();
            LocalDateTime ahora = LocalDateTime.now();

            long minutosDiferencia = Duration.between(horaUltimaEntrada, ahora).toMinutes();

            if (minutosDiferencia < MINUTOS_ESPERA_REINGRESO) {
                long minutosRestantes = MINUTOS_ESPERA_REINGRESO - minutosDiferencia;
                return new ValidacionResponseDTO(
                        false, "ESPERE " + minutosRestantes + " MINUTOS", visitante.getPrimerNombre(), "Reingreso muy rápido", 0, "N/A"
                );
            }
        }

        RegistroEntradaVisitanteModel entrada = new RegistroEntradaVisitanteModel();
        entrada.setFechaHora(LocalDateTime.now());
        entrada.setEntrada(numeroPuerta);
        entrada.setCodigoTemporal(codigo);
        registroEntradaRepository.save(entrada);

        int totalAccesos = registroEntradaRepository.contarTotalEntradas(visitante.getId_visitante());

        return new ValidacionResponseDTO(true, "Acceso Permitido", visitante.getPrimerNombre() + " " + visitante.getApellidoPaterno(), codigo.getAsunto(), totalAccesos, "Puerta " + numeroPuerta);
    }

    // --- 4. REGISTRAR SALIDA (NUEVO MÉTODO PARA EL ESCÁNER DE SALIDA) ---
    @Transactional
    public ValidacionResponseDTO registrarSalidaPorUuid(String uuid) {
        Optional<CodigoTemporalModel> codigoOpt = codigoTemporalRepository.findByUuid(uuid);

        if (codigoOpt.isEmpty()) {
            return new ValidacionResponseDTO(false, "CÓDIGO NO ENCONTRADO", null, null, 0, "N/A");
        }

        CodigoTemporalModel codigo = codigoOpt.get();
        VisitanteModel visitante = codigo.getVisitante();

        // Validar si ya estaba cerrado
        if (codigo.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return new ValidacionResponseDTO(false, "EL PASE YA ESTABA CERRADO", visitante.getPrimerNombre(), codigo.getAsunto(), 0, "N/A");
        }

        // FINALIZAR VISITA: CAMBIAR FECHA EXPIRACIÓN AL PASADO
        codigo.setFechaExpiracion(LocalDateTime.now().minusDays(1));
        codigoTemporalRepository.save(codigo);

        return new ValidacionResponseDTO(
                true,
                "SALIDA REGISTRADA",
                visitante.getPrimerNombre() + " " + visitante.getApellidoPaterno(),
                "Visita Finalizada Correctamente",
                0,
                "Salida"
        );
    }

    // --- 5. FORZAR EXPIRACIÓN POR ID (RESPALDO MANUAL) ---
    @Transactional
    public boolean forzarExpiracion(Integer idVisitante) {
        Optional<VisitanteModel> visOpt = visitanteRepository.findById(idVisitante);
        if(visOpt.isEmpty()) return false;

        VisitanteModel v = visOpt.get();
        if(v.getCodigos() != null && !v.getCodigos().isEmpty()) {
            CodigoTemporalModel ultimoCodigo = v.getCodigos().get(v.getCodigos().size() - 1);
            ultimoCodigo.setFechaExpiracion(LocalDateTime.now().minusDays(1));
            codigoTemporalRepository.save(ultimoCodigo);
            return true;
        }
        return false;
    }

    // MÉTODOS AUXILIARES
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