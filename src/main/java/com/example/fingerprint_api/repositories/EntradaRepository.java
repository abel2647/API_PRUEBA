package com.example.fingerprint_api.repositories;

import com.example.fingerprint_api.models.Entrada.EntradaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntradaRepository extends JpaRepository<EntradaModel, Integer> {
    // No necesitas métodos extra, con los de JpaRepository basta
}