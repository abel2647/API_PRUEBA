package com.example.fingerprint_api.models.Alumno;

//import java.sql.Blob;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "Alumnos")
public class AlumnoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Integer id_alumno;

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

    @Column(name = "numero_control", length = 45)
    private String numeroControl;

    @Column(name = "primer_nombre", length = 255)
    private String primerNombre;

    @Column(name = "segundo_nombre", length = 45)
    private String segundoNombre;

    @Column(name = "apellido_paterno", length = 255)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 255)
    private String apellidoMaterno;

    @Column(name = "carrera_clave", length = 255)
    private String carreraClave;

    @Column(name = "num_telefono")
    private Long numTelefono;

    @Column(name = "codigo_barra", length = 45)
    private String codigoBarra;

    @Column(name = "codigo_qr", length = 45)
    private String codigoQr;

    @Column(name = "activo", length = 255)
    private String activo;


    //GET AND SETTER'S

    public String getActivo() {
        return activo;
    }

    public void setActivo(String activo) {
        this.activo = activo;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getCarreraClave() {
        return carreraClave;
    }

    public void setCarreraClave(String carreraClave) {
        this.carreraClave = carreraClave;
    }

    public String getCodigoBarra() {
        return codigoBarra;
    }

    public void setCodigoBarra(String codigoBarra) {
        this.codigoBarra = codigoBarra;
    }

    public String getCodigoQr() {
        return codigoQr;
    }

    public void setCodigoQr(String codigoQr) {
        this.codigoQr = codigoQr;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public Integer getId_alumno() {
        return id_alumno;
    }

    public void setId_alumno(Integer id_alumno) {
        this.id_alumno = id_alumno;
    }

    public String getNumeroControl() {
        return numeroControl;
    }

    public void setNumeroControl(String numeroControl) {
        this.numeroControl = numeroControl;
    }

    public Long getNumTelefono() {
        return numTelefono;
    }

    public void setNumTelefono(Long numTelefono) {
        this.numTelefono = numTelefono;
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

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }

    public Integer getUsuario() {
        return usuario;
    }

    public void setUsuario(Integer usuario) {
        this.usuario = usuario;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

// Constructores
    //public Alumno() {}

    /*public Alumno(Long idAlumno, String numeroControl, String primerNombre,
                  String apellidoPaterno, String apellidoMaterno) {
        this.idAlumno = idAlumno;
        this.numeroControl = numeroControl;
        this.primerNombre = primerNombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
    }*/


    /* Metodo toString()*/

    @Override
    public String toString() {
        return "AlumnoModel{" +
                "activo='" + activo + '\'' +
                ", id_alumno=" + id_alumno +
                ", createdAt=" + createdAt +
                ", updateAt=" + updateAt +
                ", version=" + version +
                ", usuario=" + usuario +
                ", deleted=" + deleted +
                ", uuid='" + uuid + '\'' +
                ", numeroControl='" + numeroControl + '\'' +
                ", primerNombre='" + primerNombre + '\'' +
                ", segundoNombre='" + segundoNombre + '\'' +
                ", apellidoPaterno='" + apellidoPaterno + '\'' +
                ", apellidoMaterno='" + apellidoMaterno + '\'' +
                ", carreraClave='" + carreraClave + '\'' +
                ", numTelefono=" + numTelefono +
                ", codigoBarra='" + codigoBarra + '\'' +
                ", codigoQr='" + codigoQr + '\'' +
                '}';
    }
}
