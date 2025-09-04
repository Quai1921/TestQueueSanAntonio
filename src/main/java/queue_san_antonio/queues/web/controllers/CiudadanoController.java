package queue_san_antonio.queues.web.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import queue_san_antonio.queues.models.Ciudadano;
import queue_san_antonio.queues.services.CiudadanoService;
import queue_san_antonio.queues.services.TurnoService;
import queue_san_antonio.queues.web.dto.ciudadano.CiudadanoRequest;
import queue_san_antonio.queues.web.dto.ciudadano.CiudadanoResponse;
import queue_san_antonio.queues.web.dto.ciudadano.CiudadanoSummaryResponse;
import queue_san_antonio.queues.web.dto.ciudadano.EstablecerPrioridadRequest;
import queue_san_antonio.queues.web.dto.common.ApiResponseWrapper;
import queue_san_antonio.queues.web.dto.mapper.CiudadanoMapper;
import queue_san_antonio.queues.web.exceptions.custom.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

//Controlador REST para la gestión de ciudadanos
//Proporciona endpoints para CRUD y operaciones específicas de ciudadanos
@RestController
@RequestMapping("/api/ciudadanos")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CiudadanoController {

    private final CiudadanoService ciudadanoService;
    private final TurnoService turnoService;

    // ==========================================
    // ENDPOINTS DE CONSULTA
    // ==========================================

    //Busca un ciudadano por DNI
    //GET /api/ciudadanos/dni/{dni}
    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<CiudadanoResponse>> buscarPorDni(
            @PathVariable
            @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener entre 7 y 8 dígitos")
            String dni) {

        log.debug("Buscando ciudadano por DNI: {}", dni);

        Optional<Ciudadano> ciudadano = ciudadanoService.buscarPorDni(dni);

        if (ciudadano.isEmpty()) {
            throw ResourceNotFoundException.ciudadano(dni);
        }

        CiudadanoResponse response = CiudadanoMapper.toResponse(ciudadano.get());

        // Verificar si tiene turnos pendientes
        boolean tieneTurnoPendiente = turnoService.ciudadanoTieneTurnoPendiente(ciudadano.get().getId());
        response.setTieneTurnoPendiente(tieneTurnoPendiente);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Ciudadano encontrado")
        );
    }

    //Busca ciudadanos por DNI o apellido (para formularios)
    //GET /api/ciudadanos/search?dni={dni}&apellido={apellido}
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<CiudadanoSummaryResponse>>> buscarCiudadanos(
            @RequestParam(required = false)
            @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener entre 7 y 8 dígitos")
            String dni,

            @RequestParam(required = false)
            String apellido) {

        log.debug("Buscando ciudadanos por DNI: {} o apellido: {}", dni, apellido);

        // Validar que al menos un parámetro esté presente
        if ((dni == null || dni.trim().isEmpty()) &&
                (apellido == null || apellido.trim().isEmpty())) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("Debe proporcionar DNI o apellido para la búsqueda", "MISSING_SEARCH_PARAMS")
            );
        }

        List<Ciudadano> ciudadanos;

        if (dni != null && !dni.trim().isEmpty()) {
            // Búsqueda combinada por DNI y apellido
            ciudadanos = ciudadanoService.buscarPorDniOApellido(dni, apellido);
        } else {
            // Búsqueda solo por apellido
            ciudadanos = ciudadanoService.buscarPorApellido(apellido);
        }

        List<CiudadanoSummaryResponse> response = CiudadanoMapper.toSummaryResponseList(ciudadanos);

        // Agregar información de turnos pendientes para cada ciudadano
        response.forEach(ciudadanoResponse -> {
            Optional<Ciudadano> ciudadanoEntity = ciudadanos.stream()
                    .filter(c -> c.getDni().equals(ciudadanoResponse.getDni()))
                    .findFirst();

            if (ciudadanoEntity.isPresent()) {
                boolean tieneTurnoPendiente = turnoService.ciudadanoTieneTurnoPendiente(ciudadanoEntity.get().getId());
                ciudadanoResponse.setTieneTurnoPendiente(tieneTurnoPendiente);
            }
        });

        String mensaje = ciudadanos.isEmpty()
                ? "No se encontraron ciudadanos con los criterios especificados"
                : String.format("Se encontraron %d ciudadano(s)", ciudadanos.size());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, mensaje)
        );
    }

    //Verifica si existe un ciudadano por DNI
    //GET /api/ciudadanos/existe/{dni}
    @GetMapping("/existe/{dni}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<Boolean>> existeCiudadano(
            @PathVariable
            @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener entre 7 y 8 dígitos")
            String dni) {

        log.debug("Verificando existencia de ciudadano con DNI: {}", dni);

        boolean existe = ciudadanoService.existePorDni(dni);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(existe,
                        existe ? "El ciudadano existe" : "El ciudadano no existe")
        );
    }

    //Lista todos los ciudadanos (ordenados por apellido)
    //GET /api/ciudadanos
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<CiudadanoSummaryResponse>>> listarTodos() {

        log.debug("Listando todos los ciudadanos");

        List<Ciudadano> ciudadanos = ciudadanoService.listarTodos();
        List<CiudadanoSummaryResponse> response = CiudadanoMapper.toSummaryResponseList(ciudadanos);

        // Agregar información de turnos pendientes para cada ciudadano
        response.forEach(ciudadanoResponse -> {
            Optional<Ciudadano> ciudadanoEntity = ciudadanos.stream()
                    .filter(c -> c.getDni().equals(ciudadanoResponse.getDni()))
                    .findFirst();

            if (ciudadanoEntity.isPresent()) {
                boolean tieneTurnoPendiente = turnoService.ciudadanoTieneTurnoPendiente(ciudadanoEntity.get().getId());
                ciudadanoResponse.setTieneTurnoPendiente(tieneTurnoPendiente);
            }
        });

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d ciudadanos", ciudadanos.size()))
        );
    }




    // ==========================================
    // ENDPOINTS DE CREACIÓN Y ACTUALIZACIÓN
    // ==========================================

    //Crea un nuevo ciudadano
    //POST /api/ciudadanos
    @PostMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<CiudadanoResponse>> crearCiudadano(
            @Valid @RequestBody CiudadanoRequest request) {

        log.info("Creando nuevo ciudadano con DNI: {}", request.getDni());

        // Verificar que no exista un ciudadano con el mismo DNI
        if (ciudadanoService.existePorDni(request.getDni())) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("Ya existe un ciudadano con DNI: " + request.getDni(), "DUPLICATE_DNI")
            );
        }

        Ciudadano ciudadano = CiudadanoMapper.toEntity(request);
        Ciudadano ciudadanoGuardado = ciudadanoService.guardar(ciudadano);

        CiudadanoResponse response = CiudadanoMapper.toResponse(ciudadanoGuardado);
        response.setTieneTurnoPendiente(false); // Es nuevo, no tiene turnos

        log.info("Ciudadano creado exitosamente: {} - {}",
                ciudadanoGuardado.getDni(), ciudadanoGuardado.getNombreCompleto());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Ciudadano creado exitosamente")
        );
    }

    //Actualiza un ciudadano existente
    //PUT /api/ciudadanos/{dni}
    @PutMapping("/{dni}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<CiudadanoResponse>> actualizarCiudadano(
            @PathVariable
            @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener entre 7 y 8 dígitos")
            String dni,

            @Valid @RequestBody CiudadanoRequest request) {

        log.info("Actualizando ciudadano con DNI: {}", dni);

        // Buscar ciudadano existente
        Ciudadano ciudadano = ciudadanoService.buscarPorDni(dni)
                .orElseThrow(() -> ResourceNotFoundException.ciudadano(dni));

        // Actualizar datos usando el mapper
        CiudadanoMapper.updateEntity(ciudadano, request);

        Ciudadano ciudadanoActualizado = ciudadanoService.guardar(ciudadano);

        CiudadanoResponse response = CiudadanoMapper.toResponse(ciudadanoActualizado);

        // Verificar turnos pendientes
        boolean tieneTurnoPendiente = turnoService.ciudadanoTieneTurnoPendiente(ciudadano.getId());
        response.setTieneTurnoPendiente(tieneTurnoPendiente);

        log.info("Ciudadano actualizado exitosamente: {} - {}",
                ciudadanoActualizado.getDni(), ciudadanoActualizado.getNombreCompleto());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Ciudadano actualizado exitosamente")
        );
    }

    //Crea o actualiza un ciudadano (método helper para generar turnos)
    //POST /api/ciudadanos/crear-o-actualizar
    @PostMapping("/crear-o-actualizar")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<CiudadanoResponse>> crearOActualizarCiudadano(
            @Valid @RequestBody CiudadanoRequest request) {

        log.info("Creando o actualizando ciudadano con DNI: {}", request.getDni());

        // Usar el método del service que maneja la lógica
        Ciudadano ciudadano = ciudadanoService.crearOActualizar(
                request.getDni(),
                request.getNombre(),
                request.getApellido(),
                request.getTelefono(),
                request.getDireccion(),
                request.getObservaciones()
        );

        // Si se especifica prioridad en el request, aplicarla
        if (request.getEsPrioritario() != null && request.getEsPrioritario()) {
            ciudadanoService.establecerPrioridad(
                    ciudadano.getId(),
                    true,
                    request.getMotivoPrioridad()
            );
            // Recargar para obtener los datos actualizados
            ciudadano = ciudadanoService.buscarPorId(ciudadano.getId()).orElse(ciudadano);
        }

        CiudadanoResponse response = CiudadanoMapper.toResponse(ciudadano);

        // Verificar turnos pendientes
        boolean tieneTurnoPendiente = turnoService.ciudadanoTieneTurnoPendiente(ciudadano.getId());
        response.setTieneTurnoPendiente(tieneTurnoPendiente);

        log.info("Ciudadano procesado exitosamente: {} - {}",
                ciudadano.getDni(), ciudadano.getNombreCompleto());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Ciudadano procesado exitosamente")
        );
    }

    // ==========================================
    // ENDPOINTS DE PRIORIDAD
    // ==========================================

    //Establece o quita la prioridad de un ciudadano
    //PUT /api/ciudadanos/{dni}/prioridad
    @PutMapping("/{dni}/prioridad")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<CiudadanoResponse>> establecerPrioridad(
            @PathVariable
            @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener entre 7 y 8 dígitos")
            String dni,

            @Valid @RequestBody EstablecerPrioridadRequest request) {

        log.info("Estableciendo prioridad para ciudadano DNI: {} - Prioritario: {}",
                dni, request.getEsPrioritario());

        // Buscar ciudadano
        Ciudadano ciudadano = ciudadanoService.buscarPorDni(dni)
                .orElseThrow(() -> ResourceNotFoundException.ciudadano(dni));

        // Establecer prioridad usando el service
        ciudadanoService.establecerPrioridad(
                ciudadano.getId(),
                request.getEsPrioritario(),
                request.getMotivo()
        );

        // Recargar datos actualizados
        Ciudadano ciudadanoActualizado = ciudadanoService.buscarPorId(ciudadano.getId())
                .orElse(ciudadano);

        CiudadanoResponse response = CiudadanoMapper.toResponse(ciudadanoActualizado);

        // Verificar turnos pendientes
        boolean tieneTurnoPendiente = turnoService.ciudadanoTieneTurnoPendiente(ciudadano.getId());
        response.setTieneTurnoPendiente(tieneTurnoPendiente);

        String mensaje = request.getEsPrioritario()
                ? "Prioridad establecida exitosamente"
                : "Prioridad removida exitosamente";

        log.info("Prioridad actualizada para ciudadano: {} - Prioritario: {} - Motivo: {}",
                dni, request.getEsPrioritario(), request.getMotivo());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, mensaje)
        );
    }
}