package queue_san_antonio.queues.web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import queue_san_antonio.queues.models.*;
import queue_san_antonio.queues.security.jwt.JwtService;
import queue_san_antonio.queues.services.CiudadanoService;
import queue_san_antonio.queues.services.EmpleadoService;
import queue_san_antonio.queues.services.SectorService;
import queue_san_antonio.queues.services.TurnoService;
import queue_san_antonio.queues.web.dto.common.ApiResponseWrapper;
import queue_san_antonio.queues.web.dto.mapper.TurnoMapper;
import queue_san_antonio.queues.web.dto.turno.*;
import queue_san_antonio.queues.web.exceptions.custom.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

//Controlador REST para la gestión completa de turnos
//Incluye generación, consultas públicas y operaciones de empleados
@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
@Slf4j
@Validated
public class TurnoController {

    private final TurnoService turnoService;
    private final CiudadanoService ciudadanoService;
    private final SectorService sectorService;
    private final JwtService jwtService;
    private final EmpleadoService empleadoService;

    // ==========================================
    // ENDPOINTS PÚBLICOS (SIN AUTENTICACIÓN)
    // ==========================================

    //Consulta pública de turno por código
    //GET /api/turnos/codigo/{codigo}
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ApiResponseWrapper<TurnoResponse>> consultarTurnoPorCodigo(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{2,10}[0-9]{3}$", message = "Código de turno inválido")
            String codigo) {

        log.debug("Consulta pública de turno por código: {}", codigo);

        Turno turno = turnoService.buscarPorCodigo(codigo)
                .orElseThrow(() -> ResourceNotFoundException.turno(codigo));

        TurnoResponse response = TurnoMapper.toResponse(turno);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Turno %s encontrado (último generado)", codigo))
        );
    }

    //Consulta pública de turno por código y fecha específica
    //GET /api/turnos/codigo/{codigo}/fecha/{fecha}
    @GetMapping("/codigo/{codigo}/fecha/{fecha}")
    public ResponseEntity<ApiResponseWrapper<TurnoResponse>> consultarTurnoPorCodigoYFecha(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{2,10}[0-9]{3}$", message = "Código de turno inválido")
            String codigo,
            @PathVariable
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate fecha) {

        log.debug("Consulta pública de turno por código: {} y fecha: {}", codigo, fecha);

        Turno turno = turnoService.buscarPorCodigoYFecha(codigo, fecha)
                .orElseThrow(() -> ResourceNotFoundException.turno(codigo + " en fecha " + fecha));

        TurnoResponse response = TurnoMapper.toResponse(turno);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Turno %s encontrado para la fecha %s", codigo, fecha))
        );
    }


    // ==========================================
    // ENDPOINTS DE GENERACIÓN (EMPLEADOS)
    // ==========================================

    //Genera un turno (normal o especial según datos proporcionados)
    //POST /api/turnos/generar
    @PostMapping("/generar")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<TurnoResponse>> generarTurno(
            @Valid @RequestBody GenerarTurnoRequest request,
            Authentication authentication) {

        String tipoTurno = request.esEspecial() ? "especial" : "normal";
        String citaInfo = request.esEspecial() ?
                String.format(" - Cita: %s %s", request.getFechaCita(), request.getHoraCita()) : "";

        log.info("Generando turno {} para DNI: {} en sector: {}{}",
                tipoTurno, request.getDni(), request.getSectorId(), citaInfo);

        try {
            Long empleadoId = obtenerEmpleadoIdDesdeAuth(authentication);

            // Procesar ciudadano (crear o actualizar)
            Ciudadano ciudadano = procesarCiudadano(request);

            // Validaciones específicas para turnos especiales
            if (request.esEspecial()) {
                Sector sector = sectorService.buscarPorId(request.getSectorId())
                        .orElseThrow(() -> ResourceNotFoundException.sector(request.getSectorId()));

                if (!sector.esEspecial()) {
                    return ResponseEntity.badRequest().body(
                            ApiResponseWrapper.error("El sector no acepta turnos especiales con cita previa", "SECTOR_NOT_SPECIAL")
                    );
                }
            }

            // Generar turno según tipo
            Turno turno;
            if (request.esEspecial()) {
                turno = turnoService.generarTurnoEspecial(
                        ciudadano.getId(),
                        request.getSectorId(),
                        request.getFechaCita(),
                        request.getHoraCita(),
                        empleadoId
                );
            } else {
                turno = turnoService.generarTurno(
                        ciudadano.getId(),
                        request.getSectorId(),
                        request.getTipo(),
                        empleadoId
                );
            }

            TurnoResponse response = TurnoMapper.toResponse(turno);

            String mensaje = request.esEspecial() ?
                    String.format("Turno especial generado exitosamente con cita para %s a las %s",
                            request.getFechaCita(), request.getHoraCita()) :
                    "Turno generado exitosamente";

            log.info("Turno {} generado exitosamente: {} para ciudadano {} en sector {}{}",
                    tipoTurno, turno.getCodigo(), ciudadano.getDni(), turno.getSector().getCodigo(), citaInfo);

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response, mensaje)
            );

        } catch (IllegalArgumentException e) {
            log.warn("Error en parámetros para generar turno {}: {}", tipoTurno, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error(e.getMessage(), "INVALID_PARAMETERS")
            );
        } catch (IllegalStateException e) {
            log.warn("Error de estado para generar turno {}: {}", tipoTurno, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error(e.getMessage(), "INVALID_STATE")
            );
        }
    }

    // ==========================================
    // ENDPOINTS DE CONSULTA (EMPLEADOS)
    // ==========================================

    //Busca un turno por ID
    //GET /api/turnos/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<TurnoResponse>> buscarTurnoPorId(@PathVariable Long id) {

        log.debug("Buscando turno por ID: {}", id);

        Turno turno = turnoService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.turno(id));

        TurnoResponse response = TurnoMapper.toResponse(turno);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Turno encontrado")
        );
    }

    //Obtiene la cola de espera de un sector
    //GET /api/turnos/cola/{sectorId}
    @GetMapping("/cola/{sectorId}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<TurnoResponse>>> obtenerColaEspera(
            @PathVariable Long sectorId) {

        log.debug("Obteniendo cola de espera del sector: {}", sectorId);

        // Validar que existe el sector
        Sector sector = sectorService.buscarPorId(sectorId)
                .orElseThrow(() -> ResourceNotFoundException.sector(sectorId));

        List<Turno> cola = turnoService.obtenerColaEspera(sectorId);
        List<TurnoResponse> response = TurnoMapper.toResponseList(cola);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Cola de espera del sector %s: %d turnos",
                                sector.getCodigo(), cola.size()))
        );
    }

    //Obtiene el próximo turno a atender en un sector
    //GET /api/turnos/proximo/{sectorId}
    @GetMapping("/proximo/{sectorId}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<TurnoResponse>> obtenerProximoTurno(
            @PathVariable Long sectorId) {

        log.debug("Obteniendo próximo turno del sector: {}", sectorId);

        Sector sector = sectorService.buscarPorId(sectorId)
                .orElseThrow(() -> ResourceNotFoundException.sector(sectorId));

        return turnoService.obtenerProximoTurno(sectorId)
                .map(turno -> {
                    TurnoResponse response = TurnoMapper.toResponse(turno);

                    log.debug("Turno encontrado - ID: {}, Código: {}", turno.getId(), turno.getCodigo());

                    return ResponseEntity.ok(
                            ApiResponseWrapper.success(response, "Próximo turno encontrado")
                    );
                })
                .orElse(ResponseEntity.ok(
                        ApiResponseWrapper.success(null, "No hay turnos pendientes en el sector " + sector.getCodigo())
                ));
    }

    //Lista turnos de un ciudadano por DNI
    //GET /api/turnos/ciudadano/{dni}
    @GetMapping("/ciudadano/{dni}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<TurnoSummaryResponse>>> listarTurnosCiudadano(
            @PathVariable
            @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener entre 7 y 8 dígitos")
            String dni) {

        log.debug("Listando turnos del ciudadano DNI: {}", dni);

        Ciudadano ciudadano = ciudadanoService.buscarPorDni(dni)
                .orElseThrow(() -> ResourceNotFoundException.ciudadano(dni));

        List<Turno> turnos = turnoService.listarTurnosCiudadano(ciudadano.getId());
        List<TurnoSummaryResponse> response = TurnoMapper.toSummaryResponseList(turnos);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d turnos para el ciudadano %s",
                                turnos.size(), ciudadano.getNombreCompleto()))
        );
    }

    //Lista turnos de un sector en una fecha específica
    //GET /api/turnos/sector/{sectorId}/fecha/{fecha}
    @GetMapping("/sector/{sectorId}/fecha/{fecha}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<TurnoSummaryResponse>>> listarTurnosPorFecha(
            @PathVariable Long sectorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        log.debug("Listando turnos del sector {} para fecha: {}", sectorId, fecha);

        Sector sector = sectorService.buscarPorId(sectorId)
                .orElseThrow(() -> ResourceNotFoundException.sector(sectorId));

        List<Turno> turnos = turnoService.listarTurnosDelDia(sectorId, fecha);
        List<TurnoSummaryResponse> response = TurnoMapper.toSummaryResponseList(turnos);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d turnos para el sector %s en fecha %s",
                                turnos.size(), sector.getCodigo(), fecha))
        );
    }

    // ==========================================
    // ENDPOINTS DE OPERACIONES (EMPLEADOS)
    // ==========================================

    //Llama un turno
    //POST /api/turnos/{id}/llamar
    @PostMapping("/{id}/llamar")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<TurnoResponse>> llamarTurno(
            @PathVariable Long id,
            @Valid @RequestBody LlamarTurnoRequest request,
            Authentication authentication) {

        log.info("Llamando turno ID: {}", id);

        try {
            Long empleadoId = obtenerEmpleadoIdDesdeAuth(authentication);

//            Turno turno = turnoService.llamarTurno(id, empleadoId);
            Turno turno = turnoService.llamarTurno(id, empleadoId, request.getObservaciones());
            TurnoResponse response = TurnoMapper.toResponse(turno);

            log.info("Turno {} llamado exitosamente", turno.getCodigo());

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response, "Turno llamado exitosamente")
            );

        } catch (Exception e) {
            log.warn("Error llamando turno {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error(e.getMessage(), "CALL_ERROR")
            );
        }
    }

    //Inicia atención de un turno
    //POST /api/turnos/{id}/iniciar-atencion
    @PostMapping("/{id}/iniciar-atencion")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<TurnoResponse>> iniciarAtencion(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("Iniciando atención del turno ID: {}", id);

        try {
            Long empleadoId = obtenerEmpleadoIdDesdeAuth(authentication);

            Turno turno = turnoService.iniciarAtencion(id, empleadoId);
            TurnoResponse response = TurnoMapper.toResponse(turno);

            log.info("Atención iniciada para turno: {}", turno.getCodigo());

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response, "Atención iniciada exitosamente")
            );

        } catch (Exception e) {
            log.warn("Error iniciando atención del turno {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error(e.getMessage(), "START_ATTENTION_ERROR")
            );
        }
    }

    //Finaliza atención de un turno
    //POST /api/turnos/{id}/finalizar
    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<TurnoResponse>> finalizarAtencion(
            @PathVariable Long id,
            @Valid @RequestBody FinalizarTurnoRequest request) {

        log.info("Finalizando atención del turno ID: {}", id);

        try {
            Turno turno = turnoService.finalizarAtencion(id, request.getObservaciones());
            TurnoResponse response = TurnoMapper.toResponse(turno);

            log.info("Atención finalizada para turno: {}", turno.getCodigo());

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response, "Atención finalizada exitosamente")
            );

        } catch (Exception e) {
            log.warn("Error finalizando atención del turno {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error(e.getMessage(), "FINISH_ATTENTION_ERROR")
            );
        }
    }

    //Marca un turno como ausente
    //POST /api/turnos/{id}/marcar-ausente
    @PostMapping("/{id}/marcar-ausente")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<TurnoResponse>> marcarAusente(
            @PathVariable Long id,
            @Valid @RequestBody MarcarAusenteRequest request,
            Authentication authentication) {

        log.info("Marcando como ausente el turno ID: {}", id);
        try {
            Long empleadoId = obtenerEmpleadoIdDesdeAuth(authentication);

            // CAMBIO: pasar observaciones al service
            String obs = request.getObservaciones() != null ? request.getObservaciones().trim() : null;
            Turno turno = turnoService.marcarAusente(id, empleadoId, obs);

            TurnoResponse response = TurnoMapper.toResponse(turno);
            return ResponseEntity.ok(ApiResponseWrapper.success(response, "Turno marcado como ausente"));
        } catch (Exception e) {
            log.warn("Error marcando ausente el turno {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponseWrapper.error(e.getMessage(), "MARK_ABSENT_ERROR"));
        }
    }

    //Redirige un turno a otro sector
    //POST /api/turnos/{id}/redirigir
    @PostMapping("/{id}/redirigir")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<TurnoResponse>> redirigirTurno(
            @PathVariable Long id,
            @Valid @RequestBody RedirigirTurnoRequest request,
            Authentication authentication) {

        log.info("Redirigiendo turno ID: {} al sector: {}", id, request.getNuevoSectorId());

        try {
            Long empleadoId = obtenerEmpleadoIdDesdeAuth(authentication);

            // Validar que existe el sector destino
            Sector sectorDestino = sectorService.buscarPorId(request.getNuevoSectorId())
                    .orElseThrow(() -> ResourceNotFoundException.sector(request.getNuevoSectorId()));

//            Turno turno = turnoService.redirigirTurno(
//                    id,
//                    request.getNuevoSectorId(),
//                    request.getMotivo(),
//                    empleadoId
//            );
            Turno turno = turnoService.redirigirTurno(
                    id,
                    request.getNuevoSectorId(),
                    request.getMotivo(),
                    request.getObservaciones(),
                    empleadoId
            );

            TurnoResponse response = TurnoMapper.toResponse(turno);

            log.info("Turno {} redirigido exitosamente al sector {}",
                    turno.getCodigo(), sectorDestino.getCodigo());

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response,
                            "Turno redirigido exitosamente al sector " + sectorDestino.getCodigo())
            );

        } catch (Exception e) {
            log.warn("Error redirigiendo turno {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error(e.getMessage(), "REDIRECT_ERROR")
            );
        }
    }

    // ==========================================
    // ENDPOINTS DE ESTADÍSTICAS BÁSICAS
    // ==========================================

    //Cuenta turnos pendientes en un sector
    //GET /api/turnos/pendientes/{sectorId}
    @GetMapping("/pendientes/{sectorId}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<Integer>> contarTurnosPendientes(@PathVariable Long sectorId) {

        log.debug("Contando turnos pendientes del sector: {}", sectorId);

        Sector sector = sectorService.buscarPorId(sectorId)
                .orElseThrow(() -> ResourceNotFoundException.sector(sectorId));

        int count = turnoService.contarTurnosPendientes(sectorId);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(count,
                        String.format("El sector %s tiene %d turnos pendientes", sector.getCodigo(), count))
        );
    }




    /**
     * Lista turnos con paginación y filtros opcionales
     * GET /api/turnos/listar?limite=50&offset=0&fecha=2025-09-06&sectorId=1
     */
    @GetMapping("/listar")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<TurnosListadoResponse>> listarTurnosConFiltros(
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int limite,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha,
            @RequestParam(required = false) Long sectorId) {

        log.debug("Listando turnos - límite: {}, offset: {}, fecha: {}, sectorId: {}",
                limite, offset, fecha, sectorId);

        try {
            // Validar sector si se proporciona
            if (sectorId != null) {
                sectorService.buscarPorId(sectorId)
                        .orElseThrow(() -> ResourceNotFoundException.sector(sectorId));
            }

            // Obtener turnos y total
            List<Turno> turnos = turnoService.listarTurnosConFiltros(limite, offset, fecha, sectorId);
            long total = turnoService.contarTurnosConFiltros(fecha, sectorId);

            // Mapear respuesta
            List<TurnoSummaryResponse> turnosResponse = TurnoMapper.toSummaryResponseList(turnos);

            // Crear respuesta con metadatos de paginación
            TurnosListadoResponse response = TurnosListadoResponse.builder()
                    .turnos(turnosResponse)
                    .total(total)
                    .limite(limite)
                    .offset(offset)
                    .hasNext(offset + limite < total)
                    .hasPrevious(offset > 0)
                    .totalPaginas((int) Math.ceil((double) total / limite))
                    .paginaActual((offset / limite) + 1)
                    .filtros(TurnosListadoResponse.FiltrosAplicados.builder()
                            .fecha(fecha)
                            .sectorId(sectorId)
                            .build())
                    .build();

            String mensaje = construirMensajeListado(turnos.size(), total, fecha, sectorId);

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response, mensaje)
            );

        } catch (IllegalArgumentException e) {
            log.warn("Parámetros inválidos para listar turnos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error(e.getMessage(), "INVALID_PARAMETERS")
            );
        } catch (Exception e) {
            log.error("Error listando turnos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseWrapper.error("Error interno al listar turnos", "INTERNAL_ERROR")
            );
        }
    }

    /**
     * Lista todos los turnos recientes (endpoint simple sin filtros)
     * GET /api/turnos/todos?limite=100
     */
    @GetMapping("/todos")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<TurnoSummaryResponse>>> listarTodosTurnos(
            @RequestParam(defaultValue = "100") @Min(1) @Max(500) int limite) {

        log.debug("Listando todos los turnos - límite: {}", limite);

        try {
            List<Turno> turnos = turnoService.listarTodos(limite);
            List<TurnoSummaryResponse> response = TurnoMapper.toSummaryResponseList(turnos);

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response,
                            String.format("Se listaron %d turnos (más recientes)", turnos.size()))
            );

        } catch (IllegalArgumentException e) {
            log.warn("Parámetros inválidos para listar todos los turnos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error(e.getMessage(), "INVALID_PARAMETERS")
            );
        } catch (Exception e) {
            log.error("Error listando todos los turnos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseWrapper.error("Error interno al listar turnos", "INTERNAL_ERROR")
            );
        }
    }





    // ==========================================
    // MÉTODOS HELPER PRIVADOS
    // ==========================================

    //Obtiene el ID del empleado desde el contexto de autenticación
    private Long obtenerEmpleadoIdDesdeAuth(Authentication authentication) {
        try {
            // Obtener el token JWT del request actual
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Long empleadoId = jwtService.extractEmpleadoId(token);

                if (empleadoId != null) {
                    log.debug("ID del empleado extraído del JWT: {}", empleadoId);
                    return empleadoId;
                } else {
                    log.warn("No se pudo extraer empleadoId del token JWT");
                }
            } else {
                log.warn("No se encontró token Authorization en el request");
            }

            // Fallback: intentar obtener desde el Authentication principal
            if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                String username = authentication.getName();
                log.debug("Intentando obtener empleado por username desde Authentication: {}", username);

                Optional<Empleado> empleadoOpt = empleadoService.buscarPorUsername(username);
                if (empleadoOpt.isPresent()) {
                    Long empleadoId = empleadoOpt.get().getId();
                    log.debug("ID del empleado obtenido por username: {}", empleadoId);
                    return empleadoId;
                }
            }

            log.error("No se pudo obtener el ID del empleado desde la autenticación");
            throw new IllegalStateException("No se pudo identificar al empleado autenticado");

        } catch (Exception e) {
            log.error("Error obteniendo empleadoId desde autenticación: {}", e.getMessage());
            throw new IllegalStateException("Error de autenticación: " + e.getMessage());
        }
    }

    //Procesa ciudadano para GenerarTurnoRequest (crear o actualizar)
    private Ciudadano procesarCiudadano(GenerarTurnoRequest request) {
        Ciudadano ciudadano;

        if (request.tieneDatosCiudadano()) {
            // Crear o actualizar con datos completos
            ciudadano = ciudadanoService.crearOActualizar(
                    request.getDni(),
                    request.getNombre(),
                    request.getApellido(),
                    request.getTelefono(),
                    request.getDireccion(),
                    request.getObservaciones()
            );

            // ✅ AGREGAR LÓGICA DE PRIORIDAD
            // Si el request tiene campos de prioridad, aplicarlos
            Boolean esPrioritario = request.getEsPrioritario();
            String motivoPrioridad = request.getMotivoPrioridad();

            if (esPrioritario != null && esPrioritario) {
                ciudadanoService.establecerPrioridad(
                        ciudadano.getId(),
                        true,
                        motivoPrioridad
                );
                // Recargar para obtener los datos actualizados
                ciudadano = ciudadanoService.buscarPorId(ciudadano.getId()).orElse(ciudadano);
            }

        } else {
            // Buscar ciudadano existente
            ciudadano = ciudadanoService.buscarPorDni(request.getDni())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Ciudadano no encontrado. Debe proporcionar datos completos para ciudadanos nuevos."));
        }

        return ciudadano;
    }


    /**
     * Construye mensaje descriptivo del listado
     */
    private String construirMensajeListado(int turnosEncontrados, long total, LocalDate fecha, Long sectorId) {
        StringBuilder mensaje = new StringBuilder();

        mensaje.append(String.format("Se encontraron %d", turnosEncontrados));

        if (turnosEncontrados < total) {
            mensaje.append(String.format(" de %d", total));
        }

        mensaje.append(" turnos");

        // Agregar información de filtros aplicados
        if (fecha != null || sectorId != null) {
            mensaje.append(" (filtrado");
            if (fecha != null) {
                mensaje.append(String.format(" por fecha: %s", fecha));
            }
            if (sectorId != null) {
                mensaje.append(String.format(" por sector ID: %d", sectorId));
            }
            mensaje.append(")");
        }

        return mensaje.toString();
    }
}