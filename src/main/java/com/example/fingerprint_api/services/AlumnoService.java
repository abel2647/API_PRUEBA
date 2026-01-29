package com.example.fingerprint_api.services;

import com.digitalpersona.uareu.*;
import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import com.example.fingerprint_api.repositories.AlumnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AlumnoService {

    @Autowired
    private AlumnoRepository alumnoRepository;

    // ==========================================
    //          OBTENER LISTADOS
    // ==========================================

    public ArrayList<AlumnoModel> obtenerAlumnos() {
        return (ArrayList<AlumnoModel>) alumnoRepository.findAll();
    }

    public ArrayList<AlumnoModel> obtenerAlumnosActivos() {
        return (ArrayList<AlumnoModel>) alumnoRepository.findByActivo(1);
    }

    public ArrayList<AlumnoModel> obtenerAlumnosEliminados() {
        return (ArrayList<AlumnoModel>) alumnoRepository.findByDeleted(1);
    }

    public ArrayList<AlumnoModel> obtenerAlumnosPorCarrera(String carreraClave) {
        return (ArrayList<AlumnoModel>) alumnoRepository.findByCarreraClave(carreraClave);
    }

    public boolean existeAlumno(Integer id) {
        return alumnoRepository.existsById(id);
    }

    // ==========================================
    //          REGISTRO (CON DEBUG LOGS)
    // ==========================================

    @Transactional
    public AlumnoModel registrarAlumno(AlumnoModel alumno) {
        try {
            System.out.println("=== [DEBUG] INICIANDO REGISTRO DE ALUMNO ===");

            // Verificación de campos críticos para evitar el NULL en la base de datos
            System.out.println("Numero Control recibido: " + alumno.getNumeroControl());
            System.out.println("Nombre: " + alumno.getPrimerNombre() + " " + alumno.getApellidoPaterno());

            // Establecer campos automáticos
            alumno.setCreatedAt(LocalDateTime.now());
            alumno.setUpdateAt(LocalDateTime.now());
            alumno.setUuid(UUID.randomUUID().toString());
            alumno.setVersion(1);
            alumno.setDeleted(0);

            if (alumno.getActivo() == null) {
                alumno.setActivo(1);
            }

            System.out.println("=== [DEBUG] GUARDANDO EN REPOSITORIO ===");
            AlumnoModel alumnoGuardado = alumnoRepository.save(alumno);

            System.out.println("=== [DEBUG] REGISTRO EXITOSO. ID GENERADO: " + alumnoGuardado.getId_alumno());
            return alumnoGuardado;

        } catch (Exception e) {
            System.err.println("=== [ERROR] FALLÓ EL REGISTRO: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // ==========================================
    //          ACTUALIZACIÓN SEGURA (UUID)
    // ==========================================

    @Transactional
    public AlumnoModel actualizarAlumnoSeguro(Integer id, String uuid, AlumnoModel alumnoDetalles) {
        Optional<AlumnoModel> alumnoOptional = alumnoRepository.findById(id);
        if (alumnoOptional.isPresent() && alumnoOptional.get().getUuid().equals(uuid)) {
            AlumnoModel alumno = alumnoOptional.get();

            // IMPORTANTE: Si el frontend manda los campos, los actualizamos
            if(alumnoDetalles.getPrimerNombre() != null) alumno.setPrimerNombre(alumnoDetalles.getPrimerNombre());
            if(alumnoDetalles.getApellidoPaterno() != null) alumno.setApellidoPaterno(alumnoDetalles.getApellidoPaterno());
            if(alumnoDetalles.getApellidoMaterno() != null) alumno.setApellidoMaterno(alumnoDetalles.getApellidoMaterno());
            if(alumnoDetalles.getNumeroControl() != null) alumno.setNumeroControl(alumnoDetalles.getNumeroControl());
            if(alumnoDetalles.getCarreraClave() != null) alumno.setCarreraClave(alumnoDetalles.getCarreraClave());

            alumno.setUpdateAt(LocalDateTime.now());
            return alumnoRepository.save(alumno); // Aquí es donde se va a la base de datos
        }
        return null;
    }

    // EN: src/main/java/com/example/fingerprint_api/services/AlumnoService.java

    // ... imports

    // BUSCA ESTE MÉTODO Y REEMPLÁZALO (actualizarAlumnoDesdeMapa)
    @Transactional
    public AlumnoModel actualizarAlumnoDesdeMapa(Integer id, String uuid, Map<String, Object> datos) {
        Optional<AlumnoModel> alumnoOpt = alumnoRepository.findById(id);

        // CORRECCIÓN: Verificamos que el alumno exista.
        if (alumnoOpt.isPresent()) {
            AlumnoModel alumno = alumnoOpt.get();

            // CORRECCIÓN CRÍTICA:
            // Si el alumno en BD tiene UUID nulo (insertado por SQL), permitimos editar para corregirlo
            // O si tiene UUID, verificamos que coincida con el que mandamos.
            boolean uuidValido = (alumno.getUuid() == null) || alumno.getUuid().equals(uuid);

            if (uuidValido) {
                // Si el UUID era nulo, aprovechamos para asignarle uno nuevo y arreglar el registro
                if (alumno.getUuid() == null) {
                    alumno.setUuid(UUID.randomUUID().toString());
                }

                if (datos.containsKey("primerNombre")) alumno.setPrimerNombre((String) datos.get("primerNombre"));
                if (datos.containsKey("apellidoPaterno")) alumno.setApellidoPaterno((String) datos.get("apellidoPaterno"));
                if (datos.containsKey("apellidoMaterno")) alumno.setApellidoMaterno((String) datos.get("apellidoMaterno"));
                if (datos.containsKey("numeroControl")) alumno.setNumeroControl((String) datos.get("numeroControl"));
                if (datos.containsKey("carreraClave")) alumno.setCarreraClave((String) datos.get("carreraClave"));

                if (datos.containsKey("activo")) {
                    Object activoVal = datos.get("activo");
                    if (activoVal instanceof Integer) {
                        alumno.setActivo((Integer) activoVal);
                    } else if (activoVal instanceof String) {
                        try {
                            alumno.setActivo(Integer.parseInt((String) activoVal));
                        } catch (NumberFormatException e) {
                            System.err.println("Error parseando activo: " + e.getMessage());
                        }
                    }
                }

                alumno.setUpdateAt(LocalDateTime.now());
                return alumnoRepository.save(alumno);
            }
        }
        return null; // Retorna null si no existe o el UUID no coincide
    }

    // ==========================================
    //          ELIMINACIÓN Y RESTAURACIÓN
    // ==========================================

    @Transactional
    public boolean eliminarAlumnoSuaveSeguro(Integer id, String uuid) {
        try {
            Optional<AlumnoModel> alumnoOptional = alumnoRepository.findById(id);
            if (alumnoOptional.isPresent() && alumnoOptional.get().getUuid().equals(uuid)) {
                AlumnoModel alumno = alumnoOptional.get();
                alumno.setDeleted(1);
                alumno.setUpdateAt(LocalDateTime.now());
                alumnoRepository.save(alumno);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error en borrado suave: " + e.getMessage());
        }
        return false;
    }

    @Transactional
    public boolean restaurarAlumnoSeguro(Integer id, String uuid) {
        try {
            Optional<AlumnoModel> alumnoOptional = alumnoRepository.findById(id);
            if (alumnoOptional.isPresent() && alumnoOptional.get().getUuid().equals(uuid)) {
                AlumnoModel alumno = alumnoOptional.get();
                alumno.setDeleted(0);
                alumno.setUpdateAt(LocalDateTime.now());
                alumnoRepository.save(alumno);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error en restauración: " + e.getMessage());
        }
        return false;
    }

    @Transactional
    public boolean eliminarAlumnoSeguro(Integer id, String uuid) {
        try {
            Optional<AlumnoModel> alumnoOptional = alumnoRepository.findById(id);
            if (alumnoOptional.isPresent() && alumnoOptional.get().getUuid().equals(uuid)) {
                alumnoRepository.deleteById(id);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error en borrado físico: " + e.getMessage());
        }
        return false;
    }

    // ==========================================
    //          BÚSQUEDAS ESPECÍFICAS
    // ==========================================

    public Optional<AlumnoModel> obtenerAlumnoPorIdYUuid(Integer id, String uuid) {
        Optional<AlumnoModel> alumno = alumnoRepository.findById(id);
        if (alumno.isPresent() && alumno.get().getUuid().equals(uuid)) {
            return alumno;
        }
        return Optional.empty();
    }

    public Optional<AlumnoModel> obtenerAlumnoPorNumeroControl(String numeroControl) {
        return alumnoRepository.findByNumeroControl(numeroControl);
    }

    public ArrayList<AlumnoModel> buscarAlumnosPorNombre(String nombre) {
        return (ArrayList<AlumnoModel>) alumnoRepository.findByPrimerNombreContainingIgnoreCase(nombre);
    }

    public long contarTotalAlumnos() {
        return alumnoRepository.count();
    }

    public boolean existeNumeroControl(String numeroControl) {
        return alumnoRepository.existsByNumeroControl(numeroControl);
    }

    // ==========================================
    //          BIOMETRÍA (HUELLAS)
    // ==========================================

    @Transactional
    public boolean guardarHuellaAlumno(Integer idAlumno, byte[] fmdBytes) {
        System.out.println("=== [DEBUG] GUARDANDO HUELLA PARA ID: " + idAlumno + " ===");
        Optional<AlumnoModel> alumnoOpt = alumnoRepository.findById(idAlumno);
        if (alumnoOpt.isPresent()) {
            AlumnoModel alumno = alumnoOpt.get();
            alumno.setHuellaFmd(fmdBytes);
            alumnoRepository.save(alumno);
            System.out.println("=== [DEBUG] HUELLA GUARDADA EXITOSAMENTE ===");
            return true;
        }
        System.err.println("=== [ERROR] NO SE ENCONTRÓ ALUMNO PARA VINCULAR HUELLA ===");
        return false;
    }

    public Optional<AlumnoModel> identificarAlumno(Fmd huellaCapturada) {
        try {
            ArrayList<AlumnoModel> alumnos = (ArrayList<AlumnoModel>) alumnoRepository.findByActivo(1);
            Engine engine = UareUGlobal.GetEngine();

            for (AlumnoModel alumno : alumnos) {
                if (alumno.getHuellaFmd() == null) continue;

                Fmd huellaGuardada = UareUGlobal.GetImporter().ImportFmd(
                        alumno.getHuellaFmd(),
                        Fmd.Format.ANSI_378_2004,
                        Fmd.Format.ANSI_378_2004
                );

                int falsematch_rate = engine.Compare(huellaCapturada, 0, huellaGuardada, 0);

                if (falsematch_rate < (0x7FFFFFFF / 100000)) {
                    System.out.println("=== [MATCH] ALUMNO IDENTIFICADO: " + alumno.getPrimerNombre() + " ===");
                    return Optional.of(alumno);
                }
            }
        } catch (UareUException e) {
            System.err.println("Error en comparación biométrica: " + e.getMessage());
        }
        return Optional.empty();
    }
}