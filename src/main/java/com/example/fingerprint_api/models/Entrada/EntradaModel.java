package com.example.fingerprint_api.models.Entrada;

import jakarta.persistence.*;

@Entity
@Table(name = "entrada")
public class EntradaModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrada") // O el nombre de tu PK en esa tabla
    private Integer id;

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
}