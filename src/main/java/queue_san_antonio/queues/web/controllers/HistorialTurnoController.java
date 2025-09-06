package queue_san_antonio.queues.web.controllers;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import queue_san_antonio.queues.models.*;
import queue_san_antonio.queues.services.*;
import queue_san_antonio.queues.services.impl.HistorialTurnoServiceImpl;
import queue_san_antonio.queues.web.dto.common.ApiResponseWrapper;
import queue_san_antonio.queues.web.dto.historial.*;
import queue_san_antonio.queues.web.dto.mapper.HistorialTurnoMapper;
import queue_san_antonio.queues.web.exceptions.custom.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

//Controlador REST para la gestión del historial y auditoría de turnos
//Proporciona endpoints para trazabilidad completa y auditoría
@RestController
@RequestMapping("/api/historial")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasAnyRole('OPERADOR', 'RESPONSABLE_SECTOR', 'ADMIN')")
public class HistorialTurnoController {

    private final HistorialTurnoService historialTurnoService;
    private final TurnoService turnoService;
    private final EmpleadoService empleadoService;
    private final HistorialTurnoServiceImpl historialTurnoServiceImpl;

    private final CiudadanoService ciudadanoService;
    private final SectorService sectorService;

    // ==========================================
    // ENDPOINTS DE CONSULTA DE HISTORIAL
    // ==========================================

