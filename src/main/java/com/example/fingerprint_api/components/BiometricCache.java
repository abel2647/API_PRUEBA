package com.example.fingerprint_api.components;

import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.UareUGlobal;
import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import com.example.fingerprint_api.repositories.AlumnoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

/**
 * Mantiene una copia de las huellas en memoria RAM para búsqueda ultra-rápida.
 * Singleton: Solo existe una instancia en toda la aplicación.
 */
@Component
public class BiometricCache {

    // Mapa thread-safe: ID Alumno -> Objeto Huella (FMD) del SDK
    private final ConcurrentHashMap<Integer, Fmd> cacheHuellas = new ConcurrentHashMap<>();

    @Autowired
    private AlumnoRepository alumnoRepository;

    /**
     * Se ejecuta automáticamente al arrancar Spring Boot.
     * Carga todas las huellas de la BD a la RAM.
     */
    @PostConstruct
    public void inicializarCache() {
        System.out.println(">>> CARGANDO HUELLAS EN MEMORIA RAM...");
        List<AlumnoModel> alumnos = (List<AlumnoModel>) alumnoRepository.findAll();

        int cont = 0;
        for (AlumnoModel alumno : alumnos) {
            if (alumno.getHuellaFmd() != null && alumno.getHuellaFmd().length > 0) {
                agregarAlCache(alumno.getId_alumno(), alumno.getHuellaFmd());
                cont++;
            }
        }
        System.out.println(">>> CACHÉ INICIALIZADO: " + cont + " huellas listas para validar.");
    }

    /**
     * Convierte los bytes de BD a formato FMD del SDK y lo guarda en el mapa.
     */
    public void agregarAlCache(Integer idAlumno, byte[] huellaBytes) {
        try {
            Fmd fmd = UareUGlobal.GetImporter().ImportFmd(
                    huellaBytes,
                    Fmd.Format.ANSI_378_2004,
                    Fmd.Format.ANSI_378_2004
            );
            cacheHuellas.put(idAlumno, fmd);
        } catch (Exception e) {
            System.err.println("Error importando huella para ID " + idAlumno + ": " + e.getMessage());
        }
    }

    /**
     * Elimina una huella de la RAM (ej. si borras al alumno).
     */
    public void removerDelCache(Integer idAlumno) {
        cacheHuellas.remove(idAlumno);
    }

    public ConcurrentHashMap<Integer, Fmd> getMapa() {
        return cacheHuellas;
    }
}