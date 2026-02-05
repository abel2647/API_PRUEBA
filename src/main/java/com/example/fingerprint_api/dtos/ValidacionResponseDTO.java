package com.example.fingerprint_api.dtos;

public class ValidacionResponseDTO {
    private boolean acceso;
    private String mensaje;
    private String visitante;
    private String asunto;
    // NUEVOS CAMPOS
    private int totalAccesos;
    private String puerta;

    public ValidacionResponseDTO(boolean acceso, String mensaje, String visitante, String asunto, int totalAccesos, String puerta) {
        this.acceso = acceso;
        this.mensaje = mensaje;
        this.visitante = visitante;
        this.asunto = asunto;
        this.totalAccesos = totalAccesos;
        this.puerta = puerta;
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
    public int getTotalAccesos() { return totalAccesos; }
    public void setTotalAccesos(int totalAccesos) { this.totalAccesos = totalAccesos; }
    public String getPuerta() { return puerta; }
    public void setPuerta(String puerta) { this.puerta = puerta; }
}