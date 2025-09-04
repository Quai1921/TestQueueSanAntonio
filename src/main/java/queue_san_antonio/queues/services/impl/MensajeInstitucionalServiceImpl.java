package queue_san_antonio.queues.services.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import queue_san_antonio.queues.models.ConfiguracionPantalla;
import queue_san_antonio.queues.models.MensajeInstitucional;
import queue_san_antonio.queues.models.TipoMensaje;
import queue_san_antonio.queues.repositories.ConfiguracionPantallaRepository;
import queue_san_antonio.queues.repositories.MensajeInstitucionalRepository;
import queue_san_antonio.queues.services.MensajeInstitucionalService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MensajeInstitucionalServiceImpl implements MensajeInstitucionalService {

    private final MensajeInstitucionalRepository mensajeInstitucionalRepository;
    private final ConfiguracionPantallaRepository configuracionPantallaRepository;

    @Override
    public MensajeInstitucional guardar(MensajeInstitucional mensaje) {
        log.debug("Guardando mensaje institucional: {}", mensaje.getTitulo());
        return mensajeInstitucionalRepository.save(mensaje);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MensajeInstitucional> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return mensajeInstitucionalRepository.findById(id);
    }

    @Override
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del mensaje no puede ser nulo");
        }

        log.info("Eliminando mensaje institucional ID: {}", id);

        if (!mensajeInstitucionalRepository.existsById(id)) {
            throw new IllegalArgumentException("No se encontró mensaje con ID: " + id);
        }

        mensajeInstitucionalRepository.deleteById(id);

        log.debug("Mensaje institucional {} eliminado exitosamente", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeInstitucional> listarPorConfiguracion(Long configuracionId) {
        if (configuracionId == null) {
            return List.of();
        }
        return mensajeInstitucionalRepository.findByConfiguracionIdAndActivoTrueOrderByOrdenAsc(configuracionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeInstitucional> listarMensajesVigentes() {
        LocalDateTime ahora = LocalDateTime.now();
        return mensajeInstitucionalRepository.findMensajesVigentes(ahora);
    }

    @Override
    public MensajeInstitucional crear(Long configuracionId, TipoMensaje tipo, String titulo,
                                      String contenido, String rutaArchivo, Integer duracion, Integer orden, LocalDate fechaInicio, LocalDate fechaFin) {
        // Validar parámetros obligatorios
        validarParametrosCreacion(configuracionId, tipo, duracion);

        log.info("Creando mensaje institucional para configuración {}: {} - {}",
                configuracionId, tipo, titulo);

        // Buscar configuración
        ConfiguracionPantalla configuracion = configuracionPantallaRepository.findById(configuracionId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró configuración con ID: " + configuracionId));

        // Limpiar datos
        String tituloLimpio = titulo != null ? titulo.trim() : null;
        String contenidoLimpio = contenido != null ? contenido.trim() : null;
        String rutaArchivoLimpia = rutaArchivo != null ? rutaArchivo.trim() : null;

        // Validar contenido según tipo
        validarContenidoPorTipo(tipo, tituloLimpio, contenidoLimpio, rutaArchivoLimpia);

        // Establecer valores por defecto
        if (orden == null) {
            orden = 0;
        }

        // Convertir fechas a LocalDateTime
        LocalDateTime inicioDateTime = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime finDateTime = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;

        // Validar fechas
        if (inicioDateTime != null && finDateTime != null && inicioDateTime.isAfter(finDateTime)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }

        // Crear mensaje
        MensajeInstitucional nuevoMensaje = MensajeInstitucional.builder()
                .configuracion(configuracion)
                .tipo(tipo)
                .titulo(tituloLimpio)
                .contenido(contenidoLimpio)
                .rutaArchivo(rutaArchivoLimpia)
                .duracion(duracion)
                .orden(orden)
                .activo(true)
                .fechaInicio(inicioDateTime)
                .fechaFin(finDateTime)
                .build();

        MensajeInstitucional mensajeGuardado = guardar(nuevoMensaje);

        log.debug("Mensaje institucional creado: {} - ID: {}", tituloLimpio, mensajeGuardado.getId());

        return mensajeGuardado;
    }

    @Override
    public void activar(Long mensajeId) {
        if (mensajeId == null) {
            throw new IllegalArgumentException("El ID del mensaje no puede ser nulo");
        }

        log.info("Activando mensaje institucional ID: {}", mensajeId);

        MensajeInstitucional mensaje = buscarPorId(mensajeId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró mensaje con ID: " + mensajeId));

        if (mensaje.estaActivo()) {
            log.debug("El mensaje {} ya estaba activo", mensajeId);
            return;
        }

        mensaje.activar();
        guardar(mensaje);

        log.debug("Mensaje {} activado exitosamente", mensaje.getTitulo());
    }

    @Override
    public void desactivar(Long mensajeId) {
        if (mensajeId == null) {
            throw new IllegalArgumentException("El ID del mensaje no puede ser nulo");
        }

        log.info("Desactivando mensaje institucional ID: {}", mensajeId);

        MensajeInstitucional mensaje = buscarPorId(mensajeId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró mensaje con ID: " + mensajeId));

        if (!mensaje.estaActivo()) {
            log.debug("El mensaje {} ya estaba inactivo", mensajeId);
            return;
        }

        mensaje.desactivar();
        guardar(mensaje);

        log.debug("Mensaje {} desactivado exitosamente", mensaje.getTitulo());
    }

    @Override
    public void establecerVigencia(Long mensajeId, LocalDate fechaInicio, LocalDate fechaFin) {
        if (mensajeId == null) {
            throw new IllegalArgumentException("El ID del mensaje no puede ser nulo");
        }

        log.info("Estableciendo vigencia para mensaje ID: {} - Desde: {} Hasta: {}",
                mensajeId, fechaInicio, fechaFin);

        MensajeInstitucional mensaje = buscarPorId(mensajeId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró mensaje con ID: " + mensajeId));

        // Convertir fechas a LocalDateTime
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;

        // Validar fechas
        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }

        mensaje.establecerVigencia(inicio, fin);
        guardar(mensaje);

        log.debug("Vigencia establecida para mensaje {}: {} - {}", mensaje.getTitulo(), inicio, fin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeInstitucional> listarTodosPorConfiguracion(Long configuracionId) {
        if (configuracionId == null) {
            return List.of();
        }
        return mensajeInstitucionalRepository.findByConfiguracionIdOrderByOrdenAsc(configuracionId);
    }

    //Método adicional para actualizar contenido de mensaje
    public MensajeInstitucional actualizar(Long mensajeId, String titulo, String contenido,
                                           Integer duracion, Integer orden) {
        if (mensajeId == null) {
            throw new IllegalArgumentException("El ID del mensaje no puede ser nulo");
        }

        log.info("Actualizando mensaje institucional ID: {}", mensajeId);

        MensajeInstitucional mensaje = buscarPorId(mensajeId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró mensaje con ID: " + mensajeId));

        mensaje.actualizarContenido(
                titulo != null ? titulo.trim() : mensaje.getTitulo(),
                contenido != null ? contenido.trim() : mensaje.getContenido(),
                duracion != null ? duracion : mensaje.getDuracion()
        );

        if (orden != null) {
            mensaje.setOrden(orden);
        }

        MensajeInstitucional mensajeActualizado = guardar(mensaje);

        log.debug("Mensaje {} actualizado exitosamente", mensaje.getTitulo());

        return mensajeActualizado;
    }

    //Valida los parámetros obligatorios para crear mensaje
    private void validarParametrosCreacion(Long configuracionId, TipoMensaje tipo, Integer duracion) {
        if (configuracionId == null) {
            throw new IllegalArgumentException("El ID de la configuración es obligatorio");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de mensaje es obligatorio");
        }
        if (duracion == null || duracion < 3 || duracion > 120) {
            throw new IllegalArgumentException("La duración debe estar entre 3 y 120 segundos");
        }
    }

    //Valida el contenido según el tipo de mensaje
    private void validarContenidoPorTipo(TipoMensaje tipo, String titulo, String contenido, String rutaArchivo) {
        switch (tipo) {
            case TEXTO -> {
                if (contenido == null || contenido.isEmpty()) {
                    throw new IllegalArgumentException("El contenido es obligatorio para mensajes de texto");
                }
            }
            case IMAGEN -> {
                if (titulo == null || titulo.isEmpty()) {
                    throw new IllegalArgumentException("El título es obligatorio para mensajes con imagen");
                }

                if (rutaArchivo == null || rutaArchivo.isEmpty()) {
                    throw new IllegalArgumentException("La ruta del archivo es obligatoria para mensajes con imagen");
                }
            }
            case VIDEO -> {
                if (titulo == null || titulo.isEmpty()) {
                    throw new IllegalArgumentException("El título es obligatorio para mensajes con video");
                }

                if (rutaArchivo == null || rutaArchivo.isEmpty()) {
                    throw new IllegalArgumentException("La ruta del archivo es obligatoria para mensajes con video");
                }
            }
        }
    }
}