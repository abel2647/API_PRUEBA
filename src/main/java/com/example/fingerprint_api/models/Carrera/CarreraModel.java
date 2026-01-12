package com.example.fingerprint_api.models.Carrera;

import jakarta.persistence.*;

@Entity
@Table(name = "Carrera")
public class CarreraModel {
    @Id
    @Column(name = "id_carrera")
    private String id_carrera;

    // 'nullable = false' hace que este campo sea obligatorio en la BD
    @Column(name = "nombre_carrera")
    private String nombrecarrera;

    // --- Getters y Setters ---

    public String getId_carrera() {
        return id_carrera;
    }

    public void setId_carrera(String id_carrera) {
        this.id_carrera = id_carrera;
    }

    public String getNombrecarrera() {
        return nombrecarrera;
    }

    public void setNombrecarrera(String nombrecarrera) {
        this.nombrecarrera = nombrecarrera;
    }
}