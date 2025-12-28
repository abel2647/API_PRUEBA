package com.example.fingerprint_api.services;

import com.digitalpersona.uareu.*;
import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class MultiReaderFingerprintService {

    private static final Logger logger = LoggerFactory.getLogger(MultiReaderFingerprintService.class);

    // Mapas de control
    private final Map<String, Reader> validReaders = new ConcurrentHashMap<>();
    private final Map<String, String> readerToInstanceMap = new ConcurrentHashMap<>();
    private final Map<String, String> instanceToReaderMap = new ConcurrentHashMap<>();

    // Hilos
    private final ExecutorService captureExecutor = Executors.newFixedThreadPool(10);
    private final Map<String, Future<?>> captureTasks = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // WebSocket

    @Autowired
    private AlumnoService alumnoService;       // Lógica de Alumnos

    @Autowired
    private AsistenciaService asistenciaService; // Lógica de Asistencia

    @PostConstruct
    public void init() {
        try { refreshConnectedReaders(); } catch (Exception e) {}
    }

    // --- HARDWARE ---
    public synchronized List<String> refreshConnectedReaders() throws UareUException {
        ReaderCollection readers = UareUGlobal.GetReaderCollection();
        readers.GetReaders();
        validReaders.clear();
        for (Reader r : readers) {
            try {
                r.Open(Reader.Priority.EXCLUSIVE);
                validReaders.put(r.GetDescription().name, r);
            } catch (Exception e) { /* Ignorar fallos de apertura */ }
        }
        return new ArrayList<>(validReaders.keySet());
    }

    public Set<String> getAvailableReaderNames() {
        Set<String> all = new HashSet<>(validReaders.keySet());
        all.removeAll(readerToInstanceMap.keySet());
        return all;
    }

    // --- RESERVAS ---
    public synchronized boolean reserveReaderByInstance(String readerId, String instanceId) {
        if (!validReaders.containsKey(readerId)) return false;
        releaseReaderByInstance(instanceId); // Liberar anterior si existe

        if (readerToInstanceMap.containsKey(readerId)) return false; // Ya ocupado

        instanceToReaderMap.put(instanceId, readerId);
        readerToInstanceMap.put(readerId, instanceId);
        return true;
    }

    public synchronized void releaseReaderByInstance(String instanceId) {
        String readerId = instanceToReaderMap.remove(instanceId);
        if (readerId != null) {
            stopCapture(readerId);
            readerToInstanceMap.remove(readerId);
        }
    }

    private void stopCapture(String readerId) {
        Future<?> task = captureTasks.remove(readerId);
        if (task != null) task.cancel(true);
        Reader r = validReaders.get(readerId);
        if (r != null) { try { r.CancelCapture(); } catch(Exception e){} }
    }

    // --- REGISTRO / EDICIÓN (ENROLAMIENTO) ---
    public String registrarHuellaParaAlumno(String readerId, Integer idAlumno) {
        Reader reader = validReaders.get(readerId);
        if (reader == null) return "Lector no conectado";

        stopCapture(readerId); // Detener modo búsqueda si estaba activo

        try {
            // Captura sincrónica (espera 15 seg)
            Reader.CaptureResult cr = reader.Capture(Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT, 500, 15000);

            if (cr != null && cr.quality == Reader.CaptureQuality.GOOD) {
                Engine engine = UareUGlobal.GetEngine();
                Fmd fmd = engine.CreateFmd(cr.image, Fmd.Format.ANSI_378_2004);

                // Guardar en AlumnoService
                boolean exito = alumnoService.guardarHuellaAlumno(idAlumno, fmd.getData());
                return exito ? "Exito" : "Alumno no encontrado";
            }
            return "No se detectó huella o mala calidad";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // --- BÚSQUEDA (CHECADOR) ---
    public boolean startContinuousCapture(String readerId, String instanceId) {
        if (!instanceId.equals(readerToInstanceMap.get(readerId))) return false;

        stopCapture(readerId); // Reiniciar tarea si ya existía

        Runnable task = () -> {
            Reader reader = validReaders.get(readerId);
            String topic = "/topic/fingerprint-events/" + instanceId;

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Reader.CaptureResult cr = reader.Capture(Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT, 500, -1);
                    if (cr != null && cr.quality == Reader.CaptureQuality.GOOD) {

                        Engine engine = UareUGlobal.GetEngine();
                        Fmd fmd = engine.CreateFmd(cr.image, Fmd.Format.ANSI_378_2004);

                        // 1. Identificar
                        Optional<AlumnoModel> alumnoOpt = alumnoService.identificarAlumno(fmd);

                        if (alumnoOpt.isPresent()) {
                            // 2. Registrar Asistencia
                            boolean registrado = asistenciaService.procesarEntrada(alumnoOpt.get(), readerId);

                            // 3. Notificar Frontend
                            Map<String, Object> resp = new HashMap<>();
                            resp.put("status", "FOUND");
                            resp.put("alumno", alumnoOpt.get());
                            resp.put("nuevo_registro", registrado);
                            messagingTemplate.convertAndSend(topic, resp);
                        } else {
                            messagingTemplate.convertAndSend(topic, Map.of("status", "NOT_FOUND"));
                        }
                    }
                } catch (Exception e) {
                    if (Thread.currentThread().isInterrupted()) break;
                }
            }
        };

        captureTasks.put(readerId, captureExecutor.submit(task));
        return true;
    }
}