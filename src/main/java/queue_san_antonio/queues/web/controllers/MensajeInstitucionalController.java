package queue_san_antonio.queues.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import queue_san_antonio.queues.models.ConfiguracionPantalla;
import queue_san_antonio.queues.models.MensajeInstitucional;
import queue_san_antonio.queues.services.ConfiguracionPantallaService;
import queue_san_antonio.queues.services.MensajeInstitucionalService;
import queue_san_antonio.queues.web.dto.common.ApiResponseWrapper;
import queue_san_antonio.queues.web.dto.mapper.MensajeInstitucionalMapper;
import queue_san_antonio.queues.web.dto.mensaje.*;
import queue_san_antonio.queues.web.exceptions.custom.ResourceNotFoundException;

import java.util.List;

//Controlador REST para la gestión de mensajes institucionales
//Incluye endpoints públicos para pantallas y endpoints administrativos
@RestController
@RequestMapping("/api/mensajes-institucionales")
@RequiredArgsConstructor
@Slf4j
@Validated
public class MensajeInstitucionalController {

    private final MensajeInstitucionalService mensajeInstitucionalService;
    private final ConfiguracionPantallaService configuracionPantallaService;

    // ==========================================
    // ENDPOINTS PÚBLICOS (PARA PANTALLAS)
    // ==========================================

