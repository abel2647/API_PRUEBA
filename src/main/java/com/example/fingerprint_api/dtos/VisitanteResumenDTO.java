package com.example.fingerprint_api.dtos;
import java.time.LocalDateTime;

public class VisitanteResumenDTO {
    private Integer id;
    // CAMBIO: Separamos nombreCompleto en 3 partes
    private String primerNombre;
    private String apellidoPaterno;
    private String apellidoMaterno;

    private String asunto;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimaFechaEscaneo;
    private int totalEntradas;
    private String ultimaPuerta;
    private LocalDateTime fechaExpiracion;

    public VisitanteResumenDTO(Integer id, String primerNombre, String apellidoPaterno, String apellidoMaterno,
                               String asunto, LocalDateTime fecha, LocalDateTime ultimoEscaneo,
                               int total, String puerta, LocalDateTime fechaExpiracion) {
        this.id = id;
        this.primerNombre = primerNombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.asunto = asunto;
        this.fechaCreacion = fecha;
        this.ultimaFechaEscaneo = ultimoEscaneo;
        this.totalEntradas = total;
        this.ultimaPuerta = puerta;
        this.fechaExpiracion = fechaExpiracion;
    }

    // Getters
    public Integer getId() { return id; }
    public String getPrimerNombre() { return primerNombre; }
    public String getApellidoPaterno() { return apellidoPaterno; }
    public String getApellidoMaterno() { return apellidoMaterno; }
    public String getAsunto() { return asunto; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getUltimaFechaEscaneo() { return ultimaFechaEscaneo; }
    public int getTotalEntradas() { return totalEntradas; }
    public String getUltimaPuerta() { return ultimaPuerta; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
}