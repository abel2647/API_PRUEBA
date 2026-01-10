package com.example.fingerprint_api.repositories;

import com.example.fingerprint_api.models.Visitante.CodigoTemporalModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CodigoTemporalRepository extends JpaRepository<CodigoTemporalModel, Integer> {

    Optional<CodigoTemporalModel> findByUuid(String uuid);

    // --- CONSULTA SIN PAGINACIÓN ---
    // Devuelve una LISTA completa filtrada por los parámetros
    @Query("SELECT c FROM CodigoTemporalModel c JOIN c.visitante v WHERE " +
            "(:nombre IS NULL OR LOWER(v.primerNombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:paterno IS NULL OR LOWER(v.apellidoPaterno) LIKE LOWER(CONCAT('%', :paterno, '%'))) AND " +
            "(:materno IS NULL OR LOWER(v.apellidoMaterno) LIKE LOWER(CONCAT('%', :materno, '%'))) AND " +
            "(:fechaInicio IS NULL OR c.fechaExpiracion >= :fechaInicio) AND " +
            "(:fechaFin IS NULL OR c.fechaExpiracion <= :fechaFin) " +
            "ORDER BY c.fechaExpiracion DESC")
    List<CodigoTemporalModel> buscarHistorialSinPaginacion(
            @Param("nombre") String nombre,
            @Param("paterno") String paterno,
            @Param("materno") String materno,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}