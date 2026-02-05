package com.example.fingerprint_api.dtos;

public class ValidacionResponseDTO {
    private boolean acceso;
    private String mensaje;
    private String visitante;
    private String asunto;
    private int totalAccesos;
    private int totalSalidas; // <--- ESTE ES EL NUEVO CAMPO QUE FALTABA
    private String puerta;

    // Constructor Vacío
    public ValidacionResponseDTO() {
    }

    // Constructor Completo (AHORA CON 7 ARGUMENTOS)
    public ValidacionResponseDTO(boolean acceso, String mensaje, String visitante, String asunto, int totalAccesos, int totalSalidas, String puerta) {
        this.acceso = acceso;
        this.mensaje = mensaje;
        this.visitante = visitante;
        this.asunto = asunto;
        this.totalAccesos = totalAccesos;
        this.totalSalidas = totalSalidas; // <--- Asignamos el nuevo campo
        this.puerta = puerta;
    }

    // --- GETTERS Y SETTERS ---

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

    // GETTER Y SETTER NUEVO
    public int getTotalSalidas() { return totalSalidas; }
    public void setTotalSalidas(int totalSalidas) { this.totalSalidas = totalSalidas; }

    public String getPuerta() { return puerta; }
    public void setPuerta(String puerta) { this.puerta = puerta; }
}