/*package com.example.fingerprint_api.controllers;

import com.example.fingerprint_api.dtos.PaqueteSincronizacion;
import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import com.example.fingerprint_api.models.Visitante.VisitanteModel;
import com.example.fingerprint_api.models.Visitante.CodigoTemporalModel;
import com.example.fingerprint_api.models.Asistencia.RegistroAsistenciaModel;
import com.example.fingerprint_api.models.Visitante.RegistroEntradaVisitanteModel;
import com.example.fingerprint_api.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/replicacion")
public class ReplicacionController {

    @Autowired private AlumnoRepository alumnoRepo;
    @Autowired private VisitanteRepository visitanteRepo;
    @Autowired private CodigoTemporalRepository codigoRepo;
    @Autowired private RegistroAsistenciaRepository regAlumnoRepo;
    @Autowired private RegistroEntradaVisitanteRepository regVisitanteRepo;

    @PostMapping("/full-sync")
    @Transactional
    public PaqueteSincronizacion sincronizarTodo(@RequestBody PaqueteSincronizacion datosEntrantes) {

        // =========================================================================
        // 1. ALUMNOS (Prioridad Alta - Maestro)
        // =========================================================================
        if (datosEntrantes.getAlumnos() != null) {
            for (AlumnoModel externo : datosEntrantes.getAlumnos()) {
                // Buscamos por MATRÍCULA para evitar duplicados lógicos
                Optional<AlumnoModel> local = alumnoRepo.findByNumeroControl(externo.getNumeroControl());

                if (local.isPresent()) {
                    // UPDATE: Actualizamos todos los campos del registro existente
                    AlumnoModel existente = local.get();
                    existente.setPrimerNombre(externo.getPrimerNombre());
                    existente.setSegundoNombre(externo.getSegundoNombre());
                    existente.setApellidoPaterno(externo.getApellidoPaterno());
                    existente.setApellidoMaterno(externo.getApellidoMaterno());
                    existente.setHuellaFmd(externo.getHuellaFmd());
                    existente.setCarreraClave(externo.getCarreraClave());
                    existente.setActivo(externo.getActivo());
                    existente.setCodigoQr(externo.getCodigoQr());
                    existente.setCodigoBarra(externo.getCodigoBarra());
                    existente.setNumTelefono(externo.getNumTelefono());
                    existente.setUuid(externo.getUuid());
                    existente.setDeleted(externo.getDeleted());
                    existente.setUsuario(externo.getUsuario());
                    existente.setUpdateAt(externo.getUpdateAt());

                    alumnoRepo.save(existente);
                } else {
                    // INSERT: Es nuevo.
                    // Si tu base de datos genera IDs numéricos (1,2,3), dejamos esto en null.
                    //externo.setId(null);
                    alumnoRepo.save(externo);
                }
            }
        }

        // =========================================================================
        // 2. VISITANTES (Prioridad Alta - Maestro) - CORREGIDO
        // =========================================================================
        if (datosEntrantes.getVisitantes() != null) {
            for (VisitanteModel externo : datosEntrantes.getVisitantes()) {

                // IMPORTANTE: Buscamos por el ID (UUID) exacto que viene del cliente
                Optional<VisitanteModel> local = visitanteRepo.findById(externo.getId_visitante());

                if (local.isPresent()) {
                    // UPDATE: Si ya existe, actualizamos sus datos
                    VisitanteModel existente = local.get();
                    existente.setPrimerNombre(externo.getPrimerNombre());
                    existente.setApellidoPaterno(externo.getApellidoPaterno());
                    existente.setApellidoMaterno(externo.getApellidoMaterno());
                    // Agrega aquí otros campos si tu modelo los tiene (ej. fecha, empresa, etc.)

                    visitanteRepo.save(existente);
                } else {
                    // INSERT: No existe en el servidor.
                    // ¡NO PONEMOS EL ID EN NULL! Guardamos tal cual para mantener el enlace con los hijos.
                    visitanteRepo.save(externo);
                }
            }
        }

        // =========================================================================
        // 3. CÓDIGOS TEMPORALES (Hijo de Visitante)
        // =========================================================================
        // Este bloque DEBE ir después de Visitantes para evitar el error de Foreign Key
        if (datosEntrantes.getCodigos() != null) {
            for (CodigoTemporalModel c : datosEntrantes.getCodigos()) {
                // Verificamos si ya existe este código para no duplicar
                // Asumimos que 'c.getId()' devuelve el ID principal del código
                Optional<CodigoTemporalModel> local = codigoRepo.findById(c.getId_codigo());

                if (!local.isPresent()) {
                    // Si el ID es autoincrementable en BD, lo ponemos null.
                    // Si es UUID generado en cliente, comenta la línea de abajo.
                    //c.setId_codigo(null);

                    // Al guardar, Hibernate buscará al Visitante por su ID.
                    // Como ya guardamos al visitante en el paso 2, esto no fallará.
                    codigoRepo.save(c);
                }
            }
        }

        // =========================================================================
        // 4. REGISTROS DE ASISTENCIA (Hijos/Logs)
        // =========================================================================
        if (datosEntrantes.getRegistrosAlumnos() != null) {
            for (RegistroAsistenciaModel r : datosEntrantes.getRegistrosAlumnos()) {
                // Guardamos directamente. Si necesitas evitar duplicados, usa un findById antes.
                // r.setId(null); // Descomentar si la BD genera el ID del log
                regAlumnoRepo.save(r);
            }
        }

        if (datosEntrantes.getRegistrosVisitantes() != null) {
            for (RegistroEntradaVisitanteModel r : datosEntrantes.getRegistrosVisitantes()) {
                // Guardamos directamente.
                // r.setId(null); // Descomentar si la BD genera el ID del log
                regVisitanteRepo.save(r);
            }
        }

        // =========================================================================
        // 5. RESPUESTA (Devolver estado actual del servidor)
        // =========================================================================
        PaqueteSincronizacion respuesta = new PaqueteSincronizacion();

        // Convertimos a List explícitamente por si findAll devuelve Iterable
        respuesta.setAlumnos(alumnoRepo.findAll());
        respuesta.setVisitantes((List<VisitanteModel>) visitanteRepo.findAll());
        respuesta.setCodigos(codigoRepo.findAll());
        respuesta.setRegistrosAlumnos(regAlumnoRepo.findAll());
        respuesta.setRegistrosVisitantes(regVisitanteRepo.findAll());

        return respuesta;
    }
}*/