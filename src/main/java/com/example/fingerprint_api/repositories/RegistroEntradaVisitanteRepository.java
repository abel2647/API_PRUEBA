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

    @Query(value = "SELECT DATE(fecha_hora) as fecha, COUNT(*) as cantidad " +
            "FROM registro_entrada_visitante " +
            "WHERE fecha_hora >= :fechaLimite " +
            "GROUP BY DATE(fecha_hora) ORDER BY fecha ASC", nativeQuery = true)
    List<Map<String, Object>> countEntradasPorDia(@Param("fechaLimite") LocalDateTime fechaLimite);

    @Query("SELECT r.entrada as puerta, COUNT(r) as total FROM RegistroEntradaVisitanteModel r GROUP BY r.entrada")
    List<Map<String, Object>> countEntradasPorPuerta();

    // Agrega este método
    @Query("SELECT r.entrada as puerta, COUNT(r) as total " +
            "FROM RegistroEntradaVisitanteModel r " +
            "WHERE r.fechaHora BETWEEN :inicio AND :fin " +
            "GROUP BY r.entrada")
    List<Map<String, Object>> countPorPuertaIntervalo(@Param("inicio") LocalDateTime inicio,
                                                      @Param("fin") LocalDateTime fin);

    @Query(value = "SELECT HOUR(fecha_hora) as hora, COUNT(*) as total " +
            "FROM registro_entrada_visitante " +
            "WHERE fecha_hora BETWEEN :inicio AND :fin " +
            "GROUP BY HOUR(fecha_hora)", nativeQuery = true)
    List<Map<String, Object>> countPorHoraIntervalo(@Param("inicio") LocalDateTime inicio,
                                                    @Param("fin") LocalDateTime fin);

    // Agrega este método para obtener la lista completa
    List<RegistroEntradaVisitanteModel> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

}