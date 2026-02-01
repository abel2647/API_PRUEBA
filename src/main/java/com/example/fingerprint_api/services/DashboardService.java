package com.example.fingerprint_api.services;

import com.example.fingerprint_api.dtos.DashboardDTO;
import com.example.fingerprint_api.models.Asistencia.RegistroAsistenciaModel;
import com.example.fingerprint_api.models.Visitante.RegistroEntradaVisitanteModel;
import com.example.fingerprint_api.models.Entrada.EntradaModel;
import com.example.fingerprint_api.repositories.EntradaRepository;
import com.example.fingerprint_api.repositories.RegistroAsistenciaRepository;
import com.example.fingerprint_api.repositories.RegistroEntradaVisitanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    @Autowired
    private EntradaRepository entradaRepo;

    /**
     * Método modificado para soportar filtro por puertaId.
     */
    public DashboardDTO obtenerEstadisticasFiltradas(String fechaStr, String horaInicioStr, String horaFinStr, String tipoPersona, Integer puertaId) {
        DashboardDTO dto = new DashboardDTO();

        // ----------------------------------------------------------------
        // 0. CONFIGURACIÓN DE FECHAS Y HORAS
        // ----------------------------------------------------------------
        LocalDate fecha = (fechaStr != null && !fechaStr.isEmpty()) ? LocalDate.parse(fechaStr) : LocalDate.now();
        LocalTime horaInicio = (horaInicioStr != null && !horaInicioStr.isEmpty()) ? LocalTime.parse(horaInicioStr) : LocalTime.of(0, 0);
        LocalTime horaFin = (horaFinStr != null && !horaFinStr.isEmpty()) ? LocalTime.parse(horaFinStr) : LocalTime.of(23, 59, 59);

        LocalDateTime inicio = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fin = LocalDateTime.of(fecha, horaFin);

        boolean incluirAlumnos = tipoPersona.equals("TODOS") || tipoPersona.equals("ALUMNO");
        boolean incluirVisitantes = tipoPersona.equals("TODOS") || tipoPersona.equals("VISITANTE");

        // ----------------------------------------------------------------
        // PASO 1: KPIs (Totales Generales)
        // ----------------------------------------------------------------
        if (incluirAlumnos) {
            long totalAlumnos = alumnoRepo.countByFechaHoraBetweenAndPuerta(inicio, fin, puertaId);
            dto.setTotalAlumnosHoy(totalAlumnos);
        }
        if (incluirVisitantes) {
            long totalVisitantes = visitanteRepo.countByFechaHoraBetweenAndPuerta(inicio, fin, puertaId);
            dto.setTotalVisitantesHoy(totalVisitantes);
        }

        // ----------------------------------------------------------------
        // PASO 2: GRÁFICA DE LÍNEAS (Por Hora)
        // ----------------------------------------------------------------
        Map<Integer, Map<String, Object>> mapaHoras = new TreeMap<>();
        for (int i = 0; i < 24; i++) {
            Map<String, Object> dato = new HashMap<>();
            dato.put("hora", String.format("%02d:00", i));
            dato.put("alumnos", 0);
            dato.put("visitantes", 0);
            mapaHoras.put(i, dato);
        }

        if (incluirAlumnos) {
            List<Map<String, Object>> res = alumnoRepo.countPorHoraIntervalo(inicio, fin, puertaId);
            for (Map<String, Object> m : res) {
                Integer hora = convertirAInt(m.get("hora"));
                if (mapaHoras.containsKey(hora)) {
                    mapaHoras.get(hora).put("alumnos", convertirAInt(m.get("total")));
                }
            }
        }

        if (incluirVisitantes) {
            List<Map<String, Object>> res = visitanteRepo.countPorHoraIntervalo(inicio, fin, puertaId);
            for (Map<String, Object> m : res) {
                Integer hora = convertirAInt(m.get("hora"));
                if (mapaHoras.containsKey(hora)) {
                    mapaHoras.get(hora).put("visitantes", convertirAInt(m.get("total")));
                }
            }
        }
        dto.setAsistenciaSemanal(new ArrayList<>(mapaHoras.values()));

        // ----------------------------------------------------------------
        // PASO 3: GRÁFICA DE BARRAS (TODAS LAS PUERTAS EXISTENTES)
        // ----------------------------------------------------------------
        List<EntradaModel> todasLasPuertas = entradaRepo.findAll();
        Map<String, Integer> conteoPorPuerta = new HashMap<>();
        for (EntradaModel puerta : todasLasPuertas) {
            conteoPorPuerta.put(String.valueOf(puerta.getId()), 0);
        }

        if (incluirAlumnos) {
            List<Map<String, Object>> resAlumnos = alumnoRepo.countPorPuertaIntervalo(inicio, fin, puertaId);
            for (Map<String, Object> m : resAlumnos) {
                String pId = String.valueOf(m.get("puerta"));
                Integer cantidad = convertirAInt(m.get("total"));
                if (conteoPorPuerta.containsKey(pId)) {
                    conteoPorPuerta.put(pId, conteoPorPuerta.get(pId) + cantidad);
                }
            }
        }

        if (incluirVisitantes) {
            List<Map<String, Object>> resVisitantes = visitanteRepo.countPorPuertaIntervalo(inicio, fin, puertaId);
            for (Map<String, Object> m : resVisitantes) {
                String pId = String.valueOf(m.get("puerta"));
                Integer cantidad = convertirAInt(m.get("total"));
                if (conteoPorPuerta.containsKey(pId)) {
                    conteoPorPuerta.put(pId, conteoPorPuerta.get(pId) + cantidad);
                }
            }
        }

        List<Map<String, Object>> listaPuertas = new ArrayList<>();
        for (EntradaModel puerta : todasLasPuertas) {
            Map<String, Object> item = new HashMap<>();
            String idStr = String.valueOf(puerta.getId());
            item.put("puerta", "Puerta " + idStr);
            item.put("total", conteoPorPuerta.get(idStr));
            listaPuertas.add(item);
        }

        dto.setEntradasPorPuerta(listaPuertas);

        return dto;
    }

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

    // MODIFICADO: Acepta puertaId y agrega columnas para visitantes
    public ByteArrayInputStream generarReporteExcel(String fechaStr, String horaInicioStr, String horaFinStr, String tipoPersona, Integer puertaId) {
        LocalDate fecha = (fechaStr != null && !fechaStr.isEmpty()) ? LocalDate.parse(fechaStr) : LocalDate.now();
        LocalTime horaInicio = (horaInicioStr != null && !horaInicioStr.isEmpty()) ? LocalTime.parse(horaInicioStr) : LocalTime.of(0, 0);
        LocalTime horaFin = (horaFinStr != null && !horaFinStr.isEmpty()) ? LocalTime.parse(horaFinStr) : LocalTime.of(23, 59, 59);

        LocalDateTime inicio = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fin = LocalDateTime.of(fecha, horaFin);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // -----------------------------------------------------------
            // HOJA 1: ALUMNOS
            // -----------------------------------------------------------
            if (tipoPersona.equals("TODOS") || tipoPersona.equals("ALUMNO")) {
                Sheet sheetAlumnos = workbook.createSheet("Alumnos");
                String[] headersAlumnos = {"Nombre", "Apellido Paterno", "Apellido Materno", "No. Control", "Carrera", "Puerta", "Fecha y Hora Entrada"};
                Row headerRow = sheetAlumnos.createRow(0);

                for (int i = 0; i < headersAlumnos.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headersAlumnos[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Usamos el método filtrado
                List<RegistroAsistenciaModel> alumnos = alumnoRepo.findByFechaHoraBetweenAndPuerta(inicio, fin, puertaId);
                int rowIdx = 1;

                for (RegistroAsistenciaModel reg : alumnos) {
                    Row row = sheetAlumnos.createRow(rowIdx++);
                    row.createCell(0).setCellValue(reg.getAlumno().getPrimerNombre());
                    row.createCell(1).setCellValue(reg.getAlumno().getApellidoPaterno());
                    row.createCell(2).setCellValue(reg.getAlumno().getApellidoMaterno());
                    row.createCell(3).setCellValue(reg.getAlumno().getNumeroControl());
                    row.createCell(4).setCellValue(reg.getAlumno().getCarreraClave());
                    row.createCell(5).setCellValue(reg.getIdEntrada()); // Agregué puerta para alumno también
                    row.createCell(6).setCellValue(reg.getFechaHora().format(formatter));
                }
                for(int i=0; i<headersAlumnos.length; i++) sheetAlumnos.autoSizeColumn(i);
            }

            // -----------------------------------------------------------
            // HOJA 2: VISITANTES (Modificado con nuevos campos)
            // -----------------------------------------------------------
            if (tipoPersona.equals("TODOS") || tipoPersona.equals("VISITANTE")) {
                Sheet sheetVisitantes = workbook.createSheet("Visitantes");

                // NUEVOS ENCABEZADOS
                String[] headersVisitantes = {
                        "Nombre", "Apellido Paterno", "Apellido Materno", "Asunto",
                        "Acompañantes", "Puerta Entrada", "Fecha Hora Actual",
                        "Total Visitas Históricas", "Última Visita Registrada"
                };
                Row headerRow = sheetVisitantes.createRow(0);

                for (int i = 0; i < headersVisitantes.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headersVisitantes[i]);
                    cell.setCellStyle(headerStyle);
                }

                List<RegistroEntradaVisitanteModel> visitantes = visitanteRepo.findByFechaHoraBetweenAndPuerta(inicio, fin, puertaId);
                int rowIdx = 1;

                for (RegistroEntradaVisitanteModel reg : visitantes) {
                    var vis = reg.getCodigoTemporal().getVisitante();
                    var motivo = reg.getCodigoTemporal().getAsunto();
                    var acomp = reg.getCodigoTemporal().getNumeroAcompañantes();

                    // Consultas adicionales para historial (Por cada fila - OJO con rendimiento masivo)
                    long totalVisitas = visitanteRepo.countTotalVisitasPorVisitante(vis.getId_visitante());
                    LocalDateTime ultimaVisita = visitanteRepo.findUltimaVisitaPorVisitante(vis.getId_visitante());
                    String ultimaVisitaStr = (ultimaVisita != null) ? ultimaVisita.format(formatter) : "N/A";

                    Row row = sheetVisitantes.createRow(rowIdx++);
                    row.createCell(0).setCellValue(vis.getPrimerNombre());
                    row.createCell(1).setCellValue(vis.getApellidoPaterno());
                    row.createCell(2).setCellValue(vis.getApellidoMaterno());
                    row.createCell(3).setCellValue(motivo);
                    row.createCell(4).setCellValue(acomp != null ? acomp : 0);

                    // Puerta específica de ESTE registro
                    row.createCell(5).setCellValue(reg.getEntrada());
                    // Fecha hora actual
                    row.createCell(6).setCellValue(reg.getFechaHora().format(formatter));

                    // Nuevas columnas
                    row.createCell(7).setCellValue(totalVisitas);
                    row.createCell(8).setCellValue(ultimaVisitaStr);
                }
                for(int i=0; i<headersVisitantes.length; i++) sheetVisitantes.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel: " + e.getMessage());
        }
    }
}