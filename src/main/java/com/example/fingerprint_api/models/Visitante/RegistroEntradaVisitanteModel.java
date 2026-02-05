package com.example.fingerprint_api.models.Visitante;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_entrada_visitante")
public class RegistroEntradaVisitanteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Asegúrate que en tu BD sea 'id' (si falla, prueba 'id_registro')
    private Integer id;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "entrada")
    private int entrada;

    // --- AQUÍ ESTABA EL ERROR ---
    @ManyToOne
    // Cambiamos "codigo_temporal_id" por "id_codigo" para que coincida con tu Base de Datos
    @JoinColumn(name = "id_codigo")
    @JsonBackReference
    private CodigoTemporalModel codigoTemporal;

    // --- GETTERS Y SETTERS ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public CodigoTemporalModel getCodigoTemporal() { return codigoTemporal; }
    public void setCodigoTemporal(CodigoTemporalModel codigoTemporal) { this.codigoTemporal = codigoTemporal; }

    // --- COMPATIBILIDAD ---

    public int getEntrada() { return entrada; }
    public void setEntrada(int entrada) { this.entrada = entrada; }

    public int getPuerta() { return entrada; }
    public void setPuerta(int puerta) { this.entrada = puerta; }
}