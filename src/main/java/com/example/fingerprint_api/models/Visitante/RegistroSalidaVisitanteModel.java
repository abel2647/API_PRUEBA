package com.example.fingerprint_api.models.Visitante;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_salida_visitante")
public class RegistroSalidaVisitanteModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_salida;

    private LocalDateTime fechaHora;
    private int puerta; // Puerta por la que salió

    @ManyToOne
    @JoinColumn(name = "codigo_temporal_id")
    @JsonBackReference
    private CodigoTemporalModel codigoTemporal;

    // Getters y Setters
    public Integer getId_salida() { return id_salida; }
    public void setId_salida(Integer id_salida) { this.id_salida = id_salida; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public int getPuerta() { return puerta; }
    public void setPuerta(int puerta) { this.puerta = puerta; }
    public CodigoTemporalModel getCodigoTemporal() { return codigoTemporal; }
    public void setCodigoTemporal(CodigoTemporalModel codigoTemporal) { this.codigoTemporal = codigoTemporal; }
}