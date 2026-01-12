package com.example.fingerprint_api.dtos;

import java.time.LocalDateTime;

public class HistorialDTO {
    private String id; // Cambiado a String para que coincida con tu base de datos (P1E1)
    private String nombreCompleto;
    private String matricula;
    private String carrera;
    private String asunto;
    private String tipo;
    private String fechaHora;
    private LocalDateTime fechaEntrada;

    // Constructor vacío
    public HistorialDTO() {}

    // Constructor para Visitantes (Mantenlo para no romper el otro controlador)
    public HistorialDTO(Integer id, String nombre, String paterno, String materno, String asunto, LocalDateTime fecha) {
        this.id = String.valueOf(id);
        this.nombreCompleto = (nombre + " " + paterno + " " + (materno != null ? materno : "")).trim();
        this.asunto = asunto;
        this.fechaEntrada = fecha;
        this.fechaHora = (fecha != null) ? fecha.toString() : "";
    }

    // Getters y Setters Manuales
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public LocalDateTime getFechaEntrada() { return fechaEntrada; }
    public void setFechaEntrada(LocalDateTime fechaEntrada) { this.fechaEntrada = fechaEntrada; }
}