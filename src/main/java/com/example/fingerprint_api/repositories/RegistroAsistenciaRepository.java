package com.example.fingerprint_api.repositories;

import com.example.fingerprint_api.models.Asistencia.RegistroAsistenciaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroAsistenciaRepository extends JpaRepository<RegistroAsistenciaModel, String> {
    // Esto nos permite contar cuántos registros tiene la Puerta
    long countByIdEntrada(Integer idEntrada);
}