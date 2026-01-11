package com.example.fingerprint_api.models.Asistencia;

import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_entrada_alumno")
public class RegistroAsistenciaModel {

    // Cambiamos el ID para que coincida con tu PK de ID Construida
    @Id
    @Column(name = "id_registro_entrada", length = 50)
    private String idRegistroEntrada;

    // Relación con el alumno
    @ManyToOne
    @JoinColumn(name = "id_alumno", nullable = false)
    private AlumnoModel alumno;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "id_entrada")
    private Integer idEntrada;

    // Eliminamos la columna lector_usado de aquí

    public RegistroAsistenciaModel() {}

    // Constructor actualizado
    public RegistroAsistenciaModel(String idRegistroEntrada, AlumnoModel alumno, LocalDateTime fechaHora, Integer idEntrada) {
        this.idRegistroEntrada = idRegistroEntrada;
        this.alumno = alumno;
        this.fechaHora = fechaHora;
        this.idEntrada = idEntrada;
    }

    // --- Getters y Setters ---

    public String getIdRegistroEntrada() { return idRegistroEntrada; }
    public void setIdRegistroEntrada(String idRegistroEntrada) { this.idRegistroEntrada = idRegistroEntrada; }

    public AlumnoModel getAlumno() { return alumno; }
    public void setAlumno(AlumnoModel alumno) { this.alumno = alumno; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Integer getIdEntrada() { return idEntrada; }
    public void setIdEntrada(Integer idEntrada) { this.idEntrada = idEntrada; }
}