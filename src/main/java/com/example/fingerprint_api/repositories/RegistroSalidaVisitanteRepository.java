package com.example.fingerprint_api.repositories;

import com.example.fingerprint_api.models.Visitante.RegistroSalidaVisitanteModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroSalidaVisitanteRepository extends JpaRepository<RegistroSalidaVisitanteModel, Integer> {
}