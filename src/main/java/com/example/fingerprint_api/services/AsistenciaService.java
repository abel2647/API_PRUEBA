package com.example.fingerprint_api.services;

import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import com.example.fingerprint_api.models.Asistencia.RegistroAsistenciaModel;
import com.example.fingerprint_api.models.Entrada.EntradaModel;
import com.example.fingerprint_api.repositories.EntradaRepository;
import com.example.fingerprint_api.repositories.AlumnoRepository;
import com.example.fingerprint_api.repositories.RegistroAsistenciaRepository;
import com.example.fingerprint_api.dtos.HistorialDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AsistenciaService {

    @Autowired
    private RegistroAsistenciaRepository asistenciaRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private EntradaRepository entradaRepository;

    @Transactional
    public RegistroAsistenciaModel registrarEntrada(AlumnoModel alumno, Integer numPuerta) {
        long totalEnPuerta = asistenciaRepository.countByIdEntrada(numPuerta);
        long siguienteEntrada = totalEnPuerta + 1;

        String idLogica = "P" + numPuerta + "E" + siguienteEntrada;
        EntradaModel entradaRef = entradaRepository.getReferenceById(numPuerta);

        RegistroAsistenciaModel nuevaAsistencia = new RegistroAsistenciaModel(
                idLogica,
                alumno,
                LocalDateTime.now(),
                entradaRef.getId()
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

    /**
     * Obtiene el historial con filtros múltiples (Nombre, Apellidos y Matrícula)
     */
    public List<HistorialDTO> obtenerHistorialCompleto(String nombre, String paterno, String materno, String matricula) {
        List<RegistroAsistenciaModel> registros;

        // Si todos los parámetros son nulos o vacíos, traemos todo
        if ((nombre == null || nombre.isEmpty()) &&
                (paterno == null || paterno.isEmpty()) &&
                (materno == null || materno.isEmpty()) &&
                (matricula == null || matricula.isEmpty())) {
            registros = asistenciaRepository.findAll();
        } else {
            // Aquí usamos el nuevo método de búsqueda filtrada del Repositorio
            registros = asistenciaRepository.filtrarHistorial(nombre, paterno, materno, matricula);
        }

        return registros.stream().map(reg -> {
            HistorialDTO dto = new HistorialDTO();

            // 1. ID (getIdRegistroEntrada según tu RegistroAsistenciaModel)
            dto.setId(reg.getIdRegistroEntrada());

            // 2. Procesamos el Alumno con tus métodos reales (getPrimerNombre, etc)
            AlumnoModel alu = reg.getAlumno();
            if (alu != null) {
                String nombreCompleto = alu.getPrimerNombre() +
                        (alu.getSegundoNombre() != null && !alu.getSegundoNombre().isEmpty() ? " " + alu.getSegundoNombre() : "") +
                        " " + alu.getApellidoPaterno() +
                        (alu.getApellidoMaterno() != null && !alu.getApellidoMaterno().isEmpty() ? " " + alu.getApellidoMaterno() : "");

                dto.setNombreCompleto(nombreCompleto.trim());
                dto.setMatricula(alu.getNumeroControl());
                dto.setCarrera(alu.getCarreraClave() != null ? alu.getCarreraClave() : "N/A");
            }

            // 3. Fecha y Tipo
            dto.setFechaHora(reg.getFechaHora() != null ? reg.getFechaHora().toString() : "");
            dto.setTipo("ENTRADA");

            return dto;
        }).collect(Collectors.toList());
    }
}