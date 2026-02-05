package com.example.fingerprint_api.dtos;

import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import com.example.fingerprint_api.models.Visitante.CodigoTemporalModel;
import com.example.fingerprint_api.models.Visitante.RegistroEntradaVisitanteModel;
import com.example.fingerprint_api.models.Visitante.VisitanteModel;
import com.example.fingerprint_api.models.Asistencia.RegistroAsistenciaModel;
//import org.hibernate.mapping.List;
import java.util.List;


// No es una @Entity, es solo una clase normal (DTO)
public class PaqueteSincronizacion {
    // Maestros (Padres)
    private List<AlumnoModel> alumnos;
    private List<VisitanteModel> visitantes;

    // Transaccionales (Hijos)
    private List<CodigoTemporalModel> codigos;
    private List<RegistroAsistenciaModel> registrosAlumnos;
    private List<RegistroEntradaVisitanteModel> registrosVisitantes;

    // Constructor, Getters y Setters para todo


    public PaqueteSincronizacion() {
    }

    public PaqueteSincronizacion(List<AlumnoModel> alumnos, List<CodigoTemporalModel> codigos, List<RegistroAsistenciaModel> registrosAlumnos, List<RegistroEntradaVisitanteModel> registrosVisitantes, List<VisitanteModel> visitantes) {
        this.alumnos = alumnos;
        this.codigos = codigos;
        this.registrosAlumnos = registrosAlumnos;
        this.registrosVisitantes = registrosVisitantes;
        this.visitantes = visitantes;
    }

    public List<AlumnoModel> getAlumnos() {
        return alumnos;
    }

    public void setAlumnos(List<AlumnoModel> alumnos) {
        this.alumnos = alumnos;
    }

    public List<CodigoTemporalModel> getCodigos() {
        return codigos;
    }

    public void setCodigos(List<CodigoTemporalModel> codigos) {
        this.codigos = codigos;
    }

    public List<RegistroAsistenciaModel> getRegistrosAlumnos() {
        return registrosAlumnos;
    }

    public void setRegistrosAlumnos(List<RegistroAsistenciaModel> registrosAlumnos) {
        this.registrosAlumnos = registrosAlumnos;
    }

    public List<RegistroEntradaVisitanteModel> getRegistrosVisitantes() {
        return registrosVisitantes;
    }

    public void setRegistrosVisitantes(List<RegistroEntradaVisitanteModel> registrosVisitantes) {
        this.registrosVisitantes = registrosVisitantes;
    }

    public List<VisitanteModel> getVisitantes() {
        return visitantes;
    }

    public void setVisitantes(List<VisitanteModel> visitantes) {
        this.visitantes = visitantes;
    }
}
