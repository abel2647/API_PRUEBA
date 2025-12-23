package com.example.fingerprint_api.repositories;

import com.example.fingerprint_api.models.Visitante.VisitanteModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;

@Repository
public interface VisitanteRepository extends CrudRepository <VisitanteModel,Integer> {

    Optional<VisitanteModel> findByUuid(String uuid);

    ArrayList<VisitanteModel> findByDeleted(int i);

    ArrayList<VisitanteModel> findByPrimerNombreContainingIgnoreCase(String nombre);

    // ELIMINADAS: findByActivo, findByProcedencia, findByQrTemporal

}