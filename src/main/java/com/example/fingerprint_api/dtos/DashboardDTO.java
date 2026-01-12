package com.example.fingerprint_api.dtos;

import java.util.List;
import java.util.Map;

public class DashboardDTO {
    // KPIs
    private long totalAlumnosHoy;
    private long totalVisitantesHoy;

    // Gráficas
    private List<Map<String, Object>> asistenciaSemanal; // Para gráfica de líneas
    private List<Map<String, Object>> entradasPorPuerta; // Para gráfica de donut

    // Constructor, Getters y Setters
    public DashboardDTO() {}

    public long getTotalAlumnosHoy() { return totalAlumnosHoy; }
    public void setTotalAlumnosHoy(long totalAlumnosHoy) { this.totalAlumnosHoy = totalAlumnosHoy; }

    public long getTotalVisitantesHoy() { return totalVisitantesHoy; }
    public void setTotalVisitantesHoy(long totalVisitantesHoy) { this.totalVisitantesHoy = totalVisitantesHoy; }

    public List<Map<String, Object>> getAsistenciaSemanal() { return asistenciaSemanal; }
    public void setAsistenciaSemanal(List<Map<String, Object>> asistenciaSemanal) { this.asistenciaSemanal = asistenciaSemanal; }

    public List<Map<String, Object>> getEntradasPorPuerta() { return entradasPorPuerta; }
    public void setEntradasPorPuerta(List<Map<String, Object>> entradasPorPuerta) { this.entradasPorPuerta = entradasPorPuerta; }
}