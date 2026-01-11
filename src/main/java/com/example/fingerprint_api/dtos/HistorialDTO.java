package com.example.fingerprint_api.dtos;
import java.time.LocalDateTime;

public class HistorialDTO {
    private Integer id;
    private String nombreCompleto;
    private String asunto;
    private LocalDateTime fechaEntrada;

    // Constructor, Getters y Setters
    public HistorialDTO(Integer id, String nombre, String paterno, String materno, String asunto, LocalDateTime fecha) {
        this.id = id;
        this.nombreCompleto = (nombre + " " + paterno + " " + (materno != null ? materno : "")).trim();
        this.asunto = asunto;
        this.fechaEntrada = fecha; // Usamos la fecha de creación/expiración como referencia
    }

    // Agrega los getters aquí...
    public Integer getId() { return id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getAsunto() { return asunto; }
    public LocalDateTime getFechaEntrada() { return fechaEntrada; }
}