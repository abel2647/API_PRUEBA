package com.example.fingerprint_api.repositories;

import com.example.fingerprint_api.models.Asistencia.RegistroAsistenciaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface RegistroAsistenciaRepository extends JpaRepository<RegistroAsistenciaModel, String> {

    long countByIdEntrada(Integer idEntrada);
    long countByFechaHoraBetween(LocalDateTime start, LocalDateTime end);

    // KPI Filtrado
    @Query("SELECT COUNT(r) FROM RegistroAsistenciaModel r " +
            "WHERE r.fechaHora BETWEEN :start AND :end " +
            "AND (:puertaId IS NULL OR r.idEntrada = :puertaId)")
    long countByFechaHoraBetweenAndPuerta(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          @Param("puertaId") Integer puertaId);

    @Query(value = "SELECT DATE(fecha_hora) as fecha, COUNT(*) as cantidad " +
            "FROM registro_entrada_alumno " +
            "WHERE fecha_hora >= :fechaLimite " +
            "GROUP BY DATE(fecha_hora) ORDER BY fecha ASC", nativeQuery = true)
    List<Map<String, Object>> countEntradasPorDia(@Param("fechaLimite") LocalDateTime fechaLimite);

    @Query("SELECT r.idEntrada as puerta, COUNT(r) as total FROM RegistroAsistenciaModel r GROUP BY r.idEntrada")
    List<Map<String, Object>> countEntradasPorPuerta();

    // MODIFICADO
    @Query("SELECT r.idEntrada as puerta, COUNT(r) as total " +
            "FROM RegistroAsistenciaModel r " +
            "WHERE r.fechaHora BETWEEN :inicio AND :fin " +
            "AND (:puertaId IS NULL OR r.idEntrada = :puertaId) " +
            "GROUP BY r.idEntrada")
    List<Map<String, Object>> countPorPuertaIntervalo(@Param("inicio") LocalDateTime inicio,
                                                      @Param("fin") LocalDateTime fin,
                                                      @Param("puertaId") Integer puertaId);


    // MODIFICADO (PostgreSQL/MySQL handling)
    @Query(value = "SELECT HOUR(fecha_hora) as hora, COUNT(*) as total " +
            "FROM registro_entrada_alumno " +
            "WHERE fecha_hora BETWEEN :inicio AND :fin " +
            "AND (:puertaId IS NULL OR id_entrada = :puertaId) " +
            "GROUP BY HOUR(fecha_hora)", nativeQuery = true)
    List<Map<String, Object>> countPorHoraIntervalo(@Param("inicio") LocalDateTime inicio,
                                                    @Param("fin") LocalDateTime fin,
                                                    @Param("puertaId") Integer puertaId);

    // ... otros métodos de filtrado ...
    @Query("SELECT r FROM RegistroAsistenciaModel r WHERE " +
            "(:nombre IS NULL OR LOWER(r.alumno.primerNombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:paterno IS NULL OR LOWER(r.alumno.apellidoPaterno) LIKE LOWER(CONCAT('%', :paterno, '%'))) AND " +
            "(:matricula IS NULL OR r.alumno.numeroControl LIKE CONCAT('%', :matricula, '%')) AND " +
            "(:fecha IS NULL OR CAST(r.fechaHora AS date) = CAST(:fecha AS date))")
    List<RegistroAsistenciaModel> filtrarHistorial(
            @Param("nombre") String nombre,
            @Param("paterno") String paterno,
            @Param("matricula") String matricula,
            @Param("fecha") String fecha
    );

    // MODIFICADO para Excel
    @Query("SELECT r FROM RegistroAsistenciaModel r " +
            "WHERE r.fechaHora BETWEEN :inicio AND :fin " +
            "AND (:puertaId IS NULL OR r.idEntrada = :puertaId)")
    List<RegistroAsistenciaModel> findByFechaHoraBetweenAndPuerta(@Param("inicio") LocalDateTime inicio,
                                                                  @Param("fin") LocalDateTime fin,
                                                                  @Param("puertaId") Integer puertaId);
}
