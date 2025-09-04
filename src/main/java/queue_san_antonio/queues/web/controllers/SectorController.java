package queue_san_antonio.queues.web.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import queue_san_antonio.queues.models.Empleado;
import queue_san_antonio.queues.models.HorarioAtencion;
import queue_san_antonio.queues.models.Sector;
import queue_san_antonio.queues.services.EmpleadoService;
import queue_san_antonio.queues.services.HorarioAtencionService;
import queue_san_antonio.queues.services.SectorService;
import queue_san_antonio.queues.services.impl.DisponibilidadService;
import queue_san_antonio.queues.services.realtime.SseTurnosService;
import queue_san_antonio.queues.web.dto.common.ApiResponseWrapper;
import queue_san_antonio.queues.web.dto.horario.HorarioAtencionResponse;
import queue_san_antonio.queues.web.dto.mapper.HorarioAtencionMapper;
import queue_san_antonio.queues.web.dto.mapper.SectorMapper;
import queue_san_antonio.queues.web.dto.sector.*;
import queue_san_antonio.queues.web.exceptions.custom.ResourceNotFoundException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Controlador REST para la gestión completa de sectores y horarios de atención
//Incluye endpoints públicos para pantallas y endpoints administrativos completos
@RestController
@RequestMapping("/api/sectores")
@RequiredArgsConstructor
@Slf4j
@Validated
public class SectorController {

    private final SectorService sectorService;
    private final EmpleadoService empleadoService;
    private final HorarioAtencionService horarioAtencionService;
    private final DisponibilidadService disponibilidadService;
    private final SseTurnosService sseTurnosService;

    // ==========================================
    // ENDPOINTS PÚBLICOS (PARA PANTALLAS)
    // ==========================================

    //Lista sectores activos para pantallas públicas
    //GET /api/sectores/publicos
    @GetMapping("/publicos")
    public ResponseEntity<ApiResponseWrapper<List<SectorResponse>>> listarSectoresPublicos() {

        log.debug("Consultando sectores públicos para pantallas");

        List<Sector> sectores = sectorService.listarActivosOrdenados();
        List<SectorResponse> response = SectorMapper.toResponseList(sectores);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d sectores activos", sectores.size()))
        );
    }

    //Lista sectores especiales (requieren cita previa)
    //GET /api/sectores/especiales
    @GetMapping("/especiales")
    public ResponseEntity<ApiResponseWrapper<List<SectorResponse>>> listarSectoresEspeciales() {

        log.debug("Consultando sectores especiales");

        List<Sector> sectores = sectorService.listarSectoresEspeciales();
        List<SectorResponse> response = SectorMapper.toResponseList(sectores);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d sectores especiales", sectores.size()))
        );
    }

    //Obtiene horarios disponibles para citas de un sector en un día
    //GET /api/sectores/{id}/horarios-disponibles/{diaSemana}
    @GetMapping("/{id}/horarios-disponibles/{diaSemana}")
    public ResponseEntity<ApiResponseWrapper<List<LocalTime>>> obtenerHorariosDisponibles(
            @PathVariable Long id,
            @PathVariable DayOfWeek diaSemana) {

        log.debug("Obteniendo horarios disponibles del sector ID: {} para día: {}", id, diaSemana);

        Sector sector = sectorService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.sector(id));

        List<LocalTime> horariosDisponibles = horarioAtencionService.obtenerHorariosDisponibles(id, diaSemana);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(horariosDisponibles,
                        String.format("Se encontraron %d horarios disponibles para %s en sector %s",
                                horariosDisponibles.size(), diaSemana, sector.getCodigo()))
        );
    }

    // ==========================================
    // ENDPOINTS DE CONSULTA (EMPLEADOS)
    // ==========================================

    //Lista todos los sectores (para empleados)
    //GET /api/sectores
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<SectorResponse>>> listarTodos() {

        log.debug("Listando todos los sectores");

        List<Sector> sectores = sectorService.listarTodos();
        List<SectorResponse> response = SectorMapper.toResponseList(sectores);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d sectores", sectores.size()))
        );
    }

