package com.example.fingerprint_api.services;

import com.digitalpersona.uareu.*;
import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import com.example.fingerprint_api.repositories.AlumnoRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
public class MultiReaderFingerprintService {

    private static final Logger logger = LoggerFactory.getLogger(MultiReaderFingerprintService.class);
    private final Map<String, Reader> validReaders = new ConcurrentHashMap<>();

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private AsistenciaService asistenciaService;

    @PostConstruct
    public void init() { refreshConnectedReaders(); }

    @PreDestroy
    public void releaseAll() {
        for (Reader r : validReaders.values()) {
            try { r.CancelCapture(); r.Close(); } catch (Exception e) {}
        }
        validReaders.clear();
    }

    // Cambia 'public synchronized void' por 'public synchronized List<String>'
    public synchronized List<String> refreshConnectedReaders() {
        try {
            ReaderCollection readers = UareUGlobal.GetReaderCollection();
            readers.GetReaders();
            for (Reader r : readers) {
                try {
                    r.Open(Reader.Priority.EXCLUSIVE);
                    validReaders.put(r.GetDescription().name, r);
                } catch (Exception e) {}
            }
        } catch (UareUException e) {
            logger.error("Error SDK: " + e.getMessage());
        }
        // Agrega esta línea al final para devolver las llaves (nombres) del mapa
        return new ArrayList<>(validReaders.keySet());
    }

    public AlumnoModel identificarDedoAutomatico() {
        if (validReaders.isEmpty()) refreshConnectedReaders();
        Reader reader = validReaders.values().stream().findFirst().orElse(null);

        if (reader == null) return null;

        try {
            try { reader.CancelCapture(); } catch (Exception e) {}

            logger.info(">>> Lector encendido: Esperando dedo (5 segundos)...");
            Reader.CaptureResult cr = reader.Capture(Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT, 500, 5000);

            if (cr != null && cr.quality == Reader.CaptureQuality.GOOD) {
                Engine engine = UareUGlobal.GetEngine();
                Fmd fmdCapturado = engine.CreateFmd(cr.image, Fmd.Format.ANSI_378_2004);

                List<AlumnoModel> todos = alumnoRepository.findAll();
                for (AlumnoModel alumno : todos) {
                    if (alumno.getHuellaFmd() != null) {
                        try {
                            Fmd fmdBaseDatos = UareUGlobal.GetImporter().ImportFmd(alumno.getHuellaFmd(), Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
                            int score = engine.Compare(fmdCapturado, 0, fmdBaseDatos, 0);

                            if (score < 21474) { // Match!
                                asistenciaService.registrarEntrada(alumno, 1);
                                return alumno;
                            }
                        } catch (Exception e) { continue; }
                    }
                }
                logger.info("No hubo coincidencia en BD.");
                return null; // Caso: Dedo puesto pero NO reconocido (Ventana Roja)
            } else {
                return new AlumnoModel(); // Caso: Mala calidad (Reintento Silencioso)
            }

        } catch (UareUException e) {
            // Obtenemos el mensaje en una variable
            String mensajeError = e.getMessage();

            // Validamos que no sea nulo Y que contenga el timeout
            if (mensajeError != null && mensajeError.contains("TIMED_OUT")) {
                return new AlumnoModel(); // Reintento silencioso si nadie puso el dedo
            }

            // Si el mensaje es nulo o es otro error, logueamos y retornamos null
            logger.error("Error de hardware: " + (mensajeError != null ? mensajeError : "Mensaje nulo del SDK"));
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public String enrolarAlumnoEnBaseDeDatos(String readerId, Integer idAlumno, Integer step) {
        if (validReaders.isEmpty()) refreshConnectedReaders();
        Reader reader = validReaders.values().stream().findFirst().orElse(null);
        if (reader == null) return "Lector no encontrado";
        try {
            Reader.CaptureResult cr = reader.Capture(Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT, 500, 10000);
            if (cr != null && cr.quality == Reader.CaptureQuality.GOOD) {
                Engine engine = UareUGlobal.GetEngine();
                Fmd fmd = engine.CreateFmd(cr.image, Fmd.Format.ANSI_378_2004);
                return alumnoRepository.findById(idAlumno).map(a -> {
                    a.setHuellaFmd(fmd.getData());
                    a.setUpdateAt(LocalDateTime.now()); // Verifica que el Model tenga la 'd' en Updated
                    alumnoRepository.save(a); // .save() es más seguro que saveAndFlush()
                    return "Exito: Guardado";
                }).orElse("Alumno no encontrado");
            }
            return "Fallo Captura";
        } catch (Exception e) { return "Error: " + e.getMessage(); }
    }
}