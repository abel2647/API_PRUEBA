package com.example.fingerprint_api.services;

import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import com.example.fingerprint_api.models.Asistencia.RegistroAsistenciaModel;
import com.example.fingerprint_api.models.Entrada.EntradaModel;
import com.example.fingerprint_api.repositories.EntradaRepository; // <--- Importar
import com.example.fingerprint_api.repositories.AlumnoRepository;
import com.example.fingerprint_api.repositories.RegistroAsistenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AsistenciaService {

    @Autowired
    private RegistroAsistenciaRepository asistenciaRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    //Agregue esto para foranea
    @Autowired
    private EntradaRepository entradaRepository; // <--- 1. INYECTAR REPOSITORIO

    // DEJA SOLO ESTA VERSIÓN DEL MÉTODO
    @Transactional
    public RegistroAsistenciaModel registrarEntrada(AlumnoModel alumno, Integer numPuerta) {
        // 1. Conteo para el consecutivo
        long totalEnPuerta = asistenciaRepository.countByIdEntrada(numPuerta);
        long siguienteEntrada = totalEnPuerta + 1;

        // 2. ID Lógica (P1E1, P1E2...)
        String idLogica = "P" + numPuerta + "E" + siguienteEntrada;

        //Agregue esto para foranea
        EntradaModel entradaRef = entradaRepository.getReferenceById(numPuerta);

        // 3. Crear objeto con los 4 parámetros (ID, Alumno, Fecha, Puerta)
        RegistroAsistenciaModel nuevaAsistencia = new RegistroAsistenciaModel(
                idLogica,
                alumno,
                LocalDateTime.now(),
                //numPuerta
                //Cambie esto para foranea
                entradaRef.getId() // <--- Pasamos la referencia gestionada por Hibernate
        );

        return asistenciaRepository.save(nuevaAsistencia);
    }

    @Transactional
    public RegistroAsistenciaModel registrarPorNumeroControl(String numeroControl, Integer numPuerta) {
        Optional<AlumnoModel> alumnoOpt = alumnoRepository.findByNumeroControl(numeroControl);

        if (alumnoOpt.isPresent()) {
            return registrarEntrada(alumnoOpt.get(), numPuerta);
        } else {
            throw new RuntimeException("Alumno no encontrado");
        }
    }
}