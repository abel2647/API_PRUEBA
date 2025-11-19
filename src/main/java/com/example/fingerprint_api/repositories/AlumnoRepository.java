package com.example.fingerprint_api.repositories;

import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;

@Repository
public interface AlumnoRepository extends CrudRepository <AlumnoModel,Integer> {

    Optional<AlumnoModel> findByNumeroControl(String numeroControl);

    Optional<AlumnoModel> findByUuid(String uuid);

    ArrayList<AlumnoModel> findByActivo(String number);

    ArrayList<AlumnoModel> findByDeleted(int i);

    ArrayList<AlumnoModel> findByPrimerNombreContainingIgnoreCase(String nombre);

    boolean existsByNumeroControl(String numeroControl);

    ArrayList<AlumnoModel> findByCarreraClave(String carreraClave);

    //cambios nuevos
    // En AlumnoRepository.java
    long countByCarreraClave(String carreraClave);
    Optional<AlumnoModel> findByNumeroControlAndUuid(String numeroControl, String uuid);
    ArrayList<AlumnoModel> findByPrimerNombreContainingIgnoreCaseOrSegundoNombreContainingIgnoreCaseOrApellidoPaternoContainingIgnoreCaseOrApellidoMaternoContainingIgnoreCase(
            String primerNombre, String segundoNombre, String apellidoPaterno, String apellidoMaterno);
}
