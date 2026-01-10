package com.example.fingerprint_api.repositories;

import com.example.fingerprint_api.models.Visitante.VisitanteModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;

@Repository
public interface VisitanteRepository extends CrudRepository<VisitanteModel, Integer> {

    // Buscar si ya existe alguien con ese teléfono (para no duplicar personas)
    Optional<VisitanteModel> findByNumTelefono(Long numTelefono);

    ArrayList<VisitanteModel> findByDeleted(int deleted);

    ArrayList<VisitanteModel> findByPrimerNombreContainingIgnoreCase(String nombre);

    // NOTA: Aquí YA NO va findByUuid, porque el UUID está en la otra tabla.
}