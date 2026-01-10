package com.example.fingerprint_api.models.Visitante;

import com.example.fingerprint_api.models.Visitante.CodigoTemporalModel;
import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "registro_entrada_visitante")
public class RegistroEntradaVisitanteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relación con el pase (UUID)
    @ManyToOne
    @JoinColumn(name = "id_codigo", nullable = false)
    private CodigoTemporalModel codigoTemporal;

    @Column(nullable = false)
    private Integer entrada; // Ejemplo: "P1E1"

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    public RegistroEntradaVisitanteModel() {}

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public CodigoTemporalModel getCodigoTemporal() { return codigoTemporal; }
    public void setCodigoTemporal(CodigoTemporalModel codigoTemporal) { this.codigoTemporal = codigoTemporal; }

    public Integer getEntrada() {
        return entrada;
    }

    public void setEntrada(Integer entrada) {
        this.entrada = entrada;
    }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}