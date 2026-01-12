package com.example.fingerprint_api.services;

import com.digitalpersona.uareu.*;
import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import com.example.fingerprint_api.repositories.AlumnoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
public class AlumnoService {
    @Autowired
    AlumnoRepository alumnoRepository;

    // Estructura simple que tenías

    //Obtener alumnos
    public ArrayList<AlumnoModel> obtenerAlumnos(){
        return (ArrayList<AlumnoModel>) alumnoRepository.findAll();
    }

    //Crear alumno
    /*public AlumnoModel registrarAlumno(AlumnoModel alumno){

        // Establecer campos automáticos
        alumno.setCreatedAt(LocalDateTime.now());
        alumno.setUpdateAt(LocalDateTime.now());
        alumno.setUuid(UUID.randomUUID().toString());
        alumno.setVersion(1);
        alumno.setDeleted(0); // Por defecto no eliminado

        // Si activo no viene, establecer por defecto
        if(alumno.getActivo() == null) {
            alumno.setActivo("1");
        }

        return alumnoRepository.save(alumno);
    }*/
    //Crear alumno
    //Crear alumno
    public AlumnoModel registrarAlumno(AlumnoModel alumno){
        try {
            System.out.println("=== INICIANDO REGISTRO ===");
            System.out.println("Alumno object: " + alumno);
            System.out.println("Tipo de objeto: " + alumno.getClass().getName());

            // DEBUG detallado de cada campo
            System.out.println("numeroControl: " + alumno.getNumeroControl());
            System.out.println("primerNombre: " + alumno.getPrimerNombre());
            System.out.println("segundoNombre: " + alumno.getSegundoNombre());
            System.out.println("apellidoPaterno: " + alumno.getApellidoPaterno());
            System.out.println("apellidoMaterno: " + alumno.getApellidoMaterno());
            System.out.println("carreraClave: " + alumno.getCarreraClave());
            System.out.println("numTelefono: " + alumno.getNumTelefono());
            System.out.println("codigoBarra: " + alumno.getCodigoBarra());
            System.out.println("codigoQr: " + alumno.getCodigoQr());
            System.out.println("activo: " + alumno.getActivo());
            System.out.println("usuario: " + alumno.getUsuario());

            // Establecer campos automáticos
            System.out.println("=== ESTABLECIENDO CAMPOS AUTOMÁTICOS ===");
            alumno.setCreatedAt(LocalDateTime.now());
            alumno.setUpdateAt(LocalDateTime.now());
            alumno.setUuid(UUID.randomUUID().toString());
            alumno.setVersion(1);
            alumno.setDeleted(0);

            if(alumno.getActivo() == null) {
                alumno.setActivo(Integer.valueOf("1"));
            }

            System.out.println("=== ANTES DE SAVE ===");
            AlumnoModel alumnoGuardado = alumnoRepository.save(alumno);
            System.out.println("=== DESPUÉS DE SAVE ===");

            System.out.println("Alumno guardado exitosamente:");
            System.out.println("ID: " + alumnoGuardado.getId_alumno());
            System.out.println("UUID: " + alumnoGuardado.getUuid());

            return alumnoGuardado;

        } catch (Exception e) {
            System.out.println("=== ERROR EN REGISTRAR ALUMNO ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lanzar la excepción para ver el error completo
        }
    }

    //eliminar por Id
    public boolean eliminarAlumno(Integer id) {
        try{
            alumnoRepository.deleteById(id);
            return true;
        }catch(Exception err){
            return false;
        }
    }

    // Eliminar alumno por número de control
    public boolean eliminarAlumnoPorNumeroControl(String numeroControl) {
        try{
            Optional<AlumnoModel> alumnoOptional = alumnoRepository.findByNumeroControl(numeroControl);
            if(alumnoOptional.isPresent()){
                alumnoRepository.delete(alumnoOptional.get());
                return true;
            }
            return false;
        }catch(Exception err){
            return false;
        }
    }

    // Funciones adicionales con la misma estructura simple



    public Optional<AlumnoModel> obtenerAlumnoPorNumeroControl(String numeroControl){
        return alumnoRepository.findByNumeroControl(numeroControl);
    }

    public Optional<AlumnoModel> obtenerAlumnoPorUuid(String uuid){
        return alumnoRepository.findByUuid(uuid);
    }

    // Actualizar alumno por ID con incremento de versión (ACTUALIZADO)
    public AlumnoModel actualizarAlumno(Integer id, AlumnoModel alumnoDetalles){
        Optional<AlumnoModel> alumnoOptional = alumnoRepository.findById(id);
        if(alumnoOptional.isPresent()){
            AlumnoModel alumno = alumnoOptional.get();

            // Actualizar campos
            if(alumnoDetalles.getPrimerNombre() != null){
                alumno.setPrimerNombre(alumnoDetalles.getPrimerNombre());
            }
            if(alumnoDetalles.getSegundoNombre() != null){
                alumno.setSegundoNombre(alumnoDetalles.getSegundoNombre());
            }
            if(alumnoDetalles.getApellidoPaterno() != null){
                alumno.setApellidoPaterno(alumnoDetalles.getApellidoPaterno());
            }
            if(alumnoDetalles.getApellidoMaterno() != null){
                alumno.setApellidoMaterno(alumnoDetalles.getApellidoMaterno());
            }
            if(alumnoDetalles.getCarreraClave() != null){
                alumno.setCarreraClave(alumnoDetalles.getCarreraClave());
            }
            if(alumnoDetalles.getNumTelefono() != null){
                alumno.setNumTelefono(alumnoDetalles.getNumTelefono());
            }
            if(alumnoDetalles.getActivo() != null){
                alumno.setActivo(alumnoDetalles.getActivo());
            }
            if(alumnoDetalles.getCodigoBarra() != null){
                alumno.setCodigoBarra(alumnoDetalles.getCodigoBarra());
            }
            if(alumnoDetalles.getCodigoQr() != null){
                alumno.setCodigoQr(alumnoDetalles.getCodigoQr());
            }
            if(alumnoDetalles.getUsuario() != null){
                alumno.setUsuario(alumnoDetalles.getUsuario());
            }

            // Campos automáticos en actualización
            alumno.setUpdateAt(LocalDateTime.now());
            alumno.setVersion(alumno.getVersion() + 1); // Incrementar versión

            return alumnoRepository.save(alumno);
        }
        return null;
    }


/*
    // Actualizar alumno por nombre (NUEVO)

    public AlumnoModel actualizarAlumnoPorNombre(String nombre, AlumnoModel alumnoDetalles){
        List<AlumnoModel> alumnos = alumnoRepository.findByPrimerNombreContainingIgnoreCase(nombre);
        if(!alumnos.isEmpty()){
            // Tomar el primer alumno que coincida (podrías modificar para buscar exacto)
            AlumnoModel alumno = alumnos.get(0);

            // Actualizar campos
            if(alumnoDetalles.getPrimerNombre() != null){
                alumno.setPrimerNombre(alumnoDetalles.getPrimerNombre());
            }
            if(alumnoDetalles.getSegundoNombre() != null){
                alumno.setSegundoNombre(alumnoDetalles.getSegundoNombre());
            }
            if(alumnoDetalles.getApellidoPaterno() != null){
                alumno.setApellidoPaterno(alumnoDetalles.getApellidoPaterno());
            }
            if(alumnoDetalles.getApellidoMaterno() != null){
                alumno.setApellidoMaterno(alumnoDetalles.getApellidoMaterno());
            }
            if(alumnoDetalles.getCarreraClave() != null){
                alumno.setCarreraClave(alumnoDetalles.getCarreraClave());
            }
            if(alumnoDetalles.getNumTelefono() != null){
                alumno.setNumTelefono(alumnoDetalles.getNumTelefono());
            }
            if(alumnoDetalles.getActivo() != null){
                alumno.setActivo(alumnoDetalles.getActivo());
            }
            if(alumnoDetalles.getCodigoBarra() != null){
                alumno.setCodigoBarra(alumnoDetalles.getCodigoBarra());
            }
            if(alumnoDetalles.getCodigoQr() != null){
                alumno.setCodigoQr(alumnoDetalles.getCodigoQr());
            }
            if(alumnoDetalles.getUsuario() != null){
                alumno.setUsuario(alumnoDetalles.getUsuario());
            }

            // Campos automáticos en actualización
            alumno.setUpdateAt(LocalDateTime.now());
            alumno.setVersion(alumno.getVersion() + 1); // Incrementar versión

            return alumnoRepository.save(alumno);
        }
        return null;
    }
*/

    // Eliminar alumno suave (soft delete)
    public boolean eliminarAlumnoSuave(Integer id){
        try{
            Optional<AlumnoModel> alumnoOptional = alumnoRepository.findById(id);
            if(alumnoOptional.isPresent()){
                AlumnoModel alumno = alumnoOptional.get();
                alumno.setDeleted(1);
                alumno.setUpdateAt(LocalDateTime.now()); // Actualizar fecha de modificación
                alumnoRepository.save(alumno);
                return true;
            }
            return false;
        }catch(Exception err){
            return false;
        }
    }

    public boolean existeAlumno(Integer id){
        return alumnoRepository.existsById(id);
    }

    public ArrayList<AlumnoModel> obtenerAlumnosActivos(){
        return (ArrayList<AlumnoModel>) alumnoRepository.findByActivo(Integer.valueOf("1"));
    }

    public ArrayList<AlumnoModel> obtenerAlumnosEliminados(){
        return (ArrayList<AlumnoModel>) alumnoRepository.findByDeleted(1);
    }

    public boolean restaurarAlumno(Integer id){
        try{
            Optional<AlumnoModel> alumnoOptional = alumnoRepository.findById(id);
            if(alumnoOptional.isPresent()){
                AlumnoModel alumno = alumnoOptional.get();
                alumno.setDeleted(0);
                alumno.setUpdateAt(LocalDateTime.now()); // Actualizar fecha de modificación
                alumnoRepository.save(alumno);
                return true;
            }
            return false;
        }catch(Exception err){
            return false;
        }
    }

    // Obtener alumnos por carrera (NUEVO)
    public ArrayList<AlumnoModel> obtenerAlumnosPorCarrera(String carreraClave){
        return (ArrayList<AlumnoModel>) alumnoRepository.findByCarreraClave(carreraClave);
    }


    public ArrayList<AlumnoModel> buscarAlumnosPorNombre(String nombre){
        return (ArrayList<AlumnoModel>) alumnoRepository.findByPrimerNombreContainingIgnoreCase(nombre);
    }

    public long contarTotalAlumnos(){
        return alumnoRepository.count();
    }

    public boolean existeNumeroControl(String numeroControl){
        return alumnoRepository.existsByNumeroControl(numeroControl);
    }

    //Seguridad con UUID

    /* public Optional<AlumnoModel> obtenerAlumnoPorId(Integer id){
        return alumnoRepository.findById(id);
    }*/
    // Obtener alumno por ID y UUID (para seguridad)
    public Optional<AlumnoModel> obtenerAlumnoPorIdYUuid(Integer id, String uuid) {
        Optional<AlumnoModel> alumno = alumnoRepository.findById(id);
        if (alumno.isPresent() && alumno.get().getUuid().equals(uuid)) {
            return alumno;
        }
        return Optional.empty();
    }


    // Eliminar alumno por ID y UUID (para seguridad)
    public boolean eliminarAlumnoSeguro(Integer id, String uuid) {
        try{
            Optional<AlumnoModel> alumnoOptional = alumnoRepository.findById(id);
            if(alumnoOptional.isPresent() && alumnoOptional.get().getUuid().equals(uuid)){
                alumnoRepository.deleteById(id);
                return true;
            }
            return false;
        }catch(Exception err){
            return false;
        }
    }

    // Eliminación suave por ID y UUID
    public boolean eliminarAlumnoSuaveSeguro(Integer id, String uuid) {
        try{
            Optional<AlumnoModel> alumnoOptional = alumnoRepository.findById(id);
            if(alumnoOptional.isPresent() && alumnoOptional.get().getUuid().equals(uuid)){
                AlumnoModel alumno = alumnoOptional.get();
                alumno.setDeleted(1);
                alumno.setUpdateAt(LocalDateTime.now());
                alumnoRepository.save(alumno);
                return true;
            }
            return false;
        }catch(Exception err){
            return false;
        }
    }

    // Restaurar alumno por ID y UUID
    public boolean restaurarAlumnoSeguro(Integer id, String uuid) {
        try{
            Optional<AlumnoModel> alumnoOptional = alumnoRepository.findById(id);
            if(alumnoOptional.isPresent() && alumnoOptional.get().getUuid().equals(uuid)){
                AlumnoModel alumno = alumnoOptional.get();
                alumno.setDeleted(0);
                alumno.setUpdateAt(LocalDateTime.now());
                alumnoRepository.save(alumno);
                return true;
            }
            return false;
        }catch(Exception err){
            return false;
        }
    }

    // Actualizar alumno por ID y UUID (para seguridad)
    public AlumnoModel actualizarAlumnoSeguro(Integer id, String uuid, AlumnoModel alumnoDetalles) {
        Optional<AlumnoModel> alumnoOptional = alumnoRepository.findById(id);
        if (alumnoOptional.isPresent() && alumnoOptional.get().getUuid().equals(uuid)) {
            AlumnoModel alumno = alumnoOptional.get();

            // Actualizar campos
            if(alumnoDetalles.getPrimerNombre() != null){
                alumno.setPrimerNombre(alumnoDetalles.getPrimerNombre());
            }
            if(alumnoDetalles.getSegundoNombre() != null){
                alumno.setSegundoNombre(alumnoDetalles.getSegundoNombre());
            }
            if(alumnoDetalles.getApellidoPaterno() != null){
                alumno.setApellidoPaterno(alumnoDetalles.getApellidoPaterno());
            }
            if(alumnoDetalles.getApellidoMaterno() != null){
                alumno.setApellidoMaterno(alumnoDetalles.getApellidoMaterno());
            }
            if(alumnoDetalles.getCarreraClave() != null){
                alumno.setCarreraClave(alumnoDetalles.getCarreraClave());
            }
            if(alumnoDetalles.getNumTelefono() != null){
                alumno.setNumTelefono(alumnoDetalles.getNumTelefono());
            }
            if(alumnoDetalles.getActivo() != null){
                alumno.setActivo(alumnoDetalles.getActivo());
            }
            if(alumnoDetalles.getCodigoBarra() != null){
                alumno.setCodigoBarra(alumnoDetalles.getCodigoBarra());
            }
            if(alumnoDetalles.getCodigoQr() != null){
                alumno.setCodigoQr(alumnoDetalles.getCodigoQr());
            }
            if(alumnoDetalles.getUsuario() != null){
                alumno.setUsuario(alumnoDetalles.getUsuario());
            }

            // Campos automáticos
            alumno.setUpdateAt(LocalDateTime.now());
            alumno.setVersion(alumno.getVersion() + 1);

            return alumnoRepository.save(alumno);
        }
        return null;
    }

    /**
     * 1. REGISTRAR O EDITAR HUELLA
     * Recibe el ID del alumno y los bytes de la huella (FMD).
     */
    public boolean guardarHuellaAlumno(Integer idAlumno, byte[] fmdBytes) {
        Optional<AlumnoModel> alumnoOpt = alumnoRepository.findById(idAlumno);
        if (alumnoOpt.isPresent()) {
            AlumnoModel alumno = alumnoOpt.get();
            alumno.setHuellaFmd(fmdBytes); // Guardamos la huella
            alumnoRepository.save(alumno);
            return true;
        }
        return false;
    }

    /**
     * 2. BUSCAR ALUMNO POR HUELLA (Identificación 1:N)
     * Recorre los alumnos y compara sus huellas con la capturada.
     */
    public Optional<AlumnoModel> identificarAlumno(Fmd huellaCapturada) {
        try {
            // Traemos todos los alumnos (o solo los activos para optimizar)
            ArrayList<AlumnoModel> alumnos = (ArrayList<AlumnoModel>) alumnoRepository.findAll();

            Engine engine = UareUGlobal.GetEngine(); // Motor de comparación del SDK

            for (AlumnoModel alumno : alumnos) {
                // Si el alumno no tiene huella registrada, saltamos
                if (alumno.getHuellaFmd() == null) continue;

                // Importamos la huella guardada en BD a formato Fmd
                Fmd huellaGuardada = UareUGlobal.GetImporter().ImportFmd(
                        alumno.getHuellaFmd(),
                        Fmd.Format.ANSI_378_2004,
                        Fmd.Format.ANSI_378_2004
                );

                // Comparamos: (HuellaCapturada vs HuellaGuardada)
                // falsematch_rate: probabilidad de que sea un falso positivo.
                // Cuanto más bajo sea el número, más exacta es la coincidencia.
                int falsematch_rate = engine.Compare(huellaCapturada, 0, huellaGuardada, 0);

                // El umbral estándar es 2147 (aprox 0.001% de error).
                // Si es menor, ¡ES LA PERSONA!
                if (falsematch_rate < 2147) {
                    return Optional.of(alumno);
                }
            }
        } catch (UareUException e) {
            e.printStackTrace();
        }

        return Optional.empty(); // No se encontró coincidencia
    }



}