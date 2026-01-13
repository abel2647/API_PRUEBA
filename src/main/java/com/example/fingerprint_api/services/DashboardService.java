package com.example.fingerprint_api.services;

import com.example.fingerprint_api.dtos.DashboardDTO;
import com.example.fingerprint_api.models.Asistencia.RegistroAsistenciaModel;
import com.example.fingerprint_api.models.Visitante.RegistroEntradaVisitanteModel;
import com.example.fingerprint_api.models.Entrada.EntradaModel; // <--- IMPORTANTE
import com.example.fingerprint_api.repositories.EntradaRepository; // <--- IMPORTANTE
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
    private EntradaRepository entradaRepo; // <--- NUEVA INYECCIÓN

    /**
     * Método principal para obtener todas las métricas del dashboard según filtros.
     */
    public DashboardDTO obtenerEstadisticasFiltradas(String fechaStr, String horaInicioStr, String horaFinStr, String tipoPersona) {
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
            long totalAlumnos = alumnoRepo.countByFechaHoraBetween(inicio, fin);
            dto.setTotalAlumnosHoy(totalAlumnos);
        }
        if (incluirVisitantes) {
            long totalVisitantes = visitanteRepo.countByFechaHoraBetween(inicio, fin);
            dto.setTotalVisitantesHoy(totalVisitantes);
        }

        // ----------------------------------------------------------------
        // PASO 2: GRÁFICA DE LÍNEAS (Por Hora)
        // ----------------------------------------------------------------
        Map<Integer, Map<String, Object>> mapaHoras = new TreeMap<>();
        // Inicializar las 24 horas en 0
        for (int i = 0; i < 24; i++) {
            Map<String, Object> dato = new HashMap<>();
            dato.put("hora", String.format("%02d:00", i));
            dato.put("alumnos", 0);
            dato.put("visitantes", 0);
            mapaHoras.put(i, dato);
        }

        if (incluirAlumnos) {
            List<Map<String, Object>> res = alumnoRepo.countPorHoraIntervalo(inicio, fin);
            for (Map<String, Object> m : res) {
                Integer hora = convertirAInt(m.get("hora"));
                if (mapaHoras.containsKey(hora)) {
                    mapaHoras.get(hora).put("alumnos", convertirAInt(m.get("total")));
                }
            }
        }

        if (incluirVisitantes) {
            List<Map<String, Object>> res = visitanteRepo.countPorHoraIntervalo(inicio, fin);
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

        // A. Traer TODAS las puertas de la base de datos (Ej. 1, 2, 3, 4, 5, 6)
        List<EntradaModel> todasLasPuertas = entradaRepo.findAll();

        // B. Mapa para sumarizar. Clave = ID Puerta (String), Valor = Total acumulado
        Map<String, Integer> conteoPorPuerta = new HashMap<>();

        // C. Inicializar TODAS las puertas en 0
        for (EntradaModel puerta : todasLasPuertas) {
            conteoPorPuerta.put(String.valueOf(puerta.getId()), 0);
        }

        // D. Sumar ALUMNOS (si aplica)
        if (incluirAlumnos) {
            List<Map<String, Object>> resAlumnos = alumnoRepo.countPorPuertaIntervalo(inicio, fin);
            for (Map<String, Object> m : resAlumnos) {
                String puertaId = String.valueOf(m.get("puerta"));
                Integer cantidad = convertirAInt(m.get("total"));

                if (conteoPorPuerta.containsKey(puertaId)) {
                    conteoPorPuerta.put(puertaId, conteoPorPuerta.get(puertaId) + cantidad);
                }
            }
        }

        // E. Sumar VISITANTES (si aplica)
        if (incluirVisitantes) {
            List<Map<String, Object>> resVisitantes = visitanteRepo.countPorPuertaIntervalo(inicio, fin);
            for (Map<String, Object> m : resVisitantes) {
                String puertaId = String.valueOf(m.get("puerta"));
                Integer cantidad = convertirAInt(m.get("total"));

                if (conteoPorPuerta.containsKey(puertaId)) {
                    conteoPorPuerta.put(puertaId, conteoPorPuerta.get(puertaId) + cantidad);
                }
            }
        }

        // F. Construir la lista final iterando sobre las puertas ORIGINALES para mantener orden
        List<Map<String, Object>> listaPuertas = new ArrayList<>();

        for (EntradaModel puerta : todasLasPuertas) {
            Map<String, Object> item = new HashMap<>();
            String idStr = String.valueOf(puerta.getId());

            // Etiqueta para la gráfica
            item.put("puerta", "Puerta " + idStr);
            // Valor (será 0 si no hubo nadie, o la suma si hubo)
            item.put("total", conteoPorPuerta.get(idStr));

            listaPuertas.add(item);
        }

        dto.setEntradasPorPuerta(listaPuertas);

        return dto;
    }


    /**

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

    //METODO PARA DESCARGA EN EXCEL
    public ByteArrayInputStream generarReporteExcel(String fechaStr, String horaInicioStr, String horaFinStr, String tipoPersona) {
        // 1. Configurar Fechas (Igual que antes)
        LocalDate fecha = (fechaStr != null && !fechaStr.isEmpty()) ? LocalDate.parse(fechaStr) : LocalDate.now();
        LocalTime horaInicio = (horaInicioStr != null && !horaInicioStr.isEmpty()) ? LocalTime.parse(horaInicioStr) : LocalTime.of(0, 0);
        LocalTime horaFin = (horaFinStr != null && !horaFinStr.isEmpty()) ? LocalTime.parse(horaFinStr) : LocalTime.of(23, 59, 59);

        LocalDateTime inicio = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fin = LocalDateTime.of(fecha, horaFin);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Estilo Global para Encabezados (Negrita)
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

                // Encabezados Específicos de Alumnos
                String[] headersAlumnos = {"Nombre", "Apellido Paterno", "Apellido Materno", "No. Control", "Carrera", "Fecha y Hora Entrada"};
                Row headerRow = sheetAlumnos.createRow(0);

                for (int i = 0; i < headersAlumnos.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headersAlumnos[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Llenar Datos
                List<RegistroAsistenciaModel> alumnos = alumnoRepo.findByFechaHoraBetween(inicio, fin);
                int rowIdx = 1;

                for (RegistroAsistenciaModel reg : alumnos) {
                    Row row = sheetAlumnos.createRow(rowIdx++);
                    row.createCell(0).setCellValue(reg.getAlumno().getPrimerNombre());
                    row.createCell(1).setCellValue(reg.getAlumno().getApellidoPaterno());
                    row.createCell(2).setCellValue(reg.getAlumno().getApellidoMaterno());
                    row.createCell(3).setCellValue(reg.getAlumno().getNumeroControl());
                    row.createCell(4).setCellValue(reg.getAlumno().getCarreraClave());
                    row.createCell(5).setCellValue(reg.getFechaHora().format(formatter));
                }

                // Autoajustar columnas
                for(int i=0; i<headersAlumnos.length; i++) sheetAlumnos.autoSizeColumn(i);
            }

            // -----------------------------------------------------------
            // HOJA 2: VISITANTES
            // -----------------------------------------------------------
            if (tipoPersona.equals("TODOS") || tipoPersona.equals("VISITANTE")) {
                Sheet sheetVisitantes = workbook.createSheet("Visitantes");

                // Encabezados Específicos de Visitantes
                String[] headersVisitantes = {"Nombre", "Apellido Paterno", "Apellido Materno", "Asunto", "Acompañantes", "Fecha y Hora Entrada"};
                Row headerRow = sheetVisitantes.createRow(0);

                for (int i = 0; i < headersVisitantes.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headersVisitantes[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Llenar Datos
                List<RegistroEntradaVisitanteModel> visitantes = visitanteRepo.findByFechaHoraBetween(inicio, fin);
                int rowIdx = 1;

                for (RegistroEntradaVisitanteModel reg : visitantes) {
                    var vis = reg.getCodigoTemporal().getVisitante();
                    var motivo = reg.getCodigoTemporal().getAsunto();
                    var acomp = reg.getCodigoTemporal().getNumeroAcompañantes();

                    Row row = sheetVisitantes.createRow(rowIdx++);
                    row.createCell(0).setCellValue(vis.getPrimerNombre());
                    row.createCell(1).setCellValue(vis.getApellidoPaterno());
                    row.createCell(2).setCellValue(vis.getApellidoMaterno());
                    row.createCell(3).setCellValue(motivo);
                    row.createCell(4).setCellValue(acomp != null ? acomp : 0);
                    row.createCell(5).setCellValue(reg.getFechaHora().format(formatter));
                }

                // Autoajustar columnas
                for(int i=0; i<headersVisitantes.length; i++) sheetVisitantes.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel: " + e.getMessage());
        }
    }
}