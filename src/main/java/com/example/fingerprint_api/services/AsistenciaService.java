
package com.example.fingerprint_api.services;

import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import com.example.fingerprint_api.models.Asistencia.RegistroAsistenciaModel;
import com.example.fingerprint_api.repositories.RegistroAsistenciaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AsistenciaService {

    private static final Logger logger = LoggerFactory.getLogger(AsistenciaService.class);

    @Autowired
    private RegistroAsistenciaRepository asistenciaRepository;

    // Cache para evitar registros duplicados si el alumno deja el dedo puesto
    // Guarda: ID_Alumno -> Última hora de registro
    private final Map<Integer, LocalDateTime> ultimoRegistroCache = new ConcurrentHashMap<>();

    /**
     * Registra la entrada de un alumno si ha pasado suficiente tiempo desde su último registro.
     */
    public boolean procesarEntrada(AlumnoModel alumno, String nombreLector) {
        LocalDateTime ahora = LocalDateTime.now();

        // 1. Verificar "Anti-Spam": ¿Ya registró hace menos de 10 segundos?
        if (ultimoRegistroCache.containsKey(alumno.getId_alumno())) {
            LocalDateTime ultimo = ultimoRegistroCache.get(alumno.getId_alumno());
            long segundosDiferencia = ChronoUnit.SECONDS.between(ultimo, ahora);

            if (segundosDiferencia < 10) {
                logger.info("Registro ignorado por duplicidad rápida para alumno: {}", alumno.getId_alumno());
                return false; // No guardamos nada, es la misma pulsación
            }
        }

        // 2. Guardar en Base de Datos
        RegistroAsistenciaModel registro = new RegistroAsistenciaModel(alumno, ahora, nombreLector);
        asistenciaRepository.save(registro);

        // 3. Actualizar cache
        ultimoRegistroCache.put(alumno.getId_alumno(), ahora);
        logger.info("Entrada registrada: {} en {}", alumno.getPrimerNombre(), nombreLector);

        return true; // Se guardó exitosamente
    }
}