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
    // Esto nos permite contar cuántos registros tiene la Puerta
    long countByIdEntrada(Integer idEntrada);

    //DASHBOARD
    // 1. Contar entradas hoy
    long countByFechaHoraBetween(LocalDateTime start, LocalDateTime end);

    // 2. Agrupar por fecha (para gráfica semanal)
    // Devuelve lista de mapas: [{fecha: "2023-10-01", cantidad: 50}, ...]
    @Query(value = "SELECT DATE(fecha_hora) as fecha, COUNT(*) as cantidad " +
            "FROM registro_entrada_alumno " +
            "WHERE fecha_hora >= :fechaLimite " +
            "GROUP BY DATE(fecha_hora) ORDER BY fecha ASC", nativeQuery = true)
    List<Map<String, Object>> countEntradasPorDia(@Param("fechaLimite") LocalDateTime fechaLimite);

    // 3. Agrupar por Puerta
    @Query("SELECT r.idEntrada as puerta, COUNT(r) as total FROM RegistroAsistenciaModel r GROUP BY r.idEntrada")
    List<Map<String, Object>> countEntradasPorPuerta();

    // Agrega este método
    @Query("SELECT r.idEntrada as puerta, COUNT(r) as total " +
            "FROM RegistroAsistenciaModel r " +
            "WHERE r.fechaHora BETWEEN :inicio AND :fin " +
            "GROUP BY r.idEntrada")
    List<Map<String, Object>> countPorPuertaIntervalo(@Param("inicio") LocalDateTime inicio,
                                                      @Param("fin") LocalDateTime fin);


    // Devuelve la hora (0-23) y la cantidad
    // Nota: HOUR() funciona en MySQL/MariaDB. Si usas PostgreSQL usa EXTRACT(HOUR FROM ...)
    @Query(value = "SELECT HOUR(fecha_hora) as hora, COUNT(*) as total " +
            "FROM registro_entrada_alumno " +
            "WHERE fecha_hora BETWEEN :inicio AND :fin " +
            "GROUP BY HOUR(fecha_hora)", nativeQuery = true)
    List<Map<String, Object>> countPorHoraIntervalo(@Param("inicio") LocalDateTime inicio,
                                                    @Param("fin") LocalDateTime fin);

    // RegistroAsistenciaRepository.java
        // ... otros métodos (count, etc) ...

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

    List<RegistroAsistenciaModel> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);
    }
