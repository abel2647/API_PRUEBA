package com.example.fingerprint_api.dtos;

public class VisitanteRegistroDTO {
    private String primerNombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String sexo;
    private Integer edad;
    private Long numTelefono;
    private String asunto;
    private Integer numeroAcompañantes;
    private Integer usuario;

    // Getters y Setters (OBLIGATORIOS)
    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }
    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }
    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public Long getNumTelefono() { return numTelefono; }
    public void setNumTelefono(Long numTelefono) { this.numTelefono = numTelefono; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public Integer getNumeroAcompañantes() { return numeroAcompañantes; }
    public void setNumeroAcompañantes(Integer numeroAcompañantes) { this.numeroAcompañantes = numeroAcompañantes; }
    public Integer getUsuario() { return usuario; }
    public void setUsuario(Integer usuario) { this.usuario = usuario; }
}