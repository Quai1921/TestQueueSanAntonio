package queue_san_antonio.queues.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import queue_san_antonio.queues.models.HorarioAtencion;
import queue_san_antonio.queues.models.Sector;
import queue_san_antonio.queues.services.HorarioAtencionService;
import queue_san_antonio.queues.services.SectorService;
import queue_san_antonio.queues.web.dto.common.ApiResponseWrapper;
import queue_san_antonio.queues.web.dto.horario.HorarioAtencionRequest;
import queue_san_antonio.queues.web.dto.horario.HorarioAtencionResponse;
import queue_san_antonio.queues.web.dto.mapper.HorarioAtencionMapper;
import queue_san_antonio.queues.web.dto.sector.*;
import queue_san_antonio.queues.web.exceptions.custom.ResourceNotFoundException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/horarios")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
public class HorarioAtencionController {

    private final SectorService sectorService;
    private final HorarioAtencionService horarioAtencionService;


    // ==========================================
    // ENDPOINTS DE GESTIÓN DE HORARIOS
    // ==========================================

    //Lista todos los horarios de un sector
    //GET /api/sectores/{id}/horarios
    @GetMapping("/{id}/horarios")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<HorarioAtencionResponse>>> listarHorariosSector(
            @PathVariable Long id) {

        log.debug("Listando horarios del sector ID: {}", id);

        Sector sector = sectorService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.sector(id));

