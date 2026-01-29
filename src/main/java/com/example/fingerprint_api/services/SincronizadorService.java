package com.example.fingerprint_api.services;

import com.example.fingerprint_api.dtos.PaqueteSincronizacion;
import com.example.fingerprint_api.models.Alumno.AlumnoModel; // Asegúrate que este import sea correcto
import com.example.fingerprint_api.models.Visitante.VisitanteModel;
import com.example.fingerprint_api.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Component
@EnableScheduling
public class SincronizadorService {

    @Autowired private AlumnoRepository alumnoRepo;
    @Autowired private VisitanteRepository visitanteRepo;
    @Autowired private CodigoTemporalRepository codigoRepo;
    @Autowired private RegistroAsistenciaRepository regAlumnoRepo;
    @Autowired private RegistroEntradaVisitanteRepository regVisitanteRepo;

    // URL de tu PC Servidor
    private final String SERVER_URL = "http://100.73.91.22:8081/api/replicacion/full-sync";

    // Se ejecuta al inicio (5s) y luego cada 1 minuto (60000ms)
    @Scheduled(initialDelay = 5000, fixedRate = 60000)
    public void ejecutarSincronizacion() {
        try {
            System.out.println("--- Iniciando Sincronización Inteligente ---");

            // 1. Empaquetar mis datos locales (aunque estén vacíos) para enviarlos
            PaqueteSincronizacion misDatos = new PaqueteSincronizacion();
            misDatos.setAlumnos(alumnoRepo.findAll());
            misDatos.setVisitantes((List<VisitanteModel>) visitanteRepo.findAll());
            misDatos.setCodigos(codigoRepo.findAll());
            misDatos.setRegistrosAlumnos(regAlumnoRepo.findAll());
            misDatos.setRegistrosVisitantes(regVisitanteRepo.findAll());

            // 2. Enviar mis datos y RECIBIR los del servidor (POST)
            RestTemplate restTemplate = new RestTemplate();
            PaqueteSincronizacion datosNube = restTemplate.postForObject(
                    SERVER_URL,
                    misDatos,
                    PaqueteSincronizacion.class
            );

            // 3. Procesar lo que llegó del servidor (LA PARTE CLAVE)
            if (datosNube != null) {
                System.out.println("--- Recibidos datos del servidor. Procesando... ---");

                // Usamos los métodos "Inteligentes" para evitar el crash de IDs
                sincronizarAlumnosSafely(datosNube.getAlumnos());
                sincronizarVisitantesSafely(datosNube.getVisitantes());

                // Para los registros históricos (Hijos), saveAll suele ser seguro si son nuevos.
                // Si te diera error aquí también, tendríamos que hacer métodos safely para estos.
                if(datosNube.getCodigos() != null)
                    codigoRepo.saveAll(datosNube.getCodigos());

                if(datosNube.getRegistrosAlumnos() != null)
                    regAlumnoRepo.saveAll(datosNube.getRegistrosAlumnos());

                if(datosNube.getRegistrosVisitantes() != null)
                    regVisitanteRepo.saveAll(datosNube.getRegistrosVisitantes());

                System.out.println("--- Sincronización Finalizada con Éxito ---");
            }

        } catch (Exception e) {
            System.out.println("Fallo al conectar con servidor (Modo Offline activo): " + e.getMessage());
            // No pasa nada, seguimos trabajando local y reintentamos en 1 min.
        }
    }

    // =========================================================================
    // MÉTODOS DE GUARDADO SEGURO (Evitan el error Row Updated/Deleted)
    // =========================================================================

    private void sincronizarAlumnosSafely(List<AlumnoModel> externos) {
        if (externos == null) return;

        for (AlumnoModel externo : externos) {
            // Paso 1: Buscar si ya existe en MI base local
            Optional<AlumnoModel> local = alumnoRepo.findById(externo.getId());

            if (local.isPresent()) {
                // CASO A: Ya existe. Lo actualizamos (UPDATE)
                AlumnoModel existente = local.get();
                existente.setPrimerNombre(externo.getPrimerNombre());
                existente.setSegundoNombre(externo.getSegundoNombre());
                existente.setApellidoPaterno(externo.getApellidoPaterno());
                existente.setApellidoMaterno(externo.getApellidoMaterno());
                existente.setCarreraClave(externo.getCarreraClave());
                existente.setActivo(externo.getActivo());
                existente.setCodigoQr(externo.getCodigoQr());
                existente.setCodigoBarra(externo.getCodigoBarra());
                existente.setNumTelefono(externo.getNumTelefono());
                existente.setNumeroControl(externo.getNumeroControl());
                existente.setHuellaFmd(externo.getHuellaFmd());
                existente.setUuid(externo.getUuid());
                existente.setCreatedAt(externo.getCreatedAt());
                existente.setDeleted(externo.getDeleted());
                existente.setUsuario(externo.getUsuario());
                existente.setUpdateAt(externo.getUpdateAt());

                alumnoRepo.save(existente);
            } else {
                // CASO B: No existe (Base vacía). Lo creamos (INSERT)
                // Al verificar con findById primero, save() ya no se confunde.
                alumnoRepo.save(externo);
            }
        }
    }

    private void sincronizarVisitantesSafely(List<VisitanteModel> externos) {
        if (externos == null) return;

        for (VisitanteModel externo : externos) {
            Optional<VisitanteModel> local = visitanteRepo.findById(externo.getId_visitante());

            if (local.isPresent()) {
                VisitanteModel existente = local.get();
                existente.setApellidoMaterno(externo.getApellidoMaterno());
                existente.setApellidoPaterno(externo.getApellidoPaterno());
                existente.setCreatedAt(externo.getCreatedAt());
                existente.setDeleted(externo.getDeleted());
                existente.setUsuario(externo.getUsuario());
                existente.setUpdateAt(externo.getUpdateAt());
                existente.setEdad(externo.getEdad());
                existente.setNumTelefono(existente.getNumTelefono());
                existente.setPrimerNombre(existente.getPrimerNombre());
                existente.setSexo(existente.getSexo());

                visitanteRepo.save(existente);
            } else {
                visitanteRepo.save(externo);
            }
        }
    }
}