    //Obtiene el historial completo de un turno específico
    //GET /api/historial/turno/{turnoId}
    @GetMapping("/turno/{turnoId}")
    public ResponseEntity<ApiResponseWrapper<List<HistorialTurnoResponse>>> obtenerHistorialTurno(
            @PathVariable Long turnoId) {

        log.debug("Obteniendo historial del turno ID: {}", turnoId);

        // Verificar que el turno existe
        Turno turno = turnoService.buscarPorId(turnoId)
                .orElseThrow(() -> ResourceNotFoundException.turno(turnoId));

        List<HistorialTurno> historial = historialTurnoService.listarPorTurno(turnoId);
        List<HistorialTurnoResponse> response = HistorialTurnoMapper.toResponseList(historial);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Historial del turno %s (%d acciones)",
                                turno.getCodigo(), historial.size()))
        );
    }

    //Obtiene el historial de un turno por código (más accesible)
    //GET /api/historial/turno-codigo/{codigo}
    @GetMapping("/turno-codigo/{codigo}")
    public ResponseEntity<ApiResponseWrapper<List<HistorialTurnoResponse>>> obtenerHistorialPorCodigo(
            @PathVariable String codigo) {

        log.debug("Obteniendo historial del turno código: {}", codigo);

        // Buscar turno por código
        Turno turno = turnoService.buscarPorCodigo(codigo)
                .orElseThrow(() -> ResourceNotFoundException.turno(codigo));

        List<HistorialTurno> historial = historialTurnoService.listarPorTurno(turno.getId());
        List<HistorialTurnoResponse> response = HistorialTurnoMapper.toResponseList(historial);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Historial del turno %s (%d acciones)",
                                codigo, historial.size()))
        );
    }

    //Obtiene el historial de un turno en formato legible
    //GET /api/historial/turno/{turnoId}/legible
    @GetMapping("/turno/{turnoId}/legible")
    public ResponseEntity<ApiResponseWrapper<HistorialTurnoLegibleResponse>> obtenerHistorialLegible(
            @PathVariable Long turnoId) {

        log.debug("Obteniendo historial legible del turno ID: {}", turnoId);

        // Verificar que el turno existe
        Turno turno = turnoService.buscarPorId(turnoId)
                .orElseThrow(() -> ResourceNotFoundException.turno(turnoId));

        // Usar el método adicional del service implementación
        List<String> historialLegible = historialTurnoServiceImpl.obtenerHistorialLegible(turnoId);

        // Determinar si el turno está completado basándose en el estado
        boolean completado = turno.getEstado() == queue_san_antonio.queues.models.EstadoTurno.FINALIZADO ||
                turno.getEstado() == queue_san_antonio.queues.models.EstadoTurno.CANCELADO ||
                turno.getEstado() == queue_san_antonio.queues.models.EstadoTurno.AUSENTE;

        HistorialTurnoLegibleResponse response = HistorialTurnoMapper.toHistorialLegible(
                historialLegible,
                turno.getCodigo(),
                turno.getEstado().name(),
                turno.getFechaHoraGeneracion() != null ? turno.getFechaHoraGeneracion().toString() : null,
                turno.getFechaHoraFinalizacion() != null ? turno.getFechaHoraFinalizacion().toString() : null,
                completado
        );

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Historial legible del turno %s", turno.getCodigo()))
        );
    }

    // ==========================================
    // ENDPOINTS DE AUDITORÍA POR EMPLEADO
    // ==========================================

    //Obtiene las acciones de un empleado en un período
    //GET /api/historial/empleado/{empleadoId}/periodo?fechaInicio=2024-01-01&fechaFin=2024-01-31
    @GetMapping("/empleado/{empleadoId}/periodo")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<HistorialSummaryResponse>>> obtenerAccionesEmpleado(
            @PathVariable Long empleadoId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin) {

        log.debug("Obteniendo acciones del empleado {} entre {} y {}", empleadoId, fechaInicio, fechaFin);

        // Validar fechas
        if (fechaFin.isBefore(fechaInicio)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("La fecha de fin no puede ser anterior a la fecha de inicio",
                            "INVALID_DATE_RANGE")
            );
        }

        // Validar rango máximo (3 meses para auditoría)
        if (fechaInicio.plusMonths(3).isBefore(fechaFin)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("El período de auditoría no puede exceder 3 meses",
                            "DATE_RANGE_TOO_LARGE")
            );
        }

        // Verificar que el empleado existe
        Empleado empleado = empleadoService.buscarPorId(empleadoId)
                .orElseThrow(() -> ResourceNotFoundException.empleado(empleadoId));

        List<HistorialTurno> historial = historialTurnoService.listarAccionesEmpleado(empleadoId, fechaInicio, fechaFin);
        List<HistorialSummaryResponse> response = HistorialTurnoMapper.toSummaryResponseList(historial);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d acciones de %s entre %s y %s",
                                historial.size(), empleado.getNombreCompleto(), fechaInicio, fechaFin))
        );
    }

    //Genera un reporte de auditoría completo para un empleado
    //GET /api/historial/empleado/{empleadoId}/auditoria
    @GetMapping("/empleado/{empleadoId}/auditoria")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<ResumenAuditoriaResponse>> generarAuditoriaEmpleado(
            @PathVariable Long empleadoId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin) {

        log.info("Generando auditoría del empleado {} entre {} y {}",
                empleadoId, fechaInicio, fechaFin);

        // Validar fechas
        if (fechaFin.isBefore(fechaInicio)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("La fecha de fin no puede ser anterior a la fecha de inicio",
                            "INVALID_DATE_RANGE")
            );
        }

        // Validar rango máximo (3 meses para auditoría)
        if (fechaInicio.plusMonths(3).isBefore(fechaFin)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("El período de auditoría no puede exceder 3 meses",
                            "DATE_RANGE_TOO_LARGE")
            );
        }

        // Verificar que el empleado existe
        Empleado empleado = empleadoService.buscarPorId(empleadoId)
                .orElseThrow(() -> ResourceNotFoundException.empleado(empleadoId));

        List<HistorialTurno> historial = historialTurnoService.listarAccionesEmpleado(
                empleadoId, fechaInicio, fechaFin);

        ResumenAuditoriaResponse resumen = HistorialTurnoMapper.toResumenAuditoria(
                historial,
                fechaInicio,
                fechaFin,
                empleado.getUsername(),
                empleado.getNombreCompleto(),
                empleado.getSector() != null ? empleado.getSector().getCodigo() : "N/A",
                empleado.getSector() != null ? empleado.getSector().getNombre() : "Sin sector"
        );

        log.info("Auditoría generada para {}: {} acciones en {} días",
                empleado.getNombreCompleto(), resumen.getTotalAcciones(), resumen.getTotalDias());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(resumen, "Auditoría de empleado generada exitosamente")
        );
    }

    // ==========================================
    // ENDPOINTS DE AUDITORÍA DEL SISTEMA
    // ==========================================

    //Obtiene las últimas acciones del sistema
    //GET /api/historial/ultimas-acciones?limite=50
    @GetMapping("/ultimas-acciones")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<HistorialSummaryResponse>>> obtenerUltimasAcciones(
            @RequestParam(defaultValue = "50") @Min(1) @Max(500) int limite) {

        log.debug("Obteniendo últimas {} acciones del sistema", limite);

        List<HistorialTurno> historial = historialTurnoService.listarUltimasAcciones(limite);
        List<HistorialSummaryResponse> response = HistorialTurnoMapper.toSummaryResponseList(historial);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Últimas %d acciones del sistema", historial.size()))
        );
    }

    //Obtiene las acciones del día actual
    //GET /api/historial/acciones-hoy
    @GetMapping("/acciones-hoy")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<HistorialSummaryResponse>>> obtenerAccionesHoy() {

        log.debug("Obteniendo acciones de hoy");

        LocalDate hoy = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));

        // Usar un límite alto para obtener todas las acciones del día
        List<HistorialTurno> historial = historialTurnoService.listarUltimasAcciones(1000)
                .stream()
                .filter(h -> h.getFechaHora().toLocalDate().equals(hoy))
                .toList();

        List<HistorialSummaryResponse> response = HistorialTurnoMapper.toSummaryResponseList(historial);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d acciones del día %s", historial.size(), hoy))
        );
    }

    //Obtiene actividad reciente (últimas 24 horas)
    //GET /api/historial/actividad-reciente
    @GetMapping("/actividad-reciente")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<HistorialSummaryResponse>>> obtenerActividadReciente() {

        log.debug("Obteniendo actividad reciente (24 horas)");

        // Obtener las últimas 200 acciones y filtrar por las últimas 24 horas
        List<HistorialTurno> historial = historialTurnoService.listarUltimasAcciones(200)
                .stream()
                .filter(h -> h.getFechaHora().isAfter(java.time.LocalDateTime.now().minusHours(24)))
                .toList();

        List<HistorialSummaryResponse> response = HistorialTurnoMapper.toSummaryResponseList(historial);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d acciones en las últimas 24 horas", historial.size()))
        );
    }

    // ==========================================
    // ENDPOINTS DE CONSULTAS ESPECÍFICAS
    // ==========================================

    //Busca turnos que han sido redirigidos
    //GET /api/historial/redirecciones?limite=100
    @GetMapping("/redirecciones")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<HistorialSummaryResponse>>> obtenerRedirecciones(
            @RequestParam(defaultValue = "100") @Min(1) @Max(500) int limite) {

        log.debug("Obteniendo últimas {} redirecciones", limite);

        List<HistorialTurno> historial = historialTurnoService.listarUltimasAcciones(limite * 2) // Buscar más para filtrar
                .stream()
                .filter(HistorialTurno::esRedireccion)
                .limit(limite)
                .toList();

        List<HistorialSummaryResponse> response = HistorialTurnoMapper.toSummaryResponseList(historial);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d redirecciones recientes", historial.size()))
        );
    }

    //Busca cambios de estado específicos
    //GET /api/historial/cambios-estado?limite=100
    @GetMapping("/cambios-estado")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<HistorialSummaryResponse>>> obtenerCambiosEstado(
            @RequestParam(defaultValue = "100") @Min(1) @Max(500) int limite) {

        log.debug("Obteniendo últimos {} cambios de estado", limite);

        List<HistorialTurno> historial = historialTurnoService.listarUltimasAcciones(limite * 2) // Buscar más para filtrar
                .stream()
                .filter(HistorialTurno::esCambioEstado)
                .limit(limite)
                .toList();

        List<HistorialSummaryResponse> response = HistorialTurnoMapper.toSummaryResponseList(historial);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d cambios de estado recientes", historial.size()))
        );
    }

    // ==========================================
    // ENDPOINTS DE CONSULTAS POR FECHA
    // ==========================================

    //Obtiene el historial de una fecha específica
    //GET /api/historial/fecha/{fecha}?limite=200
    @GetMapping("/fecha/{fecha}")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<HistorialSummaryResponse>>> obtenerHistorialPorFecha(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha,
            @RequestParam(defaultValue = "200") @Min(1) @Max(1000) int limite) {

        log.debug("Obteniendo historial de la fecha {} (límite: {})", fecha, limite);

        // Obtener muchas acciones y filtrar por fecha (método simple, para producción se necesitaría query específica)
        List<HistorialTurno> historial = historialTurnoService.listarUltimasAcciones(limite * 5)
                .stream()
                .filter(h -> h.getFechaHora().toLocalDate().equals(fecha))
                .limit(limite)
                .toList();

        List<HistorialSummaryResponse> response = HistorialTurnoMapper.toSummaryResponseList(historial);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d acciones del día %s", historial.size(), fecha))
        );
    }

    //Compara la actividad entre dos fechas
    //GET /api/historial/comparar-fechas?fecha1=2024-01-01&fecha2=2024-01-02
    @GetMapping("/comparar-fechas")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<HistorialSummaryResponse>>> compararActividadEntreFechas(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha1,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha2) {

        log.debug("Comparando actividad entre {} y {}", fecha1, fecha2);

        // Obtener historial de ambas fechas
        List<HistorialTurno> historialCompleto = historialTurnoService.listarUltimasAcciones(2000)
                .stream()
                .filter(h -> {
                    LocalDate fechaAccion = h.getFechaHora().toLocalDate();
                    return fechaAccion.equals(fecha1) || fechaAccion.equals(fecha2);
                })
                .toList();

        List<HistorialSummaryResponse> response = HistorialTurnoMapper.toSummaryResponseList(historialCompleto);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Comparación entre %s y %s (%d acciones totales)",
                                fecha1, fecha2, historialCompleto.size()))
        );
    }

    // ==========================================
    // ENDPOINTS DE TRAZABILIDAD AVANZADA
    // ==========================================

    //Rastrea la trazabilidad completa de un ciudadano por DNI
    //GET /api/historial/ciudadano/{dni}/trazabilidad
    @GetMapping("/ciudadano/{dni}/trazabilidad")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<HistorialSummaryResponse>>> obtenerTrazabilidadCiudadano(
            @PathVariable String dni,
            @RequestParam(defaultValue = "500") @Min(1) @Max(1000) int limite) {

        log.debug("Obteniendo trazabilidad del ciudadano DNI: {} (límite: {})", dni, limite);

        // Obtener historial y filtrar por DNI del ciudadano
        List<HistorialTurno> historial = historialTurnoService.listarUltimasAcciones(limite * 2)
                .stream()
                .filter(h -> h.getTurno() != null &&
                        h.getTurno().getCiudadano() != null &&
                        dni.equals(h.getTurno().getCiudadano().getDni()))
                .limit(limite)
                .toList();

        List<HistorialSummaryResponse> response = HistorialTurnoMapper.toSummaryResponseList(historial);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Trazabilidad del ciudadano %s (%d acciones)", dni, historial.size()))
        );
    }

    //Obtiene resumen de actividad por empleado en el día actual
    //GET /api/historial/resumen-empleados-hoy
    @GetMapping("/resumen-empleados-hoy")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<HistorialSummaryResponse>>> obtenerResumenEmpleadosHoy() {

        log.debug("Obteniendo resumen de actividad de empleados para hoy");

        LocalDate hoy = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));

        List<HistorialTurno> historial = historialTurnoService.listarUltimasAcciones(1000)
                .stream()
                .filter(h -> h.getFechaHora().toLocalDate().equals(hoy))
                .filter(h -> h.getEmpleado() != null) // Solo acciones con empleado
                .toList();

        List<HistorialSummaryResponse> response = HistorialTurnoMapper.toSummaryResponseList(historial);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Resumen de actividad de empleados para %s (%d acciones)",
                                hoy, historial.size()))
        );
    }








    /**
     * Obtiene el historial de un ciudadano por DNI
     * GET /api/historial/ciudadano/{dni}?limite=100
     */
    @GetMapping("/ciudadano/{dni}")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<HistorialSummaryResponse>>> obtenerHistorialCiudadano(
            @PathVariable @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener entre 7 y 8 dígitos") String dni,
            @RequestParam(defaultValue = "100") @Min(1) @Max(500) int limite) {

        log.debug("Obteniendo historial del ciudadano DNI: {} (límite: {})", dni, limite);

        try {
            // Buscar ciudadano
            Ciudadano ciudadano = ciudadanoService.buscarPorDni(dni)
                    .orElseThrow(() -> ResourceNotFoundException.ciudadano(dni));

            // Obtener turnos del ciudadano
            List<Turno> turnos = turnoService.listarTurnosCiudadano(ciudadano.getId());

            // Obtener historial de todos los turnos del ciudadano
            List<HistorialTurno> historialCompleto = new ArrayList<>();
            for (Turno turno : turnos) {
                List<HistorialTurno> historialTurno = historialTurnoService.listarPorTurno(turno.getId());
                historialCompleto.addAll(historialTurno);
            }

            // Ordenar por fecha descendente y limitar
            List<HistorialTurno> historialLimitado = historialCompleto.stream()
                    .sorted((h1, h2) -> h2.getFechaHora().compareTo(h1.getFechaHora()))
                    .limit(limite)
                    .toList();

            List<HistorialSummaryResponse> response = HistorialTurnoMapper.toSummaryResponseList(historialLimitado);

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response,
                            String.format("Se encontraron %d acciones en el historial de %s",
                                    response.size(), ciudadano.getNombreCompleto()))
            );

        } catch (Exception e) {
            log.error("Error obteniendo historial del ciudadano {}: {}", dni, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseWrapper.error("Error obteniendo historial del ciudadano", "INTERNAL_ERROR")
            );
        }
    }

    /**
     * Obtiene métricas rápidas del historial de hoy
     * GET /api/historial/metricas/hoy
     */
    @GetMapping("/metricas/hoy")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<MetricasHistorialResponse>> obtenerMetricasHoy() {

        log.debug("Obteniendo métricas del historial de hoy");

        try {
            LocalDate hoy = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));

            // Obtener acciones del día
            List<HistorialTurno> accionesHoy = historialTurnoService.listarUltimasAcciones(2000)
                    .stream()
                    .filter(h -> h.getFechaHora().toLocalDate().equals(hoy))
                    .toList();

            // Calcular métricas
            long totalAcciones = accionesHoy.size();
            long turnosGenerados = accionesHoy.stream()
                    .filter(h -> h.getAccion() == AccionTurno.GENERADO)
                    .count();
            long turnosLlamados = accionesHoy.stream()
                    .filter(h -> h.getAccion() == AccionTurno.LLAMADO)
                    .count();
            long turnosFinalizados = accionesHoy.stream()
                    .filter(h -> h.getAccion() == AccionTurno.FINALIZADA_ATENCION)
                    .count();
            long redirecciones = accionesHoy.stream()
                    .filter(HistorialTurno::esRedireccion)
                    .count();
            long ausentes = accionesHoy.stream()
                    .filter(h -> h.getAccion() == AccionTurno.MARCADO_AUSENTE)
                    .count();

            // Empleados activos hoy
            long empleadosActivos = accionesHoy.stream()
                    .map(h -> h.getEmpleado().getId())
                    .distinct()
                    .count();

            MetricasHistorialResponse response = MetricasHistorialResponse.builder()
                    .fecha(hoy)
                    .totalAcciones(totalAcciones)
                    .turnosGenerados(turnosGenerados)
                    .turnosLlamados(turnosLlamados)
                    .turnosFinalizados(turnosFinalizados)
                    .redirecciones(redirecciones)
                    .ausentes(ausentes)
                    .empleadosActivos(empleadosActivos)
                    .build();

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response,
                            String.format("Métricas del día %s generadas exitosamente", hoy))
            );

        } catch (Exception e) {
            log.error("Error obteniendo métricas de hoy: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseWrapper.error("Error obteniendo métricas", "INTERNAL_ERROR")
            );
        }
    }

    /**
     * Compara actividad entre dos sectores en una fecha
     * GET /api/historial/comparar-sectores/{sectorId1}/{sectorId2}?fecha=2025-09-06
     */
    @GetMapping("/comparar-sectores/{sectorId1}/{sectorId2}")
    @PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseWrapper<ComparacionSectoresResponse>> compararSectores(
            @PathVariable Long sectorId1,
            @PathVariable Long sectorId2,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha) {

        log.debug("Comparando actividad entre sectores {} y {} para fecha: {}", sectorId1, sectorId2, fecha);

        try {
            // Validar sectores
            Sector sector1 = sectorService.buscarPorId(sectorId1)
                    .orElseThrow(() -> ResourceNotFoundException.sector(sectorId1));
            Sector sector2 = sectorService.buscarPorId(sectorId2)
                    .orElseThrow(() -> ResourceNotFoundException.sector(sectorId2));

            // Obtener turnos de ambos sectores en la fecha
            List<Turno> turnosSector1 = turnoService.listarTurnosDelDia(sectorId1, fecha);
            List<Turno> turnosSector2 = turnoService.listarTurnosDelDia(sectorId2, fecha);

            // Crear métricas de comparación
            MetricasSectorResponse metricasSector1 = crearMetricasSector(sector1, turnosSector1);
            MetricasSectorResponse metricasSector2 = crearMetricasSector(sector2, turnosSector2);

            ComparacionSectoresResponse response = ComparacionSectoresResponse.builder()
                    .fecha(fecha)
                    .sector1(metricasSector1)
                    .sector2(metricasSector2)
                    .build();

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response,
                            String.format("Comparación entre sectores %s y %s para %s",
                                    sector1.getCodigo(), sector2.getCodigo(), fecha))
            );

        } catch (Exception e) {
            log.error("Error comparando sectores: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseWrapper.error("Error comparando sectores", "INTERNAL_ERROR")
            );
        }
    }

    // ==========================================
// MÉTODOS HELPER PRIVADOS
// ==========================================

    private MetricasSectorResponse crearMetricasSector(Sector sector, List<Turno> turnos) {
        return MetricasSectorResponse.builder()
                .sectorId(sector.getId())
                .sectorCodigo(sector.getCodigo())
                .sectorNombre(sector.getNombre())
                .totalTurnos((long) turnos.size())
                .turnosFinalizados(turnos.stream()
                        .filter(t -> t.getEstado() == EstadoTurno.FINALIZADO)
                        .count())
                .turnosAusentes(turnos.stream()
                        .filter(t -> t.getEstado() == EstadoTurno.AUSENTE)
                        .count())
                .turnosRedirigidos(turnos.stream()
                        .filter(t -> t.getEstado() == EstadoTurno.REDIRIGIDO)
                        .count())
                .build();
    }
}