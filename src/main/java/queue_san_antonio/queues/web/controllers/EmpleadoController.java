package queue_san_antonio.queues.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import queue_san_antonio.queues.models.Empleado;
import queue_san_antonio.queues.models.RolEmpleado;
import queue_san_antonio.queues.models.Sector;
import queue_san_antonio.queues.services.EmpleadoService;
import queue_san_antonio.queues.services.SectorService;
import queue_san_antonio.queues.web.dto.common.ApiResponseWrapper;
import queue_san_antonio.queues.web.dto.empleado.*;
import queue_san_antonio.queues.web.dto.mapper.EmpleadoMapper;
import queue_san_antonio.queues.web.dto.mapper.SectorMapper;
import queue_san_antonio.queues.web.exceptions.custom.ResourceNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

//Controlador REST para la gestión de empleados
//Solo accesible para administradores
@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final SectorService sectorService;

    // ==========================================
    // ENDPOINTS DE CONSULTA
    // ==========================================

    //Lista todos los empleados
    //GET /api/empleados
    @GetMapping
    public ResponseEntity<ApiResponseWrapper<List<EmpleadoSummaryResponse>>> listarTodos() {

        log.debug("Listando todos los empleados");

        List<Empleado> empleados = empleadoService.listarTodos();
        List<EmpleadoSummaryResponse> response = EmpleadoMapper.toSummaryResponseList(empleados);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d empleados", empleados.size()))
        );
    }

    //Lista empleados por rol
    //GET /api/empleados/rol/{rol}
    @GetMapping("/rol/{rol}")
    public ResponseEntity<ApiResponseWrapper<List<EmpleadoSummaryResponse>>> listarPorRol(
            @PathVariable RolEmpleado rol) {

        log.debug("Listando empleados por rol: {}", rol);

        List<Empleado> empleados = empleadoService.listarPorRol(rol);
        List<EmpleadoSummaryResponse> response = EmpleadoMapper.toSummaryResponseList(empleados);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d empleados con rol %s", empleados.size(), rol))
        );
    }

    //Lista empleados por sector
    //GET /api/empleados/sector/{sectorId}
    @GetMapping("/sector/{sectorId}")
    public ResponseEntity<ApiResponseWrapper<List<EmpleadoSummaryResponse>>> listarPorSector(
            @PathVariable Long sectorId) {

        log.debug("Listando empleados del sector: {}", sectorId);

        // Validar que existe el sector
        Sector sector = sectorService.buscarPorId(sectorId)
                .orElseThrow(() -> ResourceNotFoundException.sector(sectorId));

        List<Empleado> empleados = empleadoService.listarPorSector(sectorId);
        List<EmpleadoSummaryResponse> response = EmpleadoMapper.toSummaryResponseList(empleados);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d empleados en el sector %s",
                                empleados.size(), sector.getCodigo()))
        );
    }

    //Busca un empleado por ID
    //GET /api/empleados/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<EmpleadoResponse>> buscarPorId(@PathVariable Long id) {

        log.debug("Buscando empleado por ID: {}", id);

        Empleado empleado = empleadoService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.empleado(id));

        EmpleadoResponse response = EmpleadoMapper.toResponse(empleado);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Empleado encontrado")
        );
    }

    //Busca un empleado por username
    //GET /api/empleados/username/{username}
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponseWrapper<EmpleadoResponse>> buscarPorUsername(
            @PathVariable String username) {

        log.debug("Buscando empleado por username: {}", username);

        Empleado empleado = empleadoService.buscarPorUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.empleado(username));

        EmpleadoResponse response = EmpleadoMapper.toResponse(empleado);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Empleado encontrado")
        );
    }

    //Verifica si existe un username
    //GET /api/empleados/existe-username/{username}
    @GetMapping("/existe-username/{username}")
    public ResponseEntity<ApiResponseWrapper<Boolean>> existeUsername(@PathVariable String username) {

        log.debug("Verificando existencia de username: {}", username);

        boolean existe = empleadoService.existePorUsername(username);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(existe,
                        existe ? "El username existe" : "El username está disponible")
        );
    }

    //Verifica si existe un DNI
    //GET /api/empleados/existe-dni/{dni}
    @GetMapping("/existe-dni/{dni}")
    public ResponseEntity<ApiResponseWrapper<Boolean>> existeDni(@PathVariable String dni) {

        log.debug("Verificando existencia de DNI: {}", dni);

        boolean existe = empleadoService.existePorDni(dni);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(existe,
                        existe ? "El DNI existe" : "El DNI está disponible")
        );
    }

    // ==========================================
    // ENDPOINTS DE GESTIÓN
    // ==========================================

    //Crea un nuevo empleado
    //POST /api/empleados
    @PostMapping
    public ResponseEntity<ApiResponseWrapper<EmpleadoResponse>> crearEmpleado(
            @Valid @RequestBody EmpleadoRequest request) {

        log.info("Creando nuevo empleado con username: {}", request.getUsername());

        // Validar que es creación (tiene password)
        if (!request.esCreacion()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("La contraseña es obligatoria para crear empleados", "PASSWORD_REQUIRED")
            );
        }

        // Verificar username único
        if (empleadoService.existePorUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("Ya existe un empleado con username: " + request.getUsername(), "DUPLICATE_USERNAME")
            );
        }

        // Verificar DNI único si se proporciona
        if (request.getDni() != null && empleadoService.existePorDni(request.getDni())) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("Ya existe un empleado con DNI: " + request.getDni(), "DUPLICATE_DNI")
            );
        }

        // Verificar email único si se proporciona
        if (request.getEmail() != null && empleadoService.existePorEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("Ya existe un empleado con email: " + request.getEmail(), "DUPLICATE_EMAIL")
            );
        }

        try {
            // Usar el método del service que maneja toda la lógica
            Empleado empleado = empleadoService.crear(
                    request.getUsername(),
                    request.getPassword(),
                    request.getNombre(),
                    request.getApellido(),
                    request.getEmail(),
                    request.getDni(),
                    request.getRol(),
                    request.getSectorId()
            );

            EmpleadoResponse response = EmpleadoMapper.toResponse(empleado);

            log.info("Empleado creado exitosamente: {} - {}",
                    empleado.getUsername(), empleado.getNombreCompleto());

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response, "Empleado creado exitosamente")
            );

        } catch (IllegalArgumentException e) {
            log.warn("Error creando empleado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error(e.getMessage(), "CREATION_ERROR")
            );
        }
    }

    //Actualiza un empleado existente
    //PUT /api/empleados/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<EmpleadoResponse>> actualizarEmpleado(
            @PathVariable Long id,
            @Valid @RequestBody EmpleadoUpdateRequest request) {

        log.info("Actualizando empleado ID: {}", id);

        // Buscar empleado existente
        Empleado empleado = empleadoService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.empleado(id));

        // GUARDAR EL ROL ANTERIOR ANTES DE ACTUALIZAR
        RolEmpleado rolAnterior = empleado.getRol();

        // Verificar DNI único si cambió
        if (request.getDni() != null &&
                !request.getDni().equals(empleado.getDni()) &&
                empleadoService.existePorDni(request.getDni())) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("Ya existe un empleado con DNI: " + request.getDni(), "DUPLICATE_DNI")
            );
        }

        try {

            // Actualizar solo campos permitidos
            empleado.setNombre(request.getNombreLimpio());
            empleado.setApellido(request.getApellidoLimpio());
            empleado.setEmail(request.getEmailLimpio());
            empleado.setDni(request.getDniLimpio());
            empleado.setRol(request.getRol());

            // MANEJAR CAMBIO DE ROL SI ES NECESARIO
            if (rolAnterior != request.getRol()) {
                log.info("Cambio de rol detectado para empleado {}: {} -> {}",
                        empleado.getUsername(), rolAnterior, request.getRol());
                manejarCambioDeRol(empleado, rolAnterior, request.getRol());
            }

            // Manejar sector
            if (request.getSectorId() != null) {
                empleadoService.asignarASector(id, request.getSectorId());
            }

            Empleado empleadoActualizado = empleadoService.guardar(empleado);
            EmpleadoResponse response = EmpleadoMapper.toResponse(empleadoActualizado);

            log.info("Empleado actualizado: {}", empleado.getUsername());

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response, "Empleado actualizado exitosamente")
            );

        } catch (Exception e) {
            log.warn("Error actualizando empleado {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error(e.getMessage(), "UPDATE_ERROR")
            );
        }
    }

    //Cambia la contraseña de un empleado
    //PATCH /api/empleados/{id}/password
    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponseWrapper<String>> cambiarPassword(
            @PathVariable Long id,
            @Valid @RequestBody CambiarPasswordRequest request) {

        log.info("Cambiando contraseña del empleado ID: {}", id);

        // Verificar que existe el empleado
        Empleado empleado = empleadoService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.empleado(id));

        // Validar que las contraseñas coinciden
        if (!request.passwordsCoinciden()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("Las contraseñas no coinciden", "PASSWORDS_MISMATCH")
            );
        }

        try {
            empleadoService.cambiarPassword(id, request.getNuevaPasswordLimpia());

            log.info("Contraseña cambiada exitosamente para empleado: {}", empleado.getUsername());

            return ResponseEntity.ok(
                    ApiResponseWrapper.success("Contraseña cambiada exitosamente",
                            "Contraseña actualizada para " + empleado.getUsername())
            );

        } catch (Exception e) {
            log.warn("Error cambiando contraseña del empleado {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error(e.getMessage(), "PASSWORD_CHANGE_ERROR")
            );
        }
    }

    //Asigna un empleado a un sector
    //POST /api/empleados/{id}/asignar-sector
    @PostMapping("/{id}/asignar-sector")
    public ResponseEntity<ApiResponseWrapper<EmpleadoResponse>> asignarSector(
            @PathVariable Long id,
            @Valid @RequestBody AsignarSectorRequest request) {

        log.info("Asignando empleado ID: {} al sector: {}", id, request.getSectorId());

        // Verificar que existe el empleado
        Empleado empleado = empleadoService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.empleado(id));

        // Verificar que existe el sector si no es desasignación
        if (!request.esDesasignacion()) {
            Sector sector = sectorService.buscarPorId(request.getSectorId())
                    .orElseThrow(() -> ResourceNotFoundException.sector(request.getSectorId()));

            log.debug("Asignando empleado {} al sector {}", empleado.getUsername(), sector.getCodigo());
        } else {
            log.debug("Desasignando empleado {} de sector", empleado.getUsername());
        }

        try {
            empleadoService.asignarASector(id, request.getSectorId());

            // Obtener datos actualizados
            Empleado empleadoActualizado = empleadoService.buscarPorId(id).orElse(empleado);
            EmpleadoResponse response = EmpleadoMapper.toResponse(empleadoActualizado);

            String mensaje = request.esDesasignacion() ?
                    "Empleado desasignado de sector exitosamente" :
                    "Empleado asignado a sector exitosamente";

            log.info("Asignación completada para empleado: {}", empleado.getUsername());

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response, mensaje)
            );

        } catch (Exception e) {
            log.warn("Error asignando sector al empleado {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error(e.getMessage(), "SECTOR_ASSIGNMENT_ERROR")
            );
        }
    }

    //Activa un empleado
    //PATCH /api/empleados/{id}/activar
    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponseWrapper<EmpleadoResponse>> activarEmpleado(@PathVariable Long id) {

        log.info("Activando empleado ID: {}", id);

        // Verificar que existe el empleado
        Empleado empleado = empleadoService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.empleado(id));

        empleadoService.activar(id);

        // Obtener datos actualizados
        Empleado empleadoActualizado = empleadoService.buscarPorId(id).orElse(empleado);
        EmpleadoResponse response = EmpleadoMapper.toResponse(empleadoActualizado);

        log.info("Empleado activado: {}", empleado.getUsername());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Empleado activado exitosamente")
        );
    }

    //Desactiva un empleado
    //PATCH /api/empleados/{id}/desactivar
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponseWrapper<EmpleadoResponse>> desactivarEmpleado(@PathVariable Long id) {

        log.info("Desactivando empleado ID: {}", id);

        // Verificar que existe el empleado
        Empleado empleado = empleadoService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.empleado(id));

        empleadoService.desactivar(id);

        // Obtener datos actualizados
        Empleado empleadoActualizado = empleadoService.buscarPorId(id).orElse(empleado);
        EmpleadoResponse response = EmpleadoMapper.toResponse(empleadoActualizado);

        log.info("Empleado desactivado: {}", empleado.getUsername());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Empleado desactivado exitosamente")
        );
    }

    // ==========================================
    // ENDPOINTS DE UTILIDADES
    // ==========================================

    //Obtiene estadísticas básicas de empleados
    //GET /api/empleados/estadisticas
    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponseWrapper<EstadisticasEmpleadosResponse>> obtenerEstadisticas() {

        log.debug("Obteniendo estadísticas de empleados");

        List<Empleado> todosEmpleados = empleadoService.listarTodos();

        long totalEmpleados = todosEmpleados.size();
        long empleadosActivos = todosEmpleados.stream().filter(Empleado::puedeAcceder).count();
        long administradores = todosEmpleados.stream().filter(Empleado::esAdministrador).count();
        long responsables = todosEmpleados.stream().filter(Empleado::esResponsable).count();
        long empleadosComunes = todosEmpleados.stream()
                .filter(e -> e.getRol() == RolEmpleado.OPERADOR).count();

        EstadisticasEmpleadosResponse estadisticas = EstadisticasEmpleadosResponse.builder()
                .totalEmpleados((int) totalEmpleados)
                .empleadosActivos((int) empleadosActivos)
                .empleadosInactivos((int) (totalEmpleados - empleadosActivos))
                .administradores((int) administradores)
                .responsables((int) responsables)
                .empleadosComunes((int) empleadosComunes)
                .build();

        return ResponseEntity.ok(
                ApiResponseWrapper.success(estadisticas, "Estadísticas de empleados obtenidas")
        );
    }









    /**
     * Obtiene el personal asignado a un sector específico
     * GET /api/empleados/por-sector/{sectorId}
     */
    @GetMapping("/por-sector/{sectorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<Map<String, Object>>> obtenerPersonalPorSector(
            @PathVariable Long sectorId) {

        log.debug("Obteniendo personal del sector ID: {}", sectorId);

        try {
            // Verificar que el sector existe
            Sector sector = sectorService.buscarPorId(sectorId)
                    .orElseThrow(() -> ResourceNotFoundException.sector(sectorId));

            // Buscar responsable del sector
            Optional<Empleado> responsable = empleadoService.buscarResponsablePorSector(sectorId);

            // Buscar operadores del sector
            List<Empleado> operadores = empleadoService.buscarOperadoresPorSector(sectorId);

            // Construir respuesta
            Map<String, Object> personal = new HashMap<>();
            personal.put("responsable", responsable.map(EmpleadoMapper::toResponse).orElse(null));
            personal.put("operadores", EmpleadoMapper.toResponseList(operadores));
            personal.put("sector", SectorMapper.toResponse(sector));

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(personal,
                            String.format("Personal del sector %s obtenido exitosamente", sector.getCodigo()))
            );

        } catch (Exception e) {
            log.error("Error obteniendo personal del sector {}: {}", sectorId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseWrapper.error("Error interno obteniendo personal del sector"));
        }
    }

    /**
     * Obtiene operadores disponibles (sin sector asignado)
     * GET /api/empleados/operadores-disponibles
     */
    @GetMapping("/operadores-disponibles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<EmpleadoResponse>>> obtenerOperadoresDisponibles() {

        log.debug("Obteniendo operadores disponibles (sin sector asignado)");

        try {
            List<Empleado> operadores = empleadoService.buscarOperadoresSinSector();
            List<EmpleadoResponse> response = EmpleadoMapper.toResponseList(operadores);

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response,
                            String.format("Se encontraron %d operadores disponibles", operadores.size()))
            );

        } catch (Exception e) {
            log.error("Error obteniendo operadores disponibles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseWrapper.error("Error interno obteniendo operadores disponibles"));
        }
    }

    /**
     * Obtiene empleados por rol específico
     * GET /api/empleados/por-rol/{rol}
     */
    @GetMapping("/por-rol/{rol}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<EmpleadoResponse>>> obtenerEmpleadosPorRol(
            @PathVariable String rol) {

        log.debug("Obteniendo empleados con rol: {}", rol);

        try {
            // Validar que el rol existe
            RolEmpleado rolEnum;
            try {
                rolEnum = RolEmpleado.valueOf(rol.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseWrapper.error("Rol inválido: " + rol));
            }

            List<Empleado> empleados = empleadoService.buscarPorRol(rolEnum);
            List<EmpleadoResponse> response = EmpleadoMapper.toResponseList(empleados);

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response,
                            String.format("Se encontraron %d empleados con rol %s",
                                    empleados.size(), rol))
            );

        } catch (Exception e) {
            log.error("Error obteniendo empleados por rol {}: {}", rol, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseWrapper.error("Error interno obteniendo empleados por rol"));
        }
    }












    private void manejarCambioDeRol(Empleado empleado, RolEmpleado rolAnterior, RolEmpleado nuevoRol) {
        switch (nuevoRol) {
            case ADMIN -> {
                // Admin no necesita sector, desasignar cualquier sector
                if (empleado.getSector() != null) {
                    log.info("Desasignando sector al empleado {} que pasa a ADMIN", empleado.getUsername());
                    empleado.asignarASector(null);
                }
                // Si era responsable, remover de sectores
                if (rolAnterior == RolEmpleado.RESPONSABLE_SECTOR) {
                    removerDeResponsabilidades(empleado);
                }
            }
            case RESPONSABLE_SECTOR -> {
                // Si era operador, desasignar sector actual
                if (rolAnterior == RolEmpleado.OPERADOR && empleado.getSector() != null) {
                    log.info("Desasignando sector al empleado {} que pasa de OPERADOR a RESPONSABLE_SECTOR",
                            empleado.getUsername());
                    empleado.asignarASector(null);
                }
            }
            case OPERADOR -> {
                // Si era responsable, remover de sectores donde es responsable
                if (rolAnterior == RolEmpleado.RESPONSABLE_SECTOR) {
                    removerDeResponsabilidades(empleado);
                    // También desasignar sector actual
                    empleado.asignarASector(null);
                }
            }
        }
    }

    // AGREGAR ESTE MÉTODO PRIVADO AL CONTROLLER
    private void removerDeResponsabilidades(Empleado empleado) {
        log.info("Removiendo responsabilidades del empleado {} en sectores", empleado.getUsername());

        // Buscar sectores donde es responsable
        List<Sector> sectoresResponsable = sectorService.listarTodos()
                .stream()
                .filter(s -> s.getResponsable() != null &&
                        s.getResponsable().getId().equals(empleado.getId()))
                .collect(Collectors.toList());

        for (Sector sector : sectoresResponsable) {
            log.info("Removiendo a {} como responsable del sector {}",
                    empleado.getUsername(), sector.getCodigo());
            sector.establecerResponsable(null);
            sectorService.guardar(sector);
        }
    }

    // ==========================================
    // DTO PARA ESTADÍSTICAS
    // ==========================================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EstadisticasEmpleadosResponse {
        private Integer totalEmpleados;
        private Integer empleadosActivos;
        private Integer empleadosInactivos;
        private Integer administradores;
        private Integer responsables;
        private Integer empleadosComunes;
    }
}