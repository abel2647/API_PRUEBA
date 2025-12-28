package com.example.fingerprint_api.models.Asistencia; // Ajusta el paquete según tu estructura

import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para guardar el historial de entradas/salidas.
 */
@Entity
@Table(name = "registro_entrada_alumno")
public class RegistroAsistenciaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con el alumno que entró
    @ManyToOne
    @JoinColumn(name = "id_alumno", nullable = false)
    private AlumnoModel alumno;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "lector_usado")
    private String lectorUsado; // Para saber en qué puerta entró

    // Constructor vacío
    public RegistroAsistenciaModel() {}

    // Constructor para facilitar el guardado
    public RegistroAsistenciaModel(AlumnoModel alumno, LocalDateTime fechaHora, String lectorUsado) {
        this.alumno = alumno;
        this.fechaHora = fechaHora;
        this.lectorUsado = lectorUsado;
    }

    // Getters y Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AlumnoModel getAlumno() { return alumno; }
    public void setAlumno(AlumnoModel alumno) { this.alumno = alumno; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getLectorUsado() { return lectorUsado; }
    public void setLectorUsado(String lectorUsado) { this.lectorUsado = lectorUsado; }
}