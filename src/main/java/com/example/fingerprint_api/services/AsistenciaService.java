package com.example.fingerprint_api.services;

import com.example.fingerprint_api.dtos.HistorialDTO;
import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import com.example.fingerprint_api.models.Asistencia.RegistroAsistenciaModel;
import com.example.fingerprint_api.repositories.AlumnoRepository;
import com.example.fingerprint_api.repositories.RegistroAsistenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AsistenciaService {

    @Autowired
    private RegistroAsistenciaRepository registroAsistenciaRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * MÉTODO PARA LA HUELLA DIGITAL (El que pedía el error)
     */
    @Transactional
    public RegistroAsistenciaModel registrarEntrada(AlumnoModel alumno, int idEntrada) {
        RegistroAsistenciaModel asistencia = new RegistroAsistenciaModel();

        // Generar ID único (P1E...)
        long conteo = registroAsistenciaRepository.count() + 1;
        asistencia.setIdRegistroEntrada("P" + idEntrada + "E" + conteo + "-" + (System.currentTimeMillis() % 1000));

        asistencia.setAlumno(alumno);
        asistencia.setIdEntrada(idEntrada);
        asistencia.setFechaHora(LocalDateTime.now());

        return registroAsistenciaRepository.save(asistencia);
    }

    /**
     * MÉTODO PARA EL ESCÁNER DE BARRAS
     */
    @Transactional
    public RegistroAsistenciaModel registrarPorNumeroControl(String numeroControl, Integer idEntrada) {
        AlumnoModel alumno = alumnoRepository.findByNumeroControl(numeroControl)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado: " + numeroControl));

        return registrarEntrada(alumno, idEntrada);
    }

    /**
     * OBTENER HISTORIAL FILTRADO
     */
    // Cambia la firma para recibir 'fecha'
    public List<HistorialDTO> obtenerHistorialEstudiantes(String nombre, String paterno, String matricula, String fecha) {

        // Ahora le pasamos 4 parámetros al repository (antes eran 3, por eso marcaba rojo)
        List<RegistroAsistenciaModel> listaBase = registroAsistenciaRepository.filtrarHistorial(
                nombre,
                paterno,
                matricula,
                fecha
        );

        return listaBase.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * MAPEADOR A DTO (Para que el Frontend pueda EDITAR)
     */
    private HistorialDTO convertirADTO(RegistroAsistenciaModel model) {
        HistorialDTO dto = new HistorialDTO();

        dto.setId(model.getIdRegistroEntrada());

        if (model.getAlumno() != null) {
            AlumnoModel al = model.getAlumno();
            dto.setAlumno_id(al.getId_alumno());
            dto.setUuid(al.getUuid());

            String nombreComp = al.getPrimerNombre() + " " +
                    al.getApellidoPaterno() + " " +
                    (al.getApellidoMaterno() != null ? al.getApellidoMaterno() : "");

            dto.setNombreCompleto(nombreComp.trim());
            dto.setMatricula(al.getNumeroControl());
            dto.setCarrera(al.getCarreraClave());
        }

        dto.setTipo("ENTRADA");

        if (model.getFechaHora() != null) {
            dto.setFechaHora(model.getFechaHora().format(formatter));
            dto.setFechaEntrada(model.getFechaHora());
        }

        return dto;
    }
}