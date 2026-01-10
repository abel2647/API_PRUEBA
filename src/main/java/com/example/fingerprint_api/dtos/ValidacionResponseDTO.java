package com.example.fingerprint_api.dtos;

import java.time.LocalDateTime;

public class ValidacionResponseDTO {
    // Datos de la Persona (Vienen de VisitanteModel)
    private String primerNombre;
    private String apellidoPaterno;

    // Datos del Pase/Visita (Vienen de CodigoTemporalModel)
    private String asunto;
    private Integer numeroAcompañantes;
    private LocalDateTime fechaExpiracion;
    private String uuid;

    // Estado de validación
    private boolean esValido;
    private String mensaje;

    public ValidacionResponseDTO() {}

    // --- GETTERS Y SETTERS ---
    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public Integer getNumeroAcompañantes() { return numeroAcompañantes; }
    public void setNumeroAcompañantes(Integer numeroAcompañantes) { this.numeroAcompañantes = numeroAcompañantes; }

    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public boolean isEsValido() { return esValido; }
    public void setEsValido(boolean esValido) { this.esValido = esValido; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}