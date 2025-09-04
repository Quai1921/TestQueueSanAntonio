package queue_san_antonio.queues.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import queue_san_antonio.queues.models.ConfiguracionPantalla;
import queue_san_antonio.queues.repositories.ConfiguracionPantallaRepository;
import queue_san_antonio.queues.services.ConfiguracionPantallaService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConfiguracionPantallaServiceImpl implements ConfiguracionPantallaService {

    private final ConfiguracionPantallaRepository configuracionPantallaRepository;

    @Override
    public ConfiguracionPantalla guardar(ConfiguracionPantalla configuracion) {
        log.debug("Guardando configuración de pantalla: {}", configuracion.getNombre());
        return configuracionPantallaRepository.save(configuracion);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfiguracionPantalla> obtenerConfiguracionActiva() {
        log.debug("Obteniendo configuración activa de pantalla");
        return configuracionPantallaRepository.findByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfiguracionPantalla> listarTodas() {
        return configuracionPantallaRepository.findAllByOrderByNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfiguracionPantalla> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        log.debug("Buscando configuración de pantalla por ID: {}", id);
        return configuracionPantallaRepository.findById(id);
    }

    @Override
    public ConfiguracionPantalla crear(String nombre, Integer tiempoMensaje, Integer tiempoTurno) {
        // Validar parámetros obligatorios
        validarParametrosCreacion(nombre, tiempoMensaje, tiempoTurno);

        String nombreLimpio = nombre.trim();

        log.info("Creando nueva configuración de pantalla: {}", nombreLimpio);

        // Crear configuración con valores por defecto
        ConfiguracionPantalla nuevaConfiguracion = ConfiguracionPantalla.builder()
                .nombre(nombreLimpio)
                .tiempoMensaje(tiempoMensaje)
                .tiempoTurno(tiempoTurno)
                .sonidoActivo(true)
                .volumenSonido(70)
                .animacionesActivas(true)
                .textoEncabezado("Portal de Atención")
                .activo(false) // Nueva configuración inactiva por defecto
                .build();

        ConfiguracionPantalla configuracionGuardada = guardar(nuevaConfiguracion);

        log.debug("Configuración de pantalla creada: {} - ID: {}", nombreLimpio, configuracionGuardada.getId());

        return configuracionGuardada;
    }

    @Override
    public void activar(Long configuracionId) {
        if (configuracionId == null) {
            throw new IllegalArgumentException("El ID de la configuración no puede ser nulo");
        }

        log.info("Activando configuración de pantalla ID: {}", configuracionId);

        ConfiguracionPantalla configuracion = configuracionPantallaRepository.findById(configuracionId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró configuración con ID: " + configuracionId));

        // Desactivar cualquier configuración activa actual
        Optional<ConfiguracionPantalla> configuracionActiva = obtenerConfiguracionActiva();
        if (configuracionActiva.isPresent() && !configuracionActiva.get().getId().equals(configuracionId)) {
            ConfiguracionPantalla actual = configuracionActiva.get();
            actual.desactivar();
            guardar(actual);
            log.debug("Configuración anterior {} desactivada", actual.getNombre());
        }

        // Activar nueva configuración
        configuracion.activar();
        guardar(configuracion);

        log.info("Configuración {} activada exitosamente", configuracion.getNombre());
    }

    @Override
    public void configurarSonido(Long configuracionId, Boolean activo, String archivo, Integer volumen) {
        if (configuracionId == null) {
            throw new IllegalArgumentException("El ID de la configuración no puede ser nulo");
        }

        log.info("Configurando sonido para configuración ID: {}", configuracionId);

        ConfiguracionPantalla configuracion = configuracionPantallaRepository.findById(configuracionId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró configuración con ID: " + configuracionId));

        // Validar volumen si se proporciona
        if (volumen != null && (volumen < 0 || volumen > 100)) {
            throw new IllegalArgumentException("El volumen debe estar entre 0 y 100");
        }

        configuracion.configurarSonido(activo, archivo, volumen);
        guardar(configuracion);

        log.debug("Sonido configurado para {}: activo={}, volumen={}", configuracion.getNombre(), activo, volumen);
    }


    @Override
    public void configurarApariencia(Long configuracionId) {
        if (configuracionId == null) {
            throw new IllegalArgumentException("El ID de la configuración no puede ser nulo");
        }

        log.info("Configurando apariencia para configuración ID: {}", configuracionId);

        ConfiguracionPantalla configuracion = configuracionPantallaRepository.findById(configuracionId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró configuración con ID: " + configuracionId));

        // MANTENER el valor actual de animaciones en lugar de pasarle null
        configuracion.configurarApariencia(
                configuracion.getAnimacionesActivas() // <-- ESTO ES LO QUE HAY QUE CAMBIAR
        );

        guardar(configuracion);

        log.debug("Apariencia configurada para {}: tema={}, mostrarLogo={}",
                configuracion.getNombre());
    }

    //Método adicional para actualizar configuración básica
    @Override
    public ConfiguracionPantalla actualizar(Long configuracionId, String nombre, Integer tiempoMensaje,
                                            Integer tiempoTurno, String textoEncabezado) {
        if (configuracionId == null) {
            throw new IllegalArgumentException("El ID de la configuración no puede ser nulo");
        }

        log.info("Actualizando configuración ID: {}", configuracionId);

        ConfiguracionPantalla configuracion = configuracionPantallaRepository.findById(configuracionId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró configuración con ID: " + configuracionId));

        configuracion.actualizarConfiguracion(
                nombre != null ? nombre.trim() : configuracion.getNombre(),
                tiempoMensaje != null ? tiempoMensaje : configuracion.getTiempoMensaje(),
                tiempoTurno != null ? tiempoTurno : configuracion.getTiempoTurno(),
                textoEncabezado != null ? textoEncabezado.trim() : configuracion.getTextoEncabezado()
        );

        ConfiguracionPantalla configuracionActualizada = guardar(configuracion);

        log.debug("Configuración {} actualizada exitosamente", configuracion.getNombre());

        return configuracionActualizada;
    }

    //Valida los parámetros obligatorios para crear configuración
    private void validarParametrosCreacion(String nombre, Integer tiempoMensaje, Integer tiempoTurno) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la configuración es obligatorio");
        }
        if (tiempoMensaje == null || tiempoMensaje < 3 || tiempoMensaje > 60) {
            throw new IllegalArgumentException("El tiempo de mensaje debe estar entre 3 y 60 segundos");
        }
        if (tiempoTurno == null || tiempoTurno < 3 || tiempoTurno > 30) {
            throw new IllegalArgumentException("El tiempo de turno debe estar entre 3 y 30 segundos");
        }
    }
}