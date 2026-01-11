package com.example.fingerprint_api.models.Alumno;

import com.digitalpersona.uareu.Fid;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alumnos")
public class AlumnoModel {

    public AlumnoModel() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alumno")
    private Integer id;

    @Column(name = "primer_nombre")
    private String primerNombre;

    @Column(name = "segundo_nombre")
    private String segundoNombre;

    @Column(name = "apellido_paterno")
    private String apellidoPaterno;

    @Column(name = "apellido_materno")
    private String apellidoMaterno;

    @Column(name = "carrera_clave")
    private String carreraClave;

    @Column(name = "numero_control")
    private String numeroControl;

    @Column(name = "num_telefono")
    private String numTelefono;

    @Column(name = "codigo_barra") // Campo que faltaba
    private String codigoBarra;

    @Column(name = "codigo_qr") // Campo que faltaba
    private String codigoQr;

    @Column(name = "huella_fmd", columnDefinition = "LONGBLOB")
    private byte[] huellaFmd;


    private Integer activo;
    private Integer deleted;
    private Integer version;
    private String uuid;
    private String usuario;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at") // Así se llama usualmente en la base de datos
    private LocalDateTime updatedAt;

    // --- GETTERS Y SETTERS ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    // Método alias para evitar errores en servicios viejos
    public Integer getId_alumno() { return id; }

    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }

    public String getSegundoNombre() { return segundoNombre; }
    public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public String getCarreraClave() { return carreraClave; }
    public void setCarreraClave(String carreraClave) { this.carreraClave = carreraClave; }

    public String getNumeroControl() { return numeroControl; }
    public void setNumeroControl(String numeroControl) { this.numeroControl = numeroControl; }

    public String getNumTelefono() { return numTelefono; }
    public void setNumTelefono(String numTelefono) { this.numTelefono = numTelefono; }

    public String getCodigoBarra() { return codigoBarra; } // Agregado
    public void setCodigoBarra(String codigoBarra) { this.codigoBarra = codigoBarra; }

    public String getCodigoQr() { return codigoQr; } // Agregado
    public void setCodigoQr(String codigoQr) { this.codigoQr = codigoQr; }

    public byte[] getHuellaFmd() { return huellaFmd; }
    public void setHuellaFmd(byte[] huellaFmd) { this.huellaFmd = huellaFmd; }

    public Integer getActivo() { return activo; }
    public void setActivo(Integer activo) { this.activo = activo; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdateAt() {return updatedAt;}

    public void setUpdateAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}
}