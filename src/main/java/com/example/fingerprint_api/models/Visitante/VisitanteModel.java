package com.example.fingerprint_api.models.Visitante;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Visitante") // Asegúrate que coincida con tu BD (mayúsculas/minúsculas)
public class VisitanteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_visitante") // Nombre exacto en la BD
    private Integer id_visitante;

    // Datos de Auditoría
    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "updateAt")
    private LocalDateTime updateAt;

    @Column(name = "deleted")
    private Integer deleted; // 0 = Activo, 1 = Eliminado

    @Column(name = "usuario")
    private Integer usuario;

    // Datos Personales
    @Column(name = "primer_nombre")
    private String primerNombre;

    @Column(name = "apellido_paterno")
    private String apellidoPaterno;

    @Column(name = "apellido_materno")
    private String apellidoMaterno;

    @Column(name = "sexo")
    private String sexo;

    @Column(name = "edad")
    private Integer edad;

    @Column(name = "num_telefono")
    private Long numTelefono;

    // --- Getters y Setters ---
    public Integer getId_visitante() { return id_visitante; }
    public void setId_visitante(Integer id_visitante) { this.id_visitante = id_visitante; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdateAt() { return updateAt; }
    public void setUpdateAt(LocalDateTime updateAt) { this.updateAt = updateAt; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

    public Integer getUsuario() { return usuario; }
    public void setUsuario(Integer usuario) { this.usuario = usuario; }

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
}