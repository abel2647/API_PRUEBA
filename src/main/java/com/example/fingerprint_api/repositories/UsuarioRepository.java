package com.example.fingerprint_api.repositories;

import com.example.fingerprint_api.models.Usuario.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, Integer> {
    Optional<UsuarioModel> findByUsername(String username);
}
