package com.example.fingerprint_api.dtos;
import java.time.LocalDateTime;

public class VisitanteResumenDTO {
    private Integer id;
    private String nombreCompleto;
    private String asunto;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimaFechaEscaneo;
    private int totalEntradas;
    private String ultimaPuerta;

    public VisitanteResumenDTO(Integer id, String nombre, String asunto, LocalDateTime fecha, LocalDateTime ultimoEscaneo, int total, String puerta) {
        this.id = id;
        this.nombreCompleto = nombre;
        this.asunto = asunto;
        this.fechaCreacion = fecha;
        this.ultimaFechaEscaneo = ultimoEscaneo;
        this.totalEntradas = total;
        this.ultimaPuerta = puerta;
    }

    // Getters y Setters OBLIGATORIOS
    public Integer getId() { return id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getAsunto() { return asunto; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getUltimaFechaEscaneo() { return ultimaFechaEscaneo; }
    public int getTotalEntradas() { return totalEntradas; }
    public String getUltimaPuerta() { return ultimaPuerta; }
}