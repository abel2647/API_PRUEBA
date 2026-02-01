package com.example.fingerprint_api.repositories;

import com.example.fingerprint_api.models.Visitante.VisitanteModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisitanteRepository extends JpaRepository<VisitanteModel, Integer> {

    Optional<VisitanteModel> findByNumTelefono(Long numTelefono);
    List<VisitanteModel> findByDeleted(Integer deleted);

    // CONSULTA VITAL: Solo traemos codigos, NO entradas (para evitar el error)
    @Query("SELECT DISTINCT v FROM VisitanteModel v LEFT JOIN FETCH v.codigos c WHERE v.deleted = 0")
    List<VisitanteModel> obtenerTodoElHistorial();
}