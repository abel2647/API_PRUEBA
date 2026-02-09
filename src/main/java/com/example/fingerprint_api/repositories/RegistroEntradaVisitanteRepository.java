package com.example.fingerprint_api.repositories;

import com.example.fingerprint_api.models.Visitante.RegistroEntradaVisitanteModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface RegistroEntradaVisitanteRepository extends JpaRepository<RegistroEntradaVisitanteModel, Integer> {

    long countByFechaHoraBetween(LocalDateTime start, LocalDateTime end);

    // KPI con filtro (JPQL)
    @Query("SELECT COUNT(r) FROM RegistroEntradaVisitanteModel r " +
            "WHERE r.fechaHora BETWEEN :start AND :end " +
            "AND (:puertaId IS NULL OR r.entrada = :puertaId)")
    long countByFechaHoraBetweenAndPuerta(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          @Param("puertaId") Integer puertaId);

    // Gráfica semanal (Nativa, mantenemos nativeQuery porque usa funciones de fecha específicas de DB)
    @Query(value = "SELECT DATE(fecha_hora) as fecha, COUNT(*) as cantidad " +
            "FROM registro_entrada_visitante " +
            "WHERE fecha_hora >= :fechaLimite " +
            "GROUP BY DATE(fecha_hora) ORDER BY fecha ASC", nativeQuery = true)
    List<Map<String, Object>> countEntradasPorDia(@Param("fechaLimite") LocalDateTime fechaLimite);

    // Gráfica donas (JPQL)
    @Query("SELECT r.entrada as puerta, COUNT(r) as total FROM RegistroEntradaVisitanteModel r GROUP BY r.entrada")
    List<Map<String, Object>> countEntradasPorPuerta();

    // Gráfica donas filtrada (JPQL)
    @Query("SELECT r.entrada as puerta, COUNT(r) as total " +
            "FROM RegistroEntradaVisitanteModel r " +
            "WHERE r.fechaHora BETWEEN :inicio AND :fin " +
            "AND (:puertaId IS NULL OR r.entrada = :puertaId) " +
            "GROUP BY r.entrada")
    List<Map<String, Object>> countPorPuertaIntervalo(@Param("inicio") LocalDateTime inicio,
                                                      @Param("fin") LocalDateTime fin,
                                                      @Param("puertaId") Integer puertaId);

    // Gráfica por horas (Nativa, necesaria por la función HOUR)
    @Query(value = "SELECT HOUR(fecha_hora) as hora, COUNT(*) as total " +
            "FROM registro_entrada_visitante " +
            "WHERE fecha_hora BETWEEN :inicio AND :fin " +
            "AND (:puertaId IS NULL OR entrada = :puertaId) " +
            "GROUP BY HOUR(fecha_hora)", nativeQuery = true)
    List<Map<String, Object>> countPorHoraIntervalo(@Param("inicio") LocalDateTime inicio,
                                                    @Param("fin") LocalDateTime fin,
                                                    @Param("puertaId") Integer puertaId);

    // Lista para Excel (JPQL)
    @Query("SELECT r FROM RegistroEntradaVisitanteModel r " +
            "WHERE r.fechaHora BETWEEN :inicio AND :fin " +
            "AND (:puertaId IS NULL OR r.entrada = :puertaId)")
    List<RegistroEntradaVisitanteModel> findByFechaHoraBetweenAndPuerta(@Param("inicio") LocalDateTime inicio,
                                                                        @Param("fin") LocalDateTime fin,
                                                                        @Param("puertaId") Integer puertaId);

    // --- MÉTODOS PARA HISTÓRICO EXCEL ---

    // Total de visitas (JPQL)
    @Query("SELECT COUNT(r) FROM RegistroEntradaVisitanteModel r WHERE r.codigoTemporal.visitante.id_visitante = :visitanteId")
    long countTotalVisitasPorVisitante(@Param("visitanteId") Integer visitanteId);

    // CORRECCIÓN AQUÍ:
    // Antes usaba nativeQuery con tablas mal nombradas. Ahora usa JPQL referenciando las entidades.
    // Esto evita el error "Table doesn't exist".
    @Query("SELECT MAX(r.fechaHora) FROM RegistroEntradaVisitanteModel r " +
            "WHERE r.codigoTemporal.visitante.id_visitante = :visitanteId")
    LocalDateTime findUltimaVisitaPorVisitante(@Param("visitanteId") Integer visitanteId);

    // ESTE ES EL MÉTODO QUE TE FALTA Y CAUSA EL ERROR
    @Query("SELECT COUNT(r) FROM RegistroEntradaVisitanteModel r WHERE r.codigoTemporal.visitante.id_visitante = :visitanteId")
    int contarTotalEntradas(@Param("visitanteId") Integer visitanteId);

    // --- NUEVO MÉTODO PARA REPORTE EXCEL VISITANTES (AGRUPADO) ---
    // Devuelve: [0]Nombre, [1]Paterno, [2]Materno, [3]Asunto, [4]Acompañantes, [5]TotalEntradasHoy, [6]HoraMin, [7]HoraMax
    @Query("SELECT v.primerNombre, v.apellidoPaterno, v.apellidoMaterno, " +
            "ct.asunto, ct.numeroAcompañantes, " +
            "COUNT(r), MIN(r.fechaHora), MAX(r.fechaHora) " +
            "FROM RegistroEntradaVisitanteModel r " +
            "JOIN r.codigoTemporal ct " +
            "JOIN ct.visitante v " +
            "WHERE r.fechaHora BETWEEN :inicio AND :fin " +
            "AND (:puertaId IS NULL OR r.entrada = :puertaId) " +
            "GROUP BY v.id_visitante, v.primerNombre, v.apellidoPaterno, v.apellidoMaterno, ct.asunto, ct.numeroAcompañantes " +
            "ORDER BY v.apellidoPaterno ASC")
    List<Object[]> obtenerResumenVisitantes(@Param("inicio") LocalDateTime inicio,
                                            @Param("fin") LocalDateTime fin,
                                            @Param("puertaId") Integer puertaId);
}