        List<HorarioAtencion> horarios = horarioAtencionService.listarPorSector(id);
        List<HorarioAtencionResponse> response = HorarioAtencionMapper.toSummaryResponseList(horarios);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d horarios para el sector %s",
                                horarios.size(), sector.getCodigo()))
        );
    }

    //Obtiene horarios de un sector para un día específico
    //GET /api/sectores/{id}/horarios/dia/{diaSemana}
    @GetMapping("/{id}/horarios/dia/{diaSemana}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<HorarioAtencionResponse>>> listarHorariosPorDia(
            @PathVariable Long id,
            @PathVariable DayOfWeek diaSemana) {

        log.debug("Listando horarios del sector ID: {} para día: {}", id, diaSemana);

        Sector sector = sectorService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.sector(id));

        List<HorarioAtencion> horarios = horarioAtencionService.listarPorDia(id, diaSemana);
        List<HorarioAtencionResponse> response = HorarioAtencionMapper.toResponseList(horarios);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d horarios para %s en sector %s",
                                horarios.size(), diaSemana, sector.getCodigo()))
        );
    }

    //Crea un nuevo horario de atención para un sector
    //POST /api/sectores/{id}/horarios
    @PostMapping("/{id}/horarios")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<HorarioAtencionResponse>> crearHorario(
            @PathVariable Long id,
            @Valid @RequestBody HorarioAtencionRequest request) {

        log.info("Creando horario para sector ID: {} - {} de {} a {}",
                id, request.getDiaSemana(), request.getHoraInicio(), request.getHoraFin());

        Sector sector = sectorService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.sector(id));

        if (!request.isHorarioValido()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("La hora de inicio debe ser anterior a la hora de fin", "INVALID_TIME_RANGE")
            );
        }

        if (horarioAtencionService.hayConflictoHorarios(id, request.getDiaSemana(),
                request.getHoraInicio(), request.getHoraFin())) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("El horario se superpone con otro horario existente", "TIME_CONFLICT")
            );
        }

        HorarioAtencion horario = horarioAtencionService.crear(
                id,
                request.getDiaSemana(),
                request.getHoraInicio(),
                request.getHoraFin(),
                request.getIntervaloCitas()
        );

        if (request.getCapacidadMaxima() != null || request.getObservacionesLimpias() != null) {
            HorarioAtencionMapper.updateEntity(horario, request);
            horario = horarioAtencionService.guardar(horario);
        }

        HorarioAtencionResponse response = HorarioAtencionMapper.toResponse(horario);

        log.info("Horario creado exitosamente para sector {}: {} de {} a {}",
                sector.getCodigo(), request.getDiaSemana(), request.getHoraInicio(), request.getHoraFin());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Horario de atención creado exitosamente")
        );
    }

    //Actualiza un horario de atención existente
    //PUT /api/sectores/{sectorId}/horarios/{horarioId}
    @PutMapping("/{sectorId}/horarios/{horarioId}")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<HorarioAtencionResponse>> actualizarHorario(
            @PathVariable Long sectorId,
            @PathVariable Long horarioId,
            @Valid @RequestBody HorarioAtencionRequest request) {

        log.info("Actualizando horario ID: {} del sector ID: {}", horarioId, sectorId);

        Sector sector = sectorService.buscarPorId(sectorId)
                .orElseThrow(() -> ResourceNotFoundException.sector(sectorId));

        HorarioAtencion horario = horarioAtencionService.buscarPorId(horarioId)
                .orElseThrow(() -> ResourceNotFoundException.horario(horarioId));

        if (!horario.getSector().getId().equals(sectorId)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("El horario no pertenece al sector especificado", "INVALID_SECTOR_HORARIO")
            );
        }

        if (!request.isHorarioValido()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("La hora de inicio debe ser anterior a la hora de fin", "INVALID_TIME_RANGE")
            );
        }

        HorarioAtencionMapper.updateEntity(horario, request);

        HorarioAtencion horarioActualizado = horarioAtencionService.guardar(horario);
        HorarioAtencionResponse response = HorarioAtencionMapper.toResponse(horarioActualizado);

        log.info("Horario actualizado exitosamente: {} - {} de {} a {}",
                horarioId, request.getDiaSemana(), request.getHoraInicio(), request.getHoraFin());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Horario actualizado exitosamente")
        );
    }

    //Activa un horario de atención
    //PATCH /api/sectores/{sectorId}/horarios/{horarioId}/activar
    @PatchMapping("/{sectorId}/horarios/{horarioId}/activar")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<HorarioAtencionResponse>> activarHorario(
            @PathVariable Long sectorId,
            @PathVariable Long horarioId) {

        log.info("Activando horario ID: {} del sector ID: {}", horarioId, sectorId);

        this.validarHorarioPerteneceSector(sectorId, horarioId);

        horarioAtencionService.activar(horarioId);

        HorarioAtencion horarioActualizado = horarioAtencionService.buscarPorId(horarioId)
                .orElseThrow(() -> ResourceNotFoundException.horario(horarioId));

        HorarioAtencionResponse response = HorarioAtencionMapper.toResponse(horarioActualizado);

        log.info("Horario activado: {} - {}", horarioId, horarioActualizado.getDescripcionCompleta());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Horario activado exitosamente")
        );
    }

    //Desactiva un horario de atención
    //PATCH /api/sectores/{sectorId}/horarios/{horarioId}/desactivar
    @PatchMapping("/{sectorId}/horarios/{horarioId}/desactivar")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<HorarioAtencionResponse>> desactivarHorario(
            @PathVariable Long sectorId,
            @PathVariable Long horarioId) {

        log.info("Desactivando horario ID: {} del sector ID: {}", horarioId, sectorId);

        this.validarHorarioPerteneceSector(sectorId, horarioId);

        horarioAtencionService.desactivar(horarioId);

        HorarioAtencion horarioActualizado = horarioAtencionService.buscarPorId(horarioId)
                .orElseThrow(() -> ResourceNotFoundException.horario(horarioId));

        HorarioAtencionResponse response = HorarioAtencionMapper.toResponse(horarioActualizado);

        log.info("Horario desactivado: {} - {}", horarioId, horarioActualizado.getDescripcionCompleta());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response, "Horario desactivado exitosamente")
        );
    }

    //Verifica si un sector está en horario de atención
    //GET /api/horarios/{id}/en-horario/{diaSemana}/{hora}
    @GetMapping("/{id}/en-horario/{diaSemana}/{hora}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<Boolean>> estaEnHorarioAtencion(
            @PathVariable Long id,
            @PathVariable DayOfWeek diaSemana,
            @PathVariable LocalTime hora) {

        log.debug("Verificando horario de atención para sector ID: {} - {} a las {}", id, diaSemana, hora);

        Sector sector = sectorService.buscarPorId(id)
                .orElseThrow(() -> ResourceNotFoundException.sector(id));

        boolean enHorario = horarioAtencionService.estaEnHorarioAtencion(id, diaSemana, hora);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(enHorario,
                        enHorario ?
                                String.format("El sector %s está en horario de atención", sector.getCodigo()) :
                                String.format("El sector %s NO está en horario de atención", sector.getCodigo()))
        );
    }

    // ==========================================
    // MÉTODOS HELPER PRIVADOS
    // ==========================================

    //Valida que un horario pertenezca al sector especificado
    private void validarHorarioPerteneceSector(Long sectorId, Long horarioId) {
        Sector sector = sectorService.buscarPorId(sectorId)
                .orElseThrow(() -> ResourceNotFoundException.sector(sectorId));

        HorarioAtencion horario = horarioAtencionService.buscarPorId(horarioId)
                .orElseThrow(() -> ResourceNotFoundException.horario(horarioId));

        if (!horario.getSector().getId().equals(sectorId)) {
            throw new IllegalArgumentException("El horario no pertenece al sector especificado");
        }
    }





}
