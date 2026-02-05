package com.example.fingerprint_api.repositories;

import com.example.fingerprint_api.models.Usuario.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, Integer> {
    @Query("SELECT u FROM UsuarioModel u WHERE u.username = :username")
    Optional<UsuarioModel> findByUsername(@Param("username") String username);
}
