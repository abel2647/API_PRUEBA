package com.example.fingerprint_api.models.Visitante;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "codigo_temporal")
public class CodigoTemporalModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_codigo")
    private Integer id_codigo;

    @Column(name = "uuid", nullable = false, length = 50)
    private String uuid; // El código del QR

    @Column(name = "asunto")
    private String asunto;

    @Column(name = "num_acompanantes")
    private Integer numeroAcompañantes;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    // Relación con Visitante (Muchas visitas pueden ser de 1 persona)
    @ManyToOne
    @JoinColumn(name = "visitante_id", nullable = false)
    private VisitanteModel visitante;

    // --- Getters y Setters ---
    public Integer getId_codigo() { return id_codigo; }
    public void setId_codigo(Integer id_codigo) { this.id_codigo = id_codigo; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public Integer getNumeroAcompañantes() { return numeroAcompañantes; }
    public void setNumeroAcompañantes(Integer numeroAcompañantes) { this.numeroAcompañantes = numeroAcompañantes; }

    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    public VisitanteModel getVisitante() { return visitante; }
    public void setVisitante(VisitanteModel visitante) { this.visitante = visitante; }
}