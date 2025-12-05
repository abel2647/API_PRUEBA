package com.example.fingerprint_api.models.Visitante;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Visitantes")
public class VisitanteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Integer id_visitante;

    // Campos de auditoría y seguridad (Mantenidos)
    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "updateAt")
    private LocalDateTime updateAt;

    @Column(name = "version")
    private Integer version;

    @Column(name = "usuario")
    private Integer usuario;

    @Column(name = "deleted")
    private Integer deleted;

    @Column(name = "uuid", length = 50)
    private String uuid;

    // Campos del Front-End (Imagen)
    @Column(name = "primer_nombre", length = 255)
    private String primerNombre; // Nombre

    @Column(name = "apellido_paterno", length = 255)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 255)
    private String apellidoMaterno;

    @Column(name = "sexo", length = 15) // << NUEVO CAMPO
    private String sexo;

    @Column(name = "edad") // << NUEVO CAMPO
    private Integer edad;

    @Column(name = "num_telefono")
    private Long numTelefono; // Teléfono

    @Column(name = "asunto", length = 255) // Mapeado de 'Asunto' (antes motivoVisita)
    private String asunto;

    // --- Campo para Visitas de Grupo (Registro Único) ---
    @Column(name = "num_acompanantes")
    private Integer numeroAcompañantes; // Nuevo campo para el número de personas en el grupo (además del representante)
// ...

    // ELIMINADOS: segundoNombre, activo, procedencia, identificacion, qrTemporal, qrExpiracion

    // GETTERS Y SETTERS

    public Integer getId_visitante() {
        return id_visitante;
    }
    public void setId_visitante(Integer id_visitante) {
        this.id_visitante = id_visitante;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdateAt() {
        return updateAt;
    }
    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }
    public Integer getVersion() {
        return version;
    }
    public void setVersion(Integer version) {
        this.version = version;
    }
    public Integer getUsuario() {
        return usuario;
    }
    public void setUsuario(Integer usuario) {
        this.usuario = usuario;
    }
    public Integer getDeleted() {
        return deleted;
    }
    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public String getPrimerNombre() {
        return primerNombre;
    }
    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }
    public String getApellidoPaterno() {
        return apellidoPaterno;
    }
    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }
    public String getApellidoMaterno() {
        return apellidoMaterno;
    }
    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }
    public Long getNumTelefono() {
        return numTelefono;
    }
    public void setNumTelefono(Long numTelefono) {
        this.numTelefono = numTelefono;
    }
    public String getAsunto() {
        return asunto;
    }
    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    //para masivo=========
    public Integer getNumeroAcompañantes() {
        return numeroAcompañantes;
    }

    public void setNumeroAcompañantes(Integer numeroAcompañantes) {
        this.numeroAcompañantes = numeroAcompañantes;
    }
    //================
    public String getSexo() {
        return sexo;
    }
    public void setSexo(String sexo) {
        this.sexo = sexo;
    }
    public Integer getEdad() {
        return edad;
    }
    public void setEdad(Integer edad) {
        this.edad = edad;
    }
}