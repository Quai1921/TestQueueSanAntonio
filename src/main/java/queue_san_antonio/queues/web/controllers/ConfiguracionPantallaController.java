package queue_san_antonio.queues.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import queue_san_antonio.queues.models.ConfiguracionPantalla;
import queue_san_antonio.queues.services.ConfiguracionPantallaService;
import queue_san_antonio.queues.web.dto.common.ApiResponseWrapper;
import queue_san_antonio.queues.web.dto.configuracion.*;
import queue_san_antonio.queues.web.dto.mapper.ConfiguracionPantallaMapper;
import queue_san_antonio.queues.web.exceptions.custom.ResourceNotFoundException;

import java.util.List;

//Controlador REST para la gestión de configuraciones de pantalla
//Incluye endpoints públicos para pantallas y endpoints administrativos
@RestController
@RequestMapping("/api/configuraciones-pantalla")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ConfiguracionPantallaController {

    private final ConfiguracionPantallaService configuracionPantallaService;

    // ==========================================
    // ENDPOINTS PÚBLICOS (PARA PANTALLAS)
    // ==========================================

    //Obtiene la configuración activa para las pantallas públicas
    //GET /api/configuraciones-pantalla/activa
    @GetMapping("/activa")
    public ResponseEntity<ApiResponseWrapper<ConfiguracionPantallaResponse>> obtenerConfiguracionActiva() {

        log.debug("Solicitando configuración activa para pantallas");

        ConfiguracionPantalla configuracion = configuracionPantallaService.obtenerConfiguracionActiva()
                .orElse(null);

        if (configuracion == null) {
            return ResponseEntity.ok(
                    ApiResponseWrapper.success(null, "No hay configuración activa")
            );
        }

        ConfiguracionPantallaResponse response = ConfiguracionPantallaMapper.toResponse(configuracion);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Configuración activa encontrada")
        );
    }

    // ==========================================
    // ENDPOINTS DE CONSULTA (ADMINISTRADORES)
    // ==========================================

    //Lista todas las configuraciones
    //GET /api/configuraciones-pantalla
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<ApiResponseWrapper<List<ConfiguracionPantallaSummaryResponse>>> listarTodas() {
    public ResponseEntity<ApiResponseWrapper<List<ConfiguracionPantallaResponse>>> listarTodas() {

        log.debug("Listando todas las configuraciones de pantalla");

        List<ConfiguracionPantalla> configuraciones = configuracionPantallaService.listarTodas();
//        List<ConfiguracionPantallaSummaryResponse> response =
//                ConfiguracionPantallaMapper.toSummaryResponseList(configuraciones);
        List<ConfiguracionPantallaResponse> response =
                ConfiguracionPantallaMapper.toResponseList(configuraciones);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d configuraciones", configuraciones.size()))
        );
    }

    //Obtiene una configuración específica por ID
    //GET /api/configuraciones-pantalla/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<ConfiguracionPantallaResponse>> obtenerPorId(@PathVariable Long id) {

        log.debug("Obteniendo configuración de pantalla ID: {}", id);

        ConfiguracionPantalla configuracion = configuracionPantallaService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.configuracionPantalla(id));

        ConfiguracionPantallaResponse response = ConfiguracionPantallaMapper.toResponse(configuracion);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Configuración encontrada")
        );
    }

    // ==========================================
    // ENDPOINTS DE CREACIÓN Y ACTUALIZACIÓN
    // ==========================================

    //Crea una nueva configuración de pantalla
    //POST /api/configuraciones-pantalla
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<ConfiguracionPantallaResponse>> crearConfiguracion(
            @Valid @RequestBody ConfiguracionPantallaRequest request) {

        log.info("Creando nueva configuración de pantalla: {}", request.getNombre());

        ConfiguracionPantalla configuracion = configuracionPantallaService.crear(
                request.getNombre(),
                request.getTiempoMensaje(),
                request.getTiempoTurno()
        );

        // Aplicar configuraciones adicionales si se proporcionan
        if (request.getTextoEncabezado() != null ||
                request.getSonidoActivo() != null ||
                request.getArchivoSonido() != null ||
                request.getVolumenSonido() != null) {

            // Actualizar configuración básica
            configuracion = configuracionPantallaService.actualizar(
                    configuracion.getId(),
                    configuracion.getNombre(),
                    configuracion.getTiempoMensaje(),
                    configuracion.getTiempoTurno(),
                    request.getTextoEncabezado()
            );
        }

        // Configurar sonido si se especifica
        if (request.getSonidoActivo() != null ||
                request.getArchivoSonido() != null ||
                request.getVolumenSonido() != null) {

            configuracionPantallaService.configurarSonido(
                    configuracion.getId(),
                    request.getSonidoActivo() != null ? request.getSonidoActivo() : configuracion.getSonidoActivo(),
                    request.getArchivoSonido() != null ? request.getArchivoSonido() : configuracion.getArchivoSonido(),
                    request.getVolumenSonido() != null ? request.getVolumenSonido() : configuracion.getVolumenSonido()
            );
        }

        configuracionPantallaService.configurarApariencia(configuracion.getId());

        // Obtener configuración actualizada
        configuracion = configuracionPantallaService.obtenerConfiguracionActiva()
                .orElse(configuracion);

        ConfiguracionPantallaResponse response = ConfiguracionPantallaMapper.toResponse(configuracion);

        log.info("Configuración de pantalla creada exitosamente: {} - ID: {}",
                configuracion.getNombre(), configuracion.getId());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Configuración creada exitosamente")
        );
    }

    //Actualiza una configuración existente
    //PUT /api/configuraciones-pantalla/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<ConfiguracionPantallaResponse>> actualizarConfiguracion(
            @PathVariable Long id,
            @Valid @RequestBody ConfiguracionPantallaRequest request) {

        log.info("Actualizando configuración de pantalla ID: {}", id);

        // Verificar que existe
        ConfiguracionPantalla configuracionExistente = configuracionPantallaService.obtenerConfiguracionActiva()
                .orElseThrow(() -> ResourceNotFoundException.configuracionPantalla(id));

        // Actualizar configuración básica
        ConfiguracionPantalla configuracionActualizada = configuracionPantallaService.actualizar(
                id,
                request.getNombre(),
                request.getTiempoMensaje(),
                request.getTiempoTurno(),
                request.getTextoEncabezado()
        );

        // Configurar sonido
        if (request.getSonidoActivo() != null ||
                request.getArchivoSonido() != null ||
                request.getVolumenSonido() != null) {

            configuracionPantallaService.configurarSonido(
                    id,
                    request.getSonidoActivo() != null ? request.getSonidoActivo() : configuracionActualizada.getSonidoActivo(),
                    request.getArchivoSonido(),
                    request.getVolumenSonido() != null ? request.getVolumenSonido() : configuracionActualizada.getVolumenSonido()
            );
        }

        configuracionPantallaService.configurarApariencia(id);


        // Obtener configuración final actualizada
        configuracionActualizada = configuracionPantallaService.obtenerConfiguracionActiva()
                .orElse(configuracionActualizada);

        ConfiguracionPantallaResponse response = ConfiguracionPantallaMapper.toResponse(configuracionActualizada);

        log.info("Configuración de pantalla actualizada exitosamente: {}", configuracionActualizada.getNombre());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Configuración actualizada exitosamente")
        );
    }

    // ==========================================
    // ENDPOINTS DE GESTIÓN DE ESTADO
    // ==========================================

    //Activa una configuración (desactiva las demás)
    //PUT /api/configuraciones-pantalla/{id}/activar
    @PutMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<ConfiguracionPantallaResponse>> activarConfiguracion(@PathVariable Long id) {

        log.info("Activando configuración de pantalla ID: {}", id);

        configuracionPantallaService.activar(id);

        // Obtener configuración activada
        ConfiguracionPantalla configuracion = configuracionPantallaService.obtenerConfiguracionActiva()
                .orElseThrow(() -> ResourceNotFoundException.configuracionPantalla(id));

        ConfiguracionPantallaResponse response = ConfiguracionPantallaMapper.toResponse(configuracion);

        log.info("Configuración de pantalla activada: {}", configuracion.getNombre());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Configuración activada exitosamente")
        );
    }

    // ==========================================
    // ENDPOINTS DE CONFIGURACIÓN ESPECÍFICA
    // ==========================================

    //Configura el sonido de una configuración
    //PUT /api/configuraciones-pantalla/{id}/sonido
    @PutMapping("/{id}/sonido")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<String>> configurarSonido(
            @PathVariable Long id,
            @Valid @RequestBody ConfigurarSonidoRequest request) {

        log.info("Configurando sonido para configuración ID: {} - Activo: {}", id, request.getActivo());

        configuracionPantallaService.configurarSonido(
                id,
                request.getActivo(),
                request.getArchivo(),
                request.getVolumen()
        );

        String mensaje = request.getActivo()
                ? "Sonido configurado y activado exitosamente"
                : "Sonido desactivado exitosamente";

        log.debug("Sonido configurado para configuración ID: {}", id);

        return ResponseEntity.ok(
                ApiResponseWrapper.success("OK", mensaje)
        );
    }

}