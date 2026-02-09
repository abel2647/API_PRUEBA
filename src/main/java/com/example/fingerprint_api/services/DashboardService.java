package com.example.fingerprint_api.services;

import com.example.fingerprint_api.dtos.DashboardDTO;
import com.example.fingerprint_api.models.Asistencia.RegistroAsistenciaModel;
import com.example.fingerprint_api.models.Visitante.RegistroEntradaVisitanteModel;
import com.example.fingerprint_api.models.Entrada.EntradaModel;
import com.example.fingerprint_api.repositories.EntradaRepository;
import com.example.fingerprint_api.repositories.RegistroAsistenciaRepository;
import com.example.fingerprint_api.repositories.RegistroEntradaVisitanteRepository;
import com.itextpdf.kernel.geom.PageSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


// IMPORTS PARA PDF (iText 7) - Asegúrate de que sean estos exactamente
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
//import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment; // <--- Este es el del error
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

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

            //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm:ss");

            // -----------------------------------------------------------
            // HOJA 1: ALUMNOS
            // -----------------------------------------------------------
           /* if (tipoPersona.equals("TODOS") || tipoPersona.equals("ALUMNO")) {
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

            */

            // -----------------------------------------------------------
            // HOJA 1: ALUMNOS (Modificado: Resumen por día)
            // -----------------------------------------------------------
            if (tipoPersona.equals("TODOS") || tipoPersona.equals("ALUMNO")) {
                Sheet sheetAlumnos = workbook.createSheet("Alumnos");

                // Nuevos encabezados para el resumen
                String[] headersAlumnos = {
                        "No. Control",
                        "Nombre",
                        "Apellido Paterno",
                        "Apellido Materno",
                        "Carrera",
                        "Total Entradas", // Cuántas veces entró
                        "Fecha",
                        "Primera Entrada",    // Hora del primer ingreso
                        "Última Entrada"      // Hora del último ingreso
                };

                Row headerRow = sheetAlumnos.createRow(0);

                for (int i = 0; i < headersAlumnos.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headersAlumnos[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Llamamos al nuevo método que devuelve Object[]
                List<Object[]> resumenAlumnos = alumnoRepo.obtenerResumenAsistencia(inicio, fin, puertaId);
                int rowIdx = 1;

                for (Object[] fila : resumenAlumnos) {
                    // Extraemos los datos por su posición en el SELECT del repositorio
                    String matricula = (String) fila[0];
                    String nombre    = (String) fila[1];
                    String paterno   = (String) fila[2];
                    String materno   = (String) fila[3];
                    String carrera   = (String) fila[4]; // O carreraClave según tu modelo
                    Long total       = (Long)   fila[5];
                    LocalDateTime primera = (LocalDateTime) fila[6];
                    LocalDateTime ultima  = (LocalDateTime) fila[7];

                    Row row = sheetAlumnos.createRow(rowIdx++);

                    row.createCell(0).setCellValue(matricula);
                    row.createCell(1).setCellValue(nombre);
                    row.createCell(2).setCellValue(paterno);
                    row.createCell(3).setCellValue(materno);
                    row.createCell(4).setCellValue(carrera);

                    // Datos calculados
                    row.createCell(5).setCellValue(total);
                    //row.createCell(6).setCellValue(primera != null ? primera.format(formatter) : "-");
                    //row.createCell(7).setCellValue(ultima != null ? ultima.format(formatter) : "-");
                    // Lógica modificada solo al escribir en la celda
                    if (primera != null) {
                        row.createCell(6).setCellValue(primera.format(fmtFecha)); // Fecha
                        row.createCell(7).setCellValue(primera.format(fmtHora));  // Hora Inicio
                    } else {
                        row.createCell(6).setCellValue("-");
                        row.createCell(7).setCellValue("-");
                    }

                    if (ultima != null) {
                        row.createCell(8).setCellValue(ultima.format(fmtHora));   // Hora Fin
                    } else {
                        row.createCell(8).setCellValue("-");
                    }
                }

                // Ajustar ancho de columnas
                for(int i=0; i<headersAlumnos.length; i++) sheetAlumnos.autoSizeColumn(i);
            }

            // -----------------------------------------------------------
            // HOJA 2: VISITANTES (Modificado con nuevos campos)
            // -----------------------------------------------------------
            if (tipoPersona.equals("TODOS") || tipoPersona.equals("VISITANTE")) {
                Sheet sheetVisitantes = workbook.createSheet("Visitantes");

                // Encabezados limpios para el reporte diario
                String[] headersVisitantes = {
                        "Nombre",
                        "Apellido Paterno",
                        "Apellido Materno",
                        "Asunto",
                        "Acompañantes",
                        "Total Entradas",
                        "Fecha",// Cuántas veces entró en este rango
                        "Primera Entrada",    // Hora
                        "Última Entrada"      // Hora
                };

                Row headerRow = sheetVisitantes.createRow(0);

                for (int i = 0; i < headersVisitantes.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headersVisitantes[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Llamamos al nuevo método agrupado
                List<Object[]> resumenVisitantes = visitanteRepo.obtenerResumenVisitantes(inicio, fin, puertaId);
                int rowIdx = 1;

                for (Object[] fila : resumenVisitantes) {
                    // Mapeo de columnas según la Query
                    String nombre    = (String) fila[0];
                    String paterno   = (String) fila[1];
                    String materno   = (String) fila[2];
                    String asunto    = (String) fila[3];
                    Integer acomp    = (fila[4] != null) ? (Integer) fila[4] : 0;
                    Long totalHoy    = (Long)   fila[5];
                    LocalDateTime primera = (LocalDateTime) fila[6];
                    LocalDateTime ultima  = (LocalDateTime) fila[7];

                    Row row = sheetVisitantes.createRow(rowIdx++);

                    row.createCell(0).setCellValue(nombre);
                    row.createCell(1).setCellValue(paterno);
                    row.createCell(2).setCellValue(materno);
                    row.createCell(3).setCellValue(asunto);
                    row.createCell(4).setCellValue(acomp);

                    // Datos calculados
                    row.createCell(5).setCellValue(totalHoy); // Total de veces que entró HOY
                    //row.createCell(6).setCellValue(primera != null ? primera.format(formatter) : "-");
                    //row.createCell(7).setCellValue(ultima != null ? ultima.format(formatter) : "-");
                    // Lógica modificada solo al escribir en la celda
                    if (primera != null) {
                        row.createCell(6).setCellValue(primera.format(fmtFecha)); // Fecha
                        row.createCell(7).setCellValue(primera.format(fmtHora));  // Hora Inicio
                    } else {
                        row.createCell(6).setCellValue("-");
                        row.createCell(7).setCellValue("-");
                    }

                    if (ultima != null) {
                        row.createCell(8).setCellValue(ultima.format(fmtHora));   // Hora Fin
                    } else {
                        row.createCell(8).setCellValue("-");
                    }
                }

                for(int i=0; i<headersVisitantes.length; i++) sheetVisitantes.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel: " + e.getMessage());
        }
    }


    public ByteArrayInputStream generarReportePdf(String fechaStr, String horaInicioStr, String horaFinStr, String tipoPersona, Integer puertaId) {
        LocalDate fecha = (fechaStr != null && !fechaStr.isEmpty()) ? LocalDate.parse(fechaStr) : LocalDate.now();
        LocalTime horaInicio = (horaInicioStr != null && !horaInicioStr.isEmpty()) ? LocalTime.parse(horaInicioStr) : LocalTime.of(0, 0);
        LocalTime horaFin = (horaFinStr != null && !horaFinStr.isEmpty()) ? LocalTime.parse(horaFinStr) : LocalTime.of(23, 59, 59);

        LocalDateTime inicio = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fin = LocalDateTime.of(fecha, horaFin);

        DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm:ss");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(out);
            com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            pdf.setDefaultPageSize(com.itextpdf.kernel.geom.PageSize.A4.rotate());
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);

            document.setMargins(25, 36, 40, 36);

            // --- ENCABEZADO (LOGO ITO + TEXTO INSTITUCIONAL) ---
            com.itextpdf.layout.element.Table membreteTable = new com.itextpdf.layout.element.Table(com.itextpdf.layout.properties.UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
            membreteTable.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);

            // Celda Izquierda: Logo ITO
            com.itextpdf.layout.element.Cell logoCell = new com.itextpdf.layout.element.Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
            try {
                com.itextpdf.layout.element.Image itoLogo = new com.itextpdf.layout.element.Image(com.itextpdf.io.image.ImageDataFactory.create("src/main/resources/static/images/Logo_ITO.png")).setWidth(50);
                logoCell.add(itoLogo);
            } catch (Exception e) {
                logoCell.add(new com.itextpdf.layout.element.Paragraph("Logo Institucional").setFontSize(8));
            }
            membreteTable.addCell(logoCell);

            // Celda Derecha: Texto "Instituto Tecnológico de Oaxaca"
            com.itextpdf.layout.element.Cell textCell = new com.itextpdf.layout.element.Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT)
                    .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);

            textCell.add(new com.itextpdf.layout.element.Paragraph("Instituto Tecnológico de Oaxaca")
                    .setBold()
                    .setFontSize(10)); // Tamaño ligeramente mayor para resaltar
            membreteTable.addCell(textCell);

            document.add(membreteTable);

            // Línea divisoria sutil
            //document.add(new com.itextpdf.layout.element.Paragraph("").setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(com.itextpdf.kernel.colors.ColorConstants.GRAY, 0.5f)).setMarginTop(-5));
            document.add(new com.itextpdf.layout.element.Paragraph("\n")
                    .setMarginTop(1)    // Ajusta este número para más o menos espacio
                    .setMarginBottom(1));

            // --- FILTROS ---
            String nombrePuerta = (puertaId == null) ? "Todas las puertas" : "Puerta " + puertaId;
            com.itextpdf.layout.element.Paragraph filtros = new com.itextpdf.layout.element.Paragraph()
                    .add("Fecha: " + fecha.format(fmtFecha) + "\n")
                    .add("Horario: " + horaInicioStr + " - " + horaFinStr + "\n")
                    .add("Filtro: " + nombrePuerta)
                    .setFontSize(8)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT)
                    .setMarginTop(5);
            document.add(filtros);

            // --- TÍTULO CENTRAL ---
            document.add(new com.itextpdf.layout.element.Paragraph("REPORTE DE ACCESOS")
                    .setBold().setFontSize(14).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER).setMarginTop(5));

            // --- TABLA ALUMNOS ---
            if (tipoPersona.equals("TODOS") || tipoPersona.equals("ALUMNO")) {
                document.add(new com.itextpdf.layout.element.Paragraph("RESUMEN DE ALUMNOS").setBold().setFontSize(10).setMarginTop(10));

                float[] colWidths = {10f, 13f, 13f, 13f, 18f, 6f, 9f, 9f, 9f};
                com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(com.itextpdf.layout.properties.UnitValue.createPercentArray(colWidths)).useAllAvailableWidth();

                String[] headers = {"Matrícula", "Nombre", "Ap. Paterno", "Ap. Materno", "Carrera", "Total", "Fecha", "1ra Entrada", "Últ. Entrada"};
                for (String h : headers) {
                    table.addHeaderCell(new com.itextpdf.layout.element.Cell()
                            .add(new com.itextpdf.layout.element.Paragraph(h).setBold().setFontSize(9))
                            .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
                }

                List<Object[]> resumenAlumnos = alumnoRepo.obtenerResumenAsistencia(inicio, fin, puertaId);
                for (Object[] fila : resumenAlumnos) {
                    for (int i=0; i<6; i++) table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(fila[i] != null ? fila[i].toString() : "").setFontSize(8)));

                    LocalDateTime p = (LocalDateTime) fila[6];
                    LocalDateTime u = (LocalDateTime) fila[7];
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(p != null ? p.format(fmtFecha) : "-").setFontSize(8)));
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(p != null ? p.format(fmtHora) : "-").setFontSize(8)));
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(u != null ? u.format(fmtHora) : "-").setFontSize(8)));
                }
                document.add(table);
            }

            // --- TABLA VISITANTES ---
            if (tipoPersona.equals("TODOS") || tipoPersona.equals("VISITANTE")) {
                document.add(new com.itextpdf.layout.element.Paragraph("RESUMEN DE VISITANTES").setBold().setFontSize(10).setMarginTop(15));

                float[] colWidthsV = {11f, 11f, 11f, 16f, 8f, 7f, 12f, 12f, 12f};
                com.itextpdf.layout.element.Table tableV = new com.itextpdf.layout.element.Table(com.itextpdf.layout.properties.UnitValue.createPercentArray(colWidthsV)).useAllAvailableWidth();

                String[] headersV = {"Nombre", "Ap. Paterno", "Ap. Materno", "Asunto", "Acomp.", "Total", "Fecha", "1ra Entrada", "Últ. Entrada"};
                for (String h : headersV) {
                    tableV.addHeaderCell(new com.itextpdf.layout.element.Cell()
                            .add(new com.itextpdf.layout.element.Paragraph(h).setBold().setFontSize(9))
                            .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
                }

                List<Object[]> resumenVisitantes = visitanteRepo.obtenerResumenVisitantes(inicio, fin, puertaId);
                for (Object[] fila : resumenVisitantes) {
                    for (int i=0; i<4; i++) tableV.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(fila[i] != null ? fila[i].toString() : "").setFontSize(8)));
                    tableV.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(fila[4] != null ? fila[4].toString() : "0").setFontSize(8)));
                    tableV.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(fila[5].toString()).setFontSize(8)));

                    LocalDateTime p = (LocalDateTime) fila[6];
                    LocalDateTime u = (LocalDateTime) fila[7];
                    tableV.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(p != null ? p.format(fmtFecha) : "-").setFontSize(8)));
                    tableV.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(p != null ? p.format(fmtHora) : "-").setFontSize(8)));
                    tableV.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(u != null ? u.format(fmtHora) : "-").setFontSize(8)));
                }
                document.add(tableV);
            }

            // --- PIE DE PÁGINA ---
            int n = pdf.getNumberOfPages();
            String txtGenerado = "Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            for (int i = 1; i <= n; i++) {
                document.showTextAligned(new com.itextpdf.layout.element.Paragraph(txtGenerado).setFontSize(7).setItalic(),
                        36, 25, i, com.itextpdf.layout.properties.TextAlignment.LEFT, com.itextpdf.layout.properties.VerticalAlignment.BOTTOM, 0);
            }

            document.close();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage());
        }
    }
}