package com.example.fingerprint_api.services;

import com.example.fingerprint_api.dtos.ValidacionResponseDTO;
import com.example.fingerprint_api.dtos.VisitanteRegistroDTO;
import com.example.fingerprint_api.dtos.VisitanteResumenDTO;
import com.example.fingerprint_api.models.Visitante.CodigoTemporalModel;
import com.example.fingerprint_api.models.Visitante.RegistroEntradaVisitanteModel;
import com.example.fingerprint_api.models.Visitante.RegistroSalidaVisitanteModel;
import com.example.fingerprint_api.models.Visitante.VisitanteModel;
import com.example.fingerprint_api.repositories.CodigoTemporalRepository;
import com.example.fingerprint_api.repositories.RegistroEntradaVisitanteRepository;
import com.example.fingerprint_api.repositories.RegistroSalidaVisitanteRepository;
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
    @Autowired
    RegistroSalidaVisitanteRepository registroSalidaRepository;

    // --- CONFIGURACIÓN DE TIEMPOS ---
    // Tiempo que debe esperar afuera para volver a entrar
    private static final int MINUTOS_ESPERA_REINGRESO = 15;
    // Tiempo mínimo que debe estar adentro para poder salir (evita doble escaneo accidental)
    private static final int MINUTOS_ESPERA_SALIDA = 15;

    // --------------------------------------------------------------------------------
    // 1. REGISTRO DE VISITANTE (Genera Pase de 24 Horas)
    // --------------------------------------------------------------------------------
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

        // PASE VÁLIDO POR 24 HORAS (Permite múltiples entradas/salidas)
        codigo.setFechaExpiracion(now.plusDays(1));

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

    // --------------------------------------------------------------------------------
    // 2. HISTORIAL (Resumen con Entradas y Salidas)
    // --------------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<VisitanteResumenDTO> obtenerHistorialResumen() {
        try {
            List<VisitanteModel> visitantes = visitanteRepository.obtenerTodoElHistorial();
            List<VisitanteResumenDTO> resumen = new ArrayList<>();

            for (VisitanteModel v : visitantes) {
                int totalEntradas = 0;
                int totalSalidas = 0;
                String ultimaPuerta = "Sin registro";
                LocalDateTime ultimaFecha = null;
                String ultimoAsunto = "General";
                LocalDateTime ultimaExpiracion = null;

                if (v.getCodigos() != null && !v.getCodigos().isEmpty()) {
                    CodigoTemporalModel ultimoCodigo = v.getCodigos().get(v.getCodigos().size() - 1);
                    ultimoAsunto = ultimoCodigo.getAsunto();
                    ultimaExpiracion = ultimoCodigo.getFechaExpiracion();

                    for (CodigoTemporalModel c : v.getCodigos()) {
                        // Contar Entradas
                        if (c.getEntradas() != null) {
                            totalEntradas += c.getEntradas().size();
                            for (RegistroEntradaVisitanteModel r : c.getEntradas()) {
                                if (ultimaFecha == null || r.getFechaHora().isAfter(ultimaFecha)) {
                                    ultimaFecha = r.getFechaHora();
                                    ultimaPuerta = "Puerta " + r.getEntrada();
                                }
                            }
                        }
                        // Contar Salidas
                        if (c.getSalidas() != null) {
                            totalSalidas += c.getSalidas().size();
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
                        totalSalidas, // Nuevo campo en DTO
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

    // --------------------------------------------------------------------------------
    // 3. VALIDACIÓN INTELIGENTE (MODO TORNIQUETE) - Escáner Principal
    // --------------------------------------------------------------------------------
    public ValidacionResponseDTO validarPasePorUuid(String uuid, int numeroPuerta) {
        Optional<CodigoTemporalModel> codigoOpt = codigoTemporalRepository.findByUuid(uuid);

        if (codigoOpt.isEmpty()) {
            return new ValidacionResponseDTO(false, "CÓDIGO NO ENCONTRADO", null, null, 0, 0, "N/A");
        }

        CodigoTemporalModel codigo = codigoOpt.get();
        VisitanteModel visitante = codigo.getVisitante();

        // A. CÁLCULO DE TOTALES PREVIOS (Para mostrar data aunque falle)
        int totalEntradas = registroEntradaRepository.contarTotalEntradas(visitante.getId_visitante());
        int totalSalidas = (codigo.getSalidas() != null) ? codigo.getSalidas().size() : 0;

        // B. VALIDACIONES DE ESTADO
        if (visitante.getDeleted() != null && visitante.getDeleted() == 1) {
            return new ValidacionResponseDTO(false, "VISITANTE BAJA", visitante.getPrimerNombre(), codigo.getAsunto(), totalEntradas, totalSalidas, "N/A");
        }

        if (codigo.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return new ValidacionResponseDTO(false, "PASE VENCIDO / FINALIZADO", visitante.getPrimerNombre(), codigo.getAsunto(), totalEntradas, totalSalidas, "N/A");
        }

        // C. DETERMINAR SI ESTÁ ADENTRO O AFUERA
        LocalDateTime ultimaEntrada = LocalDateTime.MIN;
        LocalDateTime ultimaSalida = LocalDateTime.MIN;

        if (codigo.getEntradas() != null && !codigo.getEntradas().isEmpty()) {
            ultimaEntrada = codigo.getEntradas().get(codigo.getEntradas().size() - 1).getFechaHora();
        }
        if (codigo.getSalidas() != null && !codigo.getSalidas().isEmpty()) {
            ultimaSalida = codigo.getSalidas().get(codigo.getSalidas().size() - 1).getFechaHora();
        }

        boolean estaAdentro = ultimaEntrada.isAfter(ultimaSalida);
        LocalDateTime ahora = LocalDateTime.now();

        // D. LÓGICA DE MOVIMIENTO
        if (estaAdentro) {
            // --- EL USUARIO ESTÁ ADENTRO -> INTENTA SALIR ---

            // Anti-Rebote (¿Acaba de entrar hace menos de 1 min?)
            long minutosDentro = Duration.between(ultimaEntrada, ahora).toMinutes();
            if (minutosDentro < MINUTOS_ESPERA_SALIDA) {
                return new ValidacionResponseDTO(
                        false,
                        "ESPERE " + (MINUTOS_ESPERA_SALIDA - minutosDentro) + " MIN",
                        visitante.getPrimerNombre(),
                        "Salida muy rápida",
                        totalEntradas, totalSalidas, "N/A"
                );
            }

            // Registrar Salida
            RegistroSalidaVisitanteModel salida = new RegistroSalidaVisitanteModel();
            salida.setFechaHora(ahora);
            salida.setPuerta(numeroPuerta);
            salida.setCodigoTemporal(codigo);
            registroSalidaRepository.save(salida);

            return new ValidacionResponseDTO(
                    true,
                    "SALIDA REGISTRADA",
                    visitante.getPrimerNombre() + " " + visitante.getApellidoPaterno(),
                    "Salida temporal",
                    totalEntradas,
                    totalSalidas + 1, // Incrementamos visualmente
                    "Puerta " + numeroPuerta
            );

        } else {
            // --- EL USUARIO ESTÁ AFUERA -> INTENTA ENTRAR ---

            // Anti-Spam (¿Acaba de entrar hace menos de 15 min?)
            if (!ultimaEntrada.equals(LocalDateTime.MIN)) {
                long minutosDesdeUltimaEntrada = Duration.between(ultimaEntrada, ahora).toMinutes();
                if (minutosDesdeUltimaEntrada < MINUTOS_ESPERA_REINGRESO) {
                    return new ValidacionResponseDTO(
                            false,
                            "ESPERE " + (MINUTOS_ESPERA_REINGRESO - minutosDesdeUltimaEntrada) + " MIN",
                            visitante.getPrimerNombre(),
                            "Reingreso Rápido",
                            totalEntradas, totalSalidas, "N/A"
                    );
                }
            }

            // Registrar Entrada
            RegistroEntradaVisitanteModel entrada = new RegistroEntradaVisitanteModel();
            entrada.setFechaHora(ahora);
            entrada.setPuerta(numeroPuerta);
            entrada.setCodigoTemporal(codigo);
            registroEntradaRepository.save(entrada);

            return new ValidacionResponseDTO(
                    true,
                    "ACCESO PERMITIDO",
                    visitante.getPrimerNombre() + " " + visitante.getApellidoPaterno(),
                    codigo.getAsunto(),
                    totalEntradas + 1, // Incrementamos visualmente
                    totalSalidas,
                    "Puerta " + numeroPuerta
            );
        }
    }

    // --------------------------------------------------------------------------------
    // 4. TERMINAR VISITA MANUALMENTE (Escáner Admin / Botón Tabla)
    // --------------------------------------------------------------------------------
    // Este método SÍ mata el pase inmediatamente.
    @Transactional
    public ValidacionResponseDTO registrarSalidaPorUuid(String uuid) {
        Optional<CodigoTemporalModel> codigoOpt = codigoTemporalRepository.findByUuid(uuid);

        if (codigoOpt.isEmpty()) {
            return new ValidacionResponseDTO(false, "CÓDIGO NO ENCONTRADO", null, null, 0, 0, "N/A");
        }

        CodigoTemporalModel codigo = codigoOpt.get();
        VisitanteModel visitante = codigo.getVisitante();

        // Calcular totales para la respuesta
        int totalEntradas = registroEntradaRepository.contarTotalEntradas(visitante.getId_visitante());
        int totalSalidas = (codigo.getSalidas() != null) ? codigo.getSalidas().size() : 0;

        if (codigo.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return new ValidacionResponseDTO(
                    false,
                    "EL PASE YA ESTABA CERRADO",
                    visitante.getPrimerNombre(),
                    codigo.getAsunto(),
                    totalEntradas, totalSalidas, "N/A"
            );
        }

        // 1. Registramos Salida Administrativa (Puerta 0)
        RegistroSalidaVisitanteModel salida = new RegistroSalidaVisitanteModel();
        salida.setFechaHora(LocalDateTime.now());
        salida.setPuerta(0); // 0 indica salida forzada/admin
        salida.setCodigoTemporal(codigo);
        registroSalidaRepository.save(salida);

        // 2. MATAMOS EL PASE (Expiración inmediata)
        codigo.setFechaExpiracion(LocalDateTime.now().minusDays(1));
        codigoTemporalRepository.save(codigo);

        return new ValidacionResponseDTO(
                true,
                "VISITA FINALIZADA",
                visitante.getPrimerNombre() + " " + visitante.getApellidoPaterno(),
                "Pase Desactivado",
                totalEntradas,
                totalSalidas + 1,
                "Admin"
        );
    }

    // --------------------------------------------------------------------------------
    // 5. MÉTODOS AUXILIARES
    // --------------------------------------------------------------------------------

    // Respaldo para cerrar por ID (sin escáner)
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