//    //Lista solo sectores activos (para operaciones)
//    //GET /api/sectores/activos
//    @GetMapping("/activos")
//    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
//    public ResponseEntity<ApiResponseWrapper<List<SectorResponse>>> listarActivos() {
//
//        log.debug("Listando sectores activos");
//
//        List<Sector> sectores = sectorService.listarActivosOrdenados();
//        List<SectorResponse> response = SectorMapper.toResponseList(sectores);
//
//        return ResponseEntity.ok(
//                ApiResponseWrapper.success(response,
//                        String.format("Se encontraron %d sectores activos", sectores.size()))
//        );
//    }

    //Busca un sector por código
    //GET /api/sectores/codigo/{codigo}
    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<SectorResponse>> buscarPorCodigo(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{2,10}$", message = "El código debe contener solo letras mayúsculas (2-10 caracteres)")
            String codigo) {

        log.debug("Buscando sector por código: {}", codigo);

        Sector sector = sectorService.buscarPorCodigo(codigo)
                .orElseThrow(() -> ResourceNotFoundException.sector(codigo));

        SectorResponse response = SectorMapper.toResponse(sector);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Sector encontrado")
        );
    }

    //Busca un sector por ID
    //GET /api/sectores/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<SectorResponse>> buscarPorId(@PathVariable Long id) {

        log.debug("Buscando sector por ID: {}", id);

        Sector sector = sectorService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.sector(id));

        SectorResponse response = SectorMapper.toResponse(sector);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Sector encontrado")
        );
    }

    //Obtiene información completa de un sector con sus horarios
    //GET /api/sectores/{id}/completo
    @GetMapping("/{id}/completo")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<SectorConHorariosResponse>> obtenerSectorCompleto(
            @PathVariable Long id) {

        log.debug("Obteniendo información completa del sector ID: {}", id);

        Sector sector = sectorService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.sector(id));

        List<HorarioAtencion> horarios = horarioAtencionService.listarPorSector(id);

        SectorResponse sectorResponse = SectorMapper.toResponse(sector);
        List<HorarioAtencionResponse> horariosResponse = HorarioAtencionMapper.toResponseList(horarios);

        SectorConHorariosResponse response = SectorConHorariosResponse.builder()
                .sector(sectorResponse)
                .horarios(horariosResponse)
                .cantidadHorarios(horarios.size())
                .tieneHorariosActivos(horarios.stream().anyMatch(HorarioAtencion::estaActivo))
                .build();

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Información completa del sector obtenida")
        );
    }

    //Verifica si existe un sector por código
    //GET /api/sectores/existe/{codigo}
    @GetMapping("/existe/{codigo}")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<Boolean>> existeSector(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{2,10}$", message = "El código debe contener solo letras mayúsculas (2-10 caracteres)")
            String codigo) {

        log.debug("Verificando existencia de sector con código: {}", codigo);

        boolean existe = sectorService.existePorCodigo(codigo);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(existe,
                        existe ? "El sector existe" : "El sector no existe")
        );
    }


    // ==========================================
    // ENDPOINTS DE ADMINISTRACIÓN DE SECTORES
    // ==========================================

    //Crea un nuevo sector
    //POST /api/sectores
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<SectorResponse>> crearSector(
            @Valid @RequestBody SectorRequest request) {

        log.info("Creando nuevo sector con código: {}", request.getCodigo());

        if (sectorService.existePorCodigo(request.getCodigo())) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("Ya existe un sector con código: " + request.getCodigo(), "DUPLICATE_CODIGO")
            );
        }

        Sector sector = SectorMapper.toEntity(request);
        Sector sectorGuardado = sectorService.guardar(sector);

        SectorResponse response = SectorMapper.toResponse(sectorGuardado);

        log.info("Sector creado exitosamente: {} - {}",
                sectorGuardado.getCodigo(), sectorGuardado.getNombre());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Sector creado exitosamente")
        );
    }

    //Actualiza un sector existente
    //PUT /api/sectores/{codigo}
    @PutMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<SectorResponse>> actualizarSector(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{2,10}$", message = "El código debe contener solo letras mayúsculas (2-10 caracteres)")
            String codigo,
            @Valid @RequestBody SectorUpdateRequest request) {

        log.info("Actualizando sector con código: {}", codigo);

        Sector sector = sectorService.buscarPorCodigo(codigo)
                .orElseThrow(() -> ResourceNotFoundException.sector(codigo));

        SectorMapper.updateEntity(sector, request);

        Sector sectorActualizado = sectorService.guardar(sector);
        SectorResponse response = SectorMapper.toResponse(sectorActualizado);

        log.info("Sector actualizado exitosamente: {} - {}",
                sectorActualizado.getCodigo(), sectorActualizado.getNombre());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Sector actualizado exitosamente")
        );
    }

    //Activa un sector
    //PATCH /api/sectores/{id}/activar
    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<SectorResponse>> activarSector(@PathVariable Long id) {

        log.info("Activando sector ID: {}", id);

        Sector sector = sectorService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.sector(id));

        sectorService.activar(id);

        Sector sectorActualizado = sectorService.buscarPorId(id).orElse(sector);
        SectorResponse response = SectorMapper.toResponse(sectorActualizado);

        log.info("Sector activado: {} - {}", sector.getCodigo(), sector.getNombre());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Sector activado exitosamente")
        );
    }

    //Desactiva un sector
    //PATCH /api/sectores/{id}/desactivar
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<SectorResponse>> desactivarSector(@PathVariable Long id) {

        log.info("Desactivando sector ID: {}", id);

        Sector sector = sectorService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.sector(id));

        sectorService.desactivar(id);

        Sector sectorActualizado = sectorService.buscarPorId(id).orElse(sector);
        SectorResponse response = SectorMapper.toResponse(sectorActualizado);

        log.info("Sector desactivado: {} - {}", sector.getCodigo(), sector.getNombre());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Sector desactivado exitosamente")
        );
    }

    //Asigna un responsable a un sector
    //POST /api/sectores/{id}/responsable
    @PostMapping("/{id}/responsable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<SectorResponse>> asignarResponsable(
            @PathVariable Long id,
            @Valid @RequestBody AsignarResponsableRequest request) {

        log.info("Asignando responsable {} al sector ID: {}", request.getEmpleadoId(), id);

        Sector sector = sectorService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.sector(id));

        Empleado empleado = empleadoService.buscarPorId(request.getEmpleadoId())
                .orElseThrow(() -> ResourceNotFoundException.empleado(request.getEmpleadoId()));

        sectorService.asignarResponsable(id, request.getEmpleadoId());

        Sector sectorActualizado = sectorService.buscarPorId(id).orElse(sector);
        SectorResponse response = SectorMapper.toResponse(sectorActualizado);

        log.info("Responsable asignado exitosamente: {} al sector {}",
                empleado.getUsername(), sector.getCodigo());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Responsable asignado exitosamente")
        );
    }

    @GetMapping("/{id}/disponibilidad")
    @PreAuthorize("hasAnyRole('OPERADOR','RESPONSABLE_SECTOR','ADMIN')")
    public ResponseEntity<ApiResponseWrapper<Map<String, Object>>> getDisponibilidad(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        List<String> horas = disponibilidadService.getHorasDisponibles(id, fecha);

        Map<String, Object> data = new HashMap<>();
        data.put("sectorId", id);
        data.put("fecha", fecha.toString());
        data.put("horasDisponibles", horas);

        return ResponseEntity.ok(ApiResponseWrapper.success(data, "Disponibilidad del día"));

    }

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    @PreAuthorize("hasAnyRole('OPERADOR','RESPONSABLE_SECTOR','ADMIN')")
    public SseEmitter stream(@PathVariable Long id) {
        return sseTurnosService.subscribe(id);
    }

}