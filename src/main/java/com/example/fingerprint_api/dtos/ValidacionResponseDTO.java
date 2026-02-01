package com.example.fingerprint_api.dtos;

public class ValidacionResponseDTO {
    private boolean acceso;
    private String mensaje;
    private String visitante;
    private String asunto;

    // Constructor sin contadores
    public ValidacionResponseDTO(boolean acceso, String mensaje, String visitante, String asunto) {
        this.acceso = acceso;
        this.mensaje = mensaje;
        this.visitante = visitante;
        this.asunto = asunto;
    }

    // Getters y Setters
    public boolean isAcceso() { return acceso; }
    public void setAcceso(boolean acceso) { this.acceso = acceso; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getVisitante() { return visitante; }
    public void setVisitante(String visitante) { this.visitante = visitante; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
}