    //Lista mensajes vigentes para las pantallas públicas
    //GET /api/mensajes-institucionales/vigentes
    @GetMapping("/vigentes")
    public ResponseEntity<ApiResponseWrapper<List<MensajeInstitucionalSummaryResponse>>> listarMensajesVigentes() {

        log.debug("Solicitando mensajes vigentes para pantallas");

        List<MensajeInstitucional> mensajes = mensajeInstitucionalService.listarMensajesVigentes();
        List<MensajeInstitucionalSummaryResponse> response =
                MensajeInstitucionalMapper.toSummaryResponseList(mensajes);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d mensajes vigentes", mensajes.size()))
        );
    }

    //Lista mensajes de una configuración específica (para pantallas)
    //GET /api/mensajes-institucionales/configuracion/{configuracionId}
    @GetMapping("/configuracion/{configuracionId}")
    public ResponseEntity<ApiResponseWrapper<List<MensajeInstitucionalSummaryResponse>>> listarPorConfiguracion(
            @PathVariable Long configuracionId) {

        log.debug("Solicitando mensajes para configuración ID: {}", configuracionId);

        // Verificar que la configuración existe
        ConfiguracionPantalla configuracion = configuracionPantallaService.obtenerConfiguracionActiva()
                .orElseThrow(() -> ResourceNotFoundException.configuracionPantalla(configuracionId));

        List<MensajeInstitucional> mensajes = mensajeInstitucionalService.listarPorConfiguracion(configuracionId);
        List<MensajeInstitucionalSummaryResponse> response =
                MensajeInstitucionalMapper.toSummaryResponseList(mensajes);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d mensajes para la configuración %s",
                                mensajes.size(), configuracion.getNombre()))
        );
    }

    // ==========================================
    // ENDPOINTS DE CONSULTA (ADMINISTRADORES)
    // ==========================================

    //Obtiene un mensaje específico por ID
    //GET /api/mensajes-institucionales/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<MensajeInstitucionalResponse>> obtenerPorId(@PathVariable Long id) {

        log.debug("Obteniendo mensaje institucional ID: {}", id);

        MensajeInstitucional mensaje = mensajeInstitucionalService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.mensajeInstitucional(id));

        MensajeInstitucionalResponse response = MensajeInstitucionalMapper.toResponse(mensaje);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Mensaje encontrado")
        );
    }

    //Lista todos los mensajes de una configuración (vista administrativa)
    //GET /api/mensajes-institucionales/admin/configuracion/{configuracionId}
    @GetMapping("/admin/configuracion/{configuracionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<MensajeInstitucionalResponse>>> listarTodosPorConfiguracion(
            @PathVariable Long configuracionId) {

        log.debug("Listando todos los mensajes para configuración ID: {} (vista admin)", configuracionId);

        // Verificar que la configuración existe
        ConfiguracionPantalla configuracion = configuracionPantallaService.obtenerConfiguracionActiva()
                .orElseThrow(() -> ResourceNotFoundException.configuracionPantalla(configuracionId));

//        List<MensajeInstitucional> mensajes = mensajeInstitucionalService.listarPorConfiguracion(configuracionId);
        List<MensajeInstitucional> mensajes = mensajeInstitucionalService.listarTodosPorConfiguracion(configuracionId);
        List<MensajeInstitucionalResponse> response =
                MensajeInstitucionalMapper.toResponseList(mensajes);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d mensajes para la configuración %s",
                                mensajes.size(), configuracion.getNombre()))
        );
    }

    // ==========================================
    // ENDPOINTS DE CREACIÓN Y ACTUALIZACIÓN
    // ==========================================

    //Crea un nuevo mensaje institucional
    //POST /api/mensajes-institucionales/configuracion/{configuracionId}
    @PostMapping("/configuracion/{configuracionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<MensajeInstitucionalResponse>> crearMensaje(
            @PathVariable Long configuracionId,
            @Valid @RequestBody MensajeInstitucionalRequest request) {

        log.info("Creando mensaje institucional para configuración ID: {} - Tipo: {}",
                configuracionId, request.getTipo());

        // Validar ruta de archivo para IMAGEN/VIDEO
        if (!request.esValidoParaTipo()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("La ruta del archivo es obligatoria para mensajes de imagen y video",
                            "MISSING_FILE_PATH")
            );
        }

        // NUEVA VALIDACIÓN: Validar contenido según tipo
        if (!request.esContenidoValido()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("El contenido es obligatorio para mensajes de texto",
                            "MISSING_CONTENT")
            );
        }

        // Validar fechas de vigencia si se proporcionan
        if (request.getFechaInicio() != null && request.getFechaFin() != null) {
            if (request.getFechaFin().isBefore(request.getFechaInicio()) ||
                    request.getFechaFin().isEqual(request.getFechaInicio())) {
                return ResponseEntity.badRequest().body(
                        ApiResponseWrapper.error("La fecha de fin debe ser posterior a la fecha de inicio",
                                "INVALID_DATE_RANGE")
                );
            }
        }

        try {
            MensajeInstitucional nuevoMensaje = mensajeInstitucionalService.crear(
                    configuracionId,
                    request.getTipo(),
                    request.getTitulo(),
                    request.getContenido(),
                    request.getRutaArchivo(),
                    request.getDuracion(),
                    request.getOrden(),
                    request.getFechaInicio(),
                    request.getFechaFin()
            );

            MensajeInstitucionalResponse response = MensajeInstitucionalMapper.toResponse(nuevoMensaje);

            log.info("Mensaje institucional creado exitosamente - ID: {} para configuración: {}",
                    nuevoMensaje.getId(), configuracionId);

            return ResponseEntity.status(201).body(
                    ApiResponseWrapper.success(response,
                            "Mensaje institucional creado exitosamente")
            );

        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al crear mensaje: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error(e.getMessage(), "VALIDATION_ERROR")
            );
        } catch (ResourceNotFoundException e) {
            log.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(404).body(
                    ApiResponseWrapper.error(e.getMessage(), "NOT_FOUND")
            );
        } catch (Exception e) {
            log.error("Error interno al crear mensaje institucional", e);
            return ResponseEntity.status(500).body(
                    ApiResponseWrapper.error("Error interno del servidor", "INTERNAL_ERROR")
            );
        }
    }

    // ==========================================
    // ENDPOINTS DE GESTIÓN DE ESTADO
    // ==========================================

    //Activa un mensaje
    //PUT /api/mensajes-institucionales/{id}/activar
    @PutMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<MensajeInstitucionalResponse>> activarMensaje(@PathVariable Long id) {

        log.info("Activando mensaje institucional ID: {}", id);

        mensajeInstitucionalService.activar(id);

        MensajeInstitucional mensaje = mensajeInstitucionalService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.mensajeInstitucional(id));

        MensajeInstitucionalResponse response = MensajeInstitucionalMapper.toResponse(mensaje);

        log.info("Mensaje institucional activado: {}", mensaje.getTitulo());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Mensaje activado exitosamente")
        );
    }

    //Desactiva un mensaje
    //PUT /api/mensajes-institucionales/{id}/desactivar
    @PutMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<MensajeInstitucionalResponse>> desactivarMensaje(@PathVariable Long id) {

        log.info("Desactivando mensaje institucional ID: {}", id);

        mensajeInstitucionalService.desactivar(id);

        MensajeInstitucional mensaje = mensajeInstitucionalService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.mensajeInstitucional(id));

        MensajeInstitucionalResponse response = MensajeInstitucionalMapper.toResponse(mensaje);

        log.info("Mensaje institucional desactivado: {}", mensaje.getTitulo());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Mensaje desactivado exitosamente")
        );
    }

    //Establece la vigencia de un mensaje
    //PUT /api/mensajes-institucionales/{id}/vigencia
    @PutMapping("/{id}/vigencia")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<MensajeInstitucionalResponse>> establecerVigencia(
            @PathVariable Long id,
            @Valid @RequestBody EstablecerVigenciaRequest request) {

        log.info("Estableciendo vigencia para mensaje ID: {} - Desde: {} hasta: {}",
                id, request.getFechaInicio(), request.getFechaFin());

        // Validar fechas
        if (!request.fechasValidas()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("La fecha de fin no puede ser anterior a la fecha de inicio",
                            "INVALID_DATE_RANGE")
            );
        }

        mensajeInstitucionalService.establecerVigencia(
                id,
                request.getFechaInicio(),
                request.getFechaFin()
        );

        MensajeInstitucional mensaje = mensajeInstitucionalService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.mensajeInstitucional(id));

        MensajeInstitucionalResponse response = MensajeInstitucionalMapper.toResponse(mensaje);

        log.info("Vigencia establecida para mensaje: {} - Desde: {} hasta: {}",
                mensaje.getTitulo(), request.getFechaInicio(), request.getFechaFin());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Vigencia establecida exitosamente")
        );
    }

    // ==========================================
    // ENDPOINTS DE ELIMINACIÓN
    // ==========================================

    //Elimina un mensaje institucional
    //DELETE /api/mensajes-institucionales/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<String>> eliminarMensaje(@PathVariable Long id) {

        log.info("Eliminando mensaje institucional ID: {}", id);

        // Verificar que existe antes de eliminar
        MensajeInstitucional mensaje = mensajeInstitucionalService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.mensajeInstitucional(id));

        String tituloMensaje = mensaje.getTitulo();

        mensajeInstitucionalService.eliminar(id);

        log.info("Mensaje institucional eliminado: {}", tituloMensaje);

        return ResponseEntity.ok(
                ApiResponseWrapper.success("OK", "Mensaje eliminado exitosamente")
        );
    }
}