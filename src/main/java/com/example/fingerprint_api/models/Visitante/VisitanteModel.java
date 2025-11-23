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

    // Campos de auditoría y seguridad
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

    // Campos de información personal
    @Column(name = "primer_nombre", length = 255)
    private String primerNombre;

    @Column(name = "segundo_nombre", length = 45)
    private String segundoNombre;

    @Column(name = "apellido_paterno", length = 255)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 255)
    private String apellidoMaterno;

    @Column(name = "num_telefono")
    private Long numTelefono;

    @Column(name = "activo", length = 255)
    private String activo;

    // Campos específicos del visitante
    @Column(name = "procedencia", length = 255)
    private String procedencia;

    @Column(name = "motivo_visita", length = 255)
    private String motivoVisita;

    @Column(name = "identificacion", length = 255)
    private String identificacion;

    // Campos para QR Temporal
    @Column(name = "qr_temporal", length = 255)
    private String qrTemporal;

    @Column(name = "qr_expiracion")
    private LocalDateTime qrExpiracion;

    // GETTERS Y SETTERS (Asegúrate de incluir todos los nuevos y eliminar los viejos)

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

    public String getSegundoNombre() {
        return segundoNombre;
    }

    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
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

    public String getActivo() {
        return activo;
    }

    public void setActivo(String activo) {
        this.activo = activo;
    }

    public String getProcedencia() {
        return procedencia;
    }

    public void setProcedencia(String procedencia) {
        this.procedencia = procedencia;
    }

    public String getMotivoVisita() {
        return motivoVisita;
    }

    public void setMotivoVisita(String motivoVisita) {
        this.motivoVisita = motivoVisita;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getQrTemporal() {
        return qrTemporal;
    }

    public void setQrTemporal(String qrTemporal) {
        this.qrTemporal = qrTemporal;
    }

    public LocalDateTime getQrExpiracion() {
        return qrExpiracion;
    }

    public void setQrExpiracion(LocalDateTime qrExpiracion) {
        this.qrExpiracion = qrExpiracion;
    }
}