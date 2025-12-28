package com.example.fingerprint_api.repositories;

import com.example.fingerprint_api.models.Asistencia.RegistroAsistenciaModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface RegistroAsistenciaRepository extends CrudRepository<RegistroAsistenciaModel, Long> {
    // Puedes agregar este método si luego quieres ver el historial de un alumno específico:
    ArrayList<RegistroAsistenciaModel> findByAlumno_Id(Integer idAlumno);
}