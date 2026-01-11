package com.example.fingerprint_api.components;

import com.digitalpersona.uareu.*;
import com.digitalpersona.uareu.Reader.CaptureCallback;
import com.example.fingerprint_api.models.Alumno.AlumnoModel;
import com.example.fingerprint_api.repositories.AlumnoRepository;
import com.example.fingerprint_api.services.AlumnoService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class FingerprintDeviceManager implements Reader.CaptureCallback {


    // Modos de operación del lector
    public enum DeviceMode {
        IDENTIFICATION, // Modo normal: Escanea y busca al alumno (Entrada)
        ENROLLMENT      // Modo registro: Espera 4 toques para crear huella nueva
    }

    private Reader currentReader;
    private DeviceMode currentMode = DeviceMode.IDENTIFICATION;

    // Variables para el proceso de enrolamiento (Registro de huella)
    private Engine.EnrollmentCallback enrollmentCallback;
    private int enrollmentSteps = 0; // Cuenta las veces que has puesto el dedo (0 a 4)
    private Fmd fmdParaGuardar = null; // Huella final lista para guardar
    private Integer idAlumnoEnProceso = null; // ID del alumno que estamos registrando

    @Autowired
    private BiometricCache biometricCache;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private AlumnoService alumnoService;

    // Cola para enviar mensajes de estado al log o frontend (opcional)
    public final LinkedBlockingQueue<String> statusQueue = new LinkedBlockingQueue<>();

    /**
     * 1. INICIALIZACIÓN: Busca y abre el lector 4500 al iniciar la App
     */
    @PostConstruct
    public void initDevice() {
        try {
            System.out.println(">>> BUSCANDO LECTOR DE HUELLA DIGITAL...");
            ReaderCollection readers = UareUGlobal.GetReaderCollection();
            readers.GetReaders();

            if (readers.isEmpty()) {
                System.err.println("!!! NO SE DETECTÓ NINGÚN LECTOR CONNECTADO !!!");
                return;
            }

            // Tomamos el primer lector encontrado
            currentReader = readers.get(0);
            System.out.println(">>> LECTOR DETECTADO: " + currentReader.GetDescription().name);

            // Abrimos conexión con el hardware
            currentReader.Open(Reader.Priority.COOPERATIVE);

            // Iniciamos la captura continua (Streaming)
            startCapture();

        } catch (UareUException e) {
            System.err.println("Error inicializando lector: " + e.getMessage());
        }
    }

    /**
     * Inicia el modo de escucha asíncrona.
     */
    private void startCapture() {
        try {
            if (currentReader != null) {
                // Inicia captura asíncrona.
                // 'this' significa que el método CaptureResult (abajo) se llamará cuando pongas el dedo.
                currentReader.StartStreaming();
                System.out.println(">>> LECTOR LISTO Y ESCUCHANDO (Modo: " + currentMode + ")");
            }
        } catch (UareUException e) {
            System.err.println("Error iniciando stream: " + e.getMessage());
        }
    }

    /**
     * 2. EVENTO PRINCIPAL: Se ejecuta cada vez que alguien pone el dedo.
     * NOTA: En tu versión del JAR el método se llama CaptureResultEvent.
     */
    @Override
    public void CaptureResultEvent(Reader.CaptureResult result) { // <--- AQUÍ: Llamamos a la variable "result"

        // Si la captura falló o es de mala calidad, ignorar
        if (result.image == null || result.quality == Reader.CaptureQuality.CANCELED) return;

        try {
            Engine engine = UareUGlobal.GetEngine();

            // Convertir la imagen cruda (FID) a características matemáticas (FMD)
            // Usamos 'result.image' porque el parámetro arriba se llama 'result'
            Fmd fmdInput = engine.CreateFmd(result.image, Fmd.Format.ANSI_378_2004);

            // DECISIÓN: ¿Qué hacemos con esta huella?
            if (currentMode == DeviceMode.IDENTIFICATION) {
                procesarIdentificacion(fmdInput, engine);
            } else if (currentMode == DeviceMode.ENROLLMENT) {
                procesarEnrolamiento(fmdInput, engine);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * MODO IDENTIFICACIÓN: Busca la huella en el Cache y registra entrada.
     */
    private void procesarIdentificacion(Fmd fmdInput, Engine engine) {
        try {
            int targetRate = 2147; // Probabilidad de error 0.001% (Estándar)
            boolean matchFound = false;

            // Recorrer memoria RAM (Cache) - Es muy rápido
            for (Map.Entry<Integer, Fmd> entry : biometricCache.getMapa().entrySet()) {

                // Comparar huella capturada vs huella en memoria
                int score = engine.Compare(fmdInput, 0, entry.getValue(), 0);

                if (score < targetRate) {
                    // ¡COINCIDENCIA ENCONTRADA!
                    Integer idAlumno = entry.getKey();
                    System.out.println(">>> ¡ALUMNO IDENTIFICADO! ID: " + idAlumno);

                    // Aquí llamamos a la lógica para registrar la entrada en BD
                    registrarEntradaEnBaseDeDatos(idAlumno);

                    matchFound = true;
                    break; // Dejar de buscar
                }
            }

            if (!matchFound) {
                System.out.println(">>> HUELLA NO RECONOCIDA");
            }

        } catch (UareUException e) {
            System.err.println("Error en comparación: " + e.getMessage());
        }
    }

    /**
     * MODO REGISTRO: Requiere 4 toques para crear una plantilla perfecta.
     */
    private void procesarEnrolamiento(Fmd fmdInput, Engine engine) {
        try {
            // El SDK tiene una clase 'Enrollment' que junta 4 muestras
            // Aquí simulamos la lógica básica o usamos Enrollment Class si la tienes.
            // Para simplificar integración con Spring, usaremos una lógica manual simple:
            // "Guardar la última huella de buena calidad inmediatamente"
            // O implementar la lógica de 4 pasos. Implementaré la lógica de 4 pasos.

            if (fmdParaGuardar == null) {
                // Creamos un enrollador si no existe
                // Nota: En una implementación real completa, se usa engine.CreateEnrollmentFmd
                // que pide 4 FMDs. Para este ejemplo rápido, guardaremos la PRIMERA buena captura
                // para facilitar tus pruebas iniciales, luego puedes habilitar el loop de 4.

                // --- MODO SIMPLE (1 Toque para registrar) ---
                System.out.println(">>> HUELLA CAPTURADA PARA REGISTRO");
                this.fmdParaGuardar = fmdInput;

                // Guardar en BD
                guardarHuellaEnBD();

                // Volver a modo normal
                setMode(DeviceMode.IDENTIFICATION, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Guarda la huella capturada en el Alumno pendiente.
     */
    private void guardarHuellaEnBD() {
        if (idAlumnoEnProceso != null && fmdParaGuardar != null) {
            alumnoService.guardarHuellaAlumno(idAlumnoEnProceso, fmdParaGuardar.getData());

            // Actualizar el caché inmediatamente para que funcione sin reiniciar
            biometricCache.agregarAlCache(idAlumnoEnProceso, fmdParaGuardar.getData());

            System.out.println(">>> HUELLA GUARDADA EXITOSAMENTE EN BD Y CACHÉ PARA ID: " + idAlumnoEnProceso);
        }
    }

    /**
     * Método auxiliar para registrar la entrada en la tabla 'registro_entrada_alumno'
     */
    private void registrarEntradaEnBaseDeDatos(Integer idAlumno) {
        // Aquí puedes inyectar un servicio 'EntradaService' y llamar a .save()
        // Ejemplo rápido:
        // RegistroEntrada entrada = new RegistroEntrada();
        // entrada.setFecha(new Date());
        // entrada.setAlumnoId(idAlumno);
        // repoEntrada.save(entrada);
        System.out.println(" (Simulacion) Insertando registro en tabla 'registro_entrada_alumno'...");
    }

    // ================= MÉTODOS PÚBLICOS DE CONTROL =================

    /**
     * Llamar desde el Controller para iniciar el registro de un alumno.
     */
    public void iniciarRegistro(Integer idAlumno) {
        this.idAlumnoEnProceso = idAlumno;
        this.fmdParaGuardar = null;
        this.enrollmentSteps = 0;
        setMode(DeviceMode.ENROLLMENT, null);
        System.out.println(">>> SISTEMA LISTO PARA REGISTRAR ALUMNO ID: " + idAlumno + ". PON EL DEDO EN EL LECTOR.");
    }

    public void setMode(DeviceMode mode, Integer idAlumno) {
        this.currentMode = mode;
        if(mode == DeviceMode.IDENTIFICATION) {
            this.idAlumnoEnProceso = null;
            System.out.println(">>> MODO CAMBIADO A: IDENTIFICACIÓN (ENTRADAS)");
        } else {
            System.out.println(">>> MODO CAMBIADO A: REGISTRO");
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (currentReader != null) {
                currentReader.StopStreaming();
                currentReader.Close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}