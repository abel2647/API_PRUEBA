package com.example.fingerprint_api.services;

import com.example.fingerprint_api.dtos.DashboardDTO;
import com.example.fingerprint_api.repositories.RegistroAsistenciaRepository;
import com.example.fingerprint_api.repositories.RegistroEntradaVisitanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class DashboardService {

    @Autowired
    private RegistroAsistenciaRepository alumnoRepo;

    @Autowired
    private RegistroEntradaVisitanteRepository visitanteRepo;

    /**
     * Método principal para obtener todas las métricas del dashboard según filtros.
     */
    public DashboardDTO obtenerEstadisticasFiltradas(String fechaStr, String horaInicioStr, String horaFinStr, String tipoPersona) {
        DashboardDTO dto = new DashboardDTO();

        // ----------------------------------------------------------------
        // 1. CONFIGURACIÓN DE FECHAS Y HORAS (FILTROS)
        // ----------------------------------------------------------------

        // Fecha: Si es nula, usamos HOY
        LocalDate fecha = (fechaStr != null && !fechaStr.isEmpty())
                ? LocalDate.parse(fechaStr)
                : LocalDate.now();

        // Hora Inicio: Si es nula, usamos 00:00
        LocalTime horaInicio = (horaInicioStr != null && !horaInicioStr.isEmpty())
                ? LocalTime.parse(horaInicioStr)
                : LocalTime.MIN;

        // Hora Fin: Si es nula, usamos 23:59:59
        LocalTime horaFin = (horaFinStr != null && !horaFinStr.isEmpty())
                ? LocalTime.parse(horaFinStr)
                : LocalTime.MAX;

        LocalDateTime inicio = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fin = LocalDateTime.of(fecha, horaFin);

        // Determinar qué datos consultar
        boolean incluirAlumnos = "TODOS".equals(tipoPersona) || "ALUMNO".equals(tipoPersona);
        boolean incluirVisitantes = "TODOS".equals(tipoPersona) || "VISITANTE".equals(tipoPersona);


        // ----------------------------------------------------------------
        // 2. CALCULO DE KPIs (TOTALES)
        // ----------------------------------------------------------------
        long totalAlumnos = incluirAlumnos ? alumnoRepo.countByFechaHoraBetween(inicio, fin) : 0;
        long totalVisitantes = incluirVisitantes ? visitanteRepo.countByFechaHoraBetween(inicio, fin) : 0;

        dto.setTotalAlumnosHoy(totalAlumnos);
        dto.setTotalVisitantesHoy(totalVisitantes);


        // ----------------------------------------------------------------
        // 3. GRÁFICA 1: AFLUENCIA POR PUERTA (1 al 6)
        // ----------------------------------------------------------------
        // Inicializamos mapa para asegurar que siempre existan las puertas 1 a 6 (aunque tengan 0)
        Map<Integer, Map<String, Object>> mapaPuertas = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            Map<String, Object> datos = new HashMap<>();
            datos.put("puerta", i);
            datos.put("alumnos", 0);
            datos.put("visitantes", 0);
            mapaPuertas.put(i, datos);
        }

        // Llenar datos de ALUMNOS
        if (incluirAlumnos) {
            List<Map<String, Object>> res = alumnoRepo.countPorPuertaIntervalo(inicio, fin);
            for (Map<String, Object> m : res) {
                Integer puerta = convertirAInt(m.get("puerta"));
                if (mapaPuertas.containsKey(puerta)) {
                    mapaPuertas.get(puerta).put("alumnos", convertirAInt(m.get("total")));
                }
            }
        }

        // Llenar datos de VISITANTES
        if (incluirVisitantes) {
            List<Map<String, Object>> res = visitanteRepo.countPorPuertaIntervalo(inicio, fin);
            for (Map<String, Object> m : res) {
                Integer puerta = convertirAInt(m.get("puerta"));
                if (mapaPuertas.containsKey(puerta)) {
                    mapaPuertas.get(puerta).put("visitantes", convertirAInt(m.get("total")));
                }
            }
        }

        // Convertir Mapa a Lista ordenada por número de puerta
        List<Map<String, Object>> listaPuertas = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            listaPuertas.add(mapaPuertas.get(i));
        }
        dto.setEntradasPorPuerta(listaPuertas);


        // ----------------------------------------------------------------
        // 4. GRÁFICA 2: TENDENCIA HORARIA (Por Hora)
        // ----------------------------------------------------------------
        // Usamos TreeMap para que las horas se ordenen automáticamente (07, 08, 09...)
        Map<Integer, Map<String, Object>> mapaHoras = new TreeMap<>();

        int hInicioInt = horaInicio.getHour();
        int hFinInt = horaFin.getHour();

        // Inicializar todas las horas del rango con 0
        for (int h = hInicioInt; h <= hFinInt; h++) {
            Map<String, Object> datos = new HashMap<>();
            datos.put("hora", String.format("%02d:00", h)); // Formato "07:00"
            datos.put("alumnos", 0);
            datos.put("visitantes", 0);
            mapaHoras.put(h, datos);
        }

        // Llenar datos de ALUMNOS por Hora
        if (incluirAlumnos) {
            List<Map<String, Object>> res = alumnoRepo.countPorHoraIntervalo(inicio, fin);
            for (Map<String, Object> m : res) {
                Integer hora = convertirAInt(m.get("hora"));
                // Solo insertamos si la hora está dentro del rango filtrado
                if (mapaHoras.containsKey(hora)) {
                    mapaHoras.get(hora).put("alumnos", convertirAInt(m.get("total")));
                }
            }
        }

        // Llenar datos de VISITANTES por Hora
        if (incluirVisitantes) {
            List<Map<String, Object>> res = visitanteRepo.countPorHoraIntervalo(inicio, fin);
            for (Map<String, Object> m : res) {
                Integer hora = convertirAInt(m.get("hora"));
                if (mapaHoras.containsKey(hora)) {
                    mapaHoras.get(hora).put("visitantes", convertirAInt(m.get("total")));
                }
            }
        }

        // Convertir a Lista
        dto.setAsistenciaSemanal(new ArrayList<>(mapaHoras.values()));

        return dto;
    }

    /**
     * Método auxiliar para convertir resultados de JPA (que pueden ser Long, BigInteger, etc.) a Integer seguro.
     */
    private Integer convertirAInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}