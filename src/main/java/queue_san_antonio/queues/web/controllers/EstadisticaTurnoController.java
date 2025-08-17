package queue_san_antonio.queues.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import queue_san_antonio.queues.models.EstadisticaTurno;
import queue_san_antonio.queues.models.Sector;
import queue_san_antonio.queues.models.Empleado;
import queue_san_antonio.queues.services.EstadisticaTurnoService;
import queue_san_antonio.queues.services.SectorService;
import queue_san_antonio.queues.services.EmpleadoService;
import queue_san_antonio.queues.web.dto.common.ApiResponseWrapper;
import queue_san_antonio.queues.web.dto.estadistica.*;
import queue_san_antonio.queues.web.dto.mapper.EstadisticaTurnoMapper;
import queue_san_antonio.queues.web.exceptions.custom.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//Controlador REST para la gestión de estadísticas de turnos
//Proporciona endpoints para consultas y reportes estadísticos
@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMINISTRADOR')")
public class EstadisticaTurnoController {

    private final EstadisticaTurnoService estadisticaTurnoService;
    private final SectorService sectorService;
    private final EmpleadoService empleadoService;

    // ==========================================
    // ENDPOINTS DE ESTADÍSTICAS DIARIAS
    // ==========================================

    //Obtiene estadísticas de un sector para una fecha específica
    //GET /api/estadisticas/sector/{sectorId}/fecha/{fecha}
    @GetMapping("/sector/{sectorId}/fecha/{fecha}")
    public ResponseEntity<ApiResponseWrapper<EstadisticaTurnoResponse>> obtenerEstadisticaDiaria(
            @PathVariable Long sectorId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha) {

        log.debug("Obteniendo estadística diaria del sector {} para fecha {}", sectorId, fecha);

        // Verificar que el sector existe
        Sector sector = sectorService.buscarPorId(sectorId)
                .orElseThrow(() -> ResourceNotFoundException.sector(sectorId));

        EstadisticaTurno estadistica = estadisticaTurnoService.obtenerEstadisticaDelDia(sectorId, fecha);
        EstadisticaTurnoResponse response = EstadisticaTurnoMapper.toResponse(estadistica);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Estadística del sector %s para %s", sector.getCodigo(), fecha))
        );
    }

    //Obtiene estadísticas de un empleado para una fecha específica
    //GET /api/estadisticas/empleado/{empleadoId}/sector/{sectorId}/fecha/{fecha}
    @GetMapping("/empleado/{empleadoId}/sector/{sectorId}/fecha/{fecha}")
    public ResponseEntity<ApiResponseWrapper<EstadisticaTurnoResponse>> obtenerEstadisticaEmpleado(
            @PathVariable Long empleadoId,
            @PathVariable Long sectorId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha) {

        log.debug("Obteniendo estadística del empleado {} en sector {} para fecha {}",
                empleadoId, sectorId, fecha);

        // Verificar que el empleado y sector existen
        Empleado empleado = empleadoService.buscarPorId(empleadoId)
                .orElseThrow(() -> ResourceNotFoundException.empleado(empleadoId));

        Sector sector = sectorService.buscarPorId(sectorId)
                .orElseThrow(() -> ResourceNotFoundException.sector(sectorId));

        EstadisticaTurno estadistica = estadisticaTurnoService.obtenerEstadisticaEmpleado(empleadoId, sectorId, fecha);
        EstadisticaTurnoResponse response = EstadisticaTurnoMapper.toResponse(estadistica);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Estadística de %s en %s para %s",
                                empleado.getNombreCompleto(), sector.getCodigo(), fecha))
        );
    }

    //Obtiene estadísticas de hoy para un sector
    //GET /api/estadisticas/sector/{sectorId}/hoy
    @GetMapping("/sector/{sectorId}/hoy")
    public ResponseEntity<ApiResponseWrapper<EstadisticaTurnoResponse>> obtenerEstadisticaHoy(
            @PathVariable Long sectorId) {

        log.debug("Obteniendo estadística de hoy del sector {}", sectorId);

        return obtenerEstadisticaDiaria(sectorId, LocalDate.now());
    }

    // ==========================================
    // ENDPOINTS DE ESTADÍSTICAS POR PERÍODO
    // ==========================================

    //Obtiene estadísticas de un sector entre fechas
    //GET /api/estadisticas/sector/{sectorId}/periodo?fechaInicio=2024-01-01&fechaFin=2024-01-31
    @GetMapping("/sector/{sectorId}/periodo")
    public ResponseEntity<ApiResponseWrapper<List<EstadisticaSummaryResponse>>> obtenerEstadisticasSector(
            @PathVariable Long sectorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin) {

        log.debug("Obteniendo estadísticas del sector {} entre {} y {}", sectorId, fechaInicio, fechaFin);

        // Validar fechas
        if (fechaFin.isBefore(fechaInicio)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("La fecha de fin no puede ser anterior a la fecha de inicio",
                            "INVALID_DATE_RANGE")
            );
        }

        // Verificar que el sector existe
        Sector sector = sectorService.buscarPorId(sectorId)
                .orElseThrow(() -> ResourceNotFoundException.sector(sectorId));

        List<EstadisticaTurno> estadisticas = estadisticaTurnoService.obtenerEstadisticasSector(
                sectorId, fechaInicio, fechaFin);

        List<EstadisticaSummaryResponse> response = EstadisticaTurnoMapper.toSummaryResponseList(estadisticas);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d estadísticas del sector %s entre %s y %s",
                                estadisticas.size(), sector.getCodigo(), fechaInicio, fechaFin))
        );
    }

    //Obtiene estadísticas generales entre fechas (todos los sectores)
    //GET /api/estadisticas/general/periodo?fechaInicio=2024-01-01&fechaFin=2024-01-31
    @GetMapping("/general/periodo")
    public ResponseEntity<ApiResponseWrapper<List<EstadisticaSummaryResponse>>> obtenerEstadisticasGenerales(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin) {

        log.debug("Obteniendo estadísticas generales entre {} y {}", fechaInicio, fechaFin);

        // Validar fechas
        if (fechaFin.isBefore(fechaInicio)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("La fecha de fin no puede ser anterior a la fecha de inicio",
                            "INVALID_DATE_RANGE")
            );
        }

        // Validar rango máximo (6 meses)
        if (fechaInicio.plusMonths(6).isBefore(fechaFin)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("El período no puede exceder 6 meses",
                            "DATE_RANGE_TOO_LARGE")
            );
        }

        List<EstadisticaTurno> estadisticas = estadisticaTurnoService.obtenerEstadisticasGenerales(
                fechaInicio, fechaFin);

        List<EstadisticaSummaryResponse> response = EstadisticaTurnoMapper.toSummaryResponseList(estadisticas);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d estadísticas generales entre %s y %s",
                                estadisticas.size(), fechaInicio, fechaFin))
        );
    }

    //Obtiene estadísticas de un empleado entre fechas
    //GET /api/estadisticas/empleado/{empleadoId}/periodo?fechaInicio=2024-01-01&fechaFin=2024-01-31
    @GetMapping("/empleado/{empleadoId}/periodo")
    public ResponseEntity<ApiResponseWrapper<List<EstadisticaSummaryResponse>>> obtenerEstadisticasEmpleado(
            @PathVariable Long empleadoId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin) {

        log.debug("Obteniendo estadísticas del empleado {} entre {} y {}", empleadoId, fechaInicio, fechaFin);

        // Validar fechas
        if (fechaFin.isBefore(fechaInicio)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("La fecha de fin no puede ser anterior a la fecha de inicio",
                            "INVALID_DATE_RANGE")
            );
        }

        // Verificar que el empleado existe
        Empleado empleado = empleadoService.buscarPorId(empleadoId)
                .orElseThrow(() -> ResourceNotFoundException.empleado(empleadoId));

        List<EstadisticaTurno> estadisticas = estadisticaTurnoService.obtenerEstadisticasEmpleado(
                empleadoId, fechaInicio, fechaFin);

        List<EstadisticaSummaryResponse> response = EstadisticaTurnoMapper.toSummaryResponseList(estadisticas);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Se encontraron %d estadísticas de %s entre %s y %s",
                                estadisticas.size(), empleado.getNombreCompleto(), fechaInicio, fechaFin))
        );
    }

    // ==========================================
    // ENDPOINTS DE REPORTES Y RESÚMENES
    // ==========================================

    //Genera un reporte resumido para un período
    //POST /api/estadisticas/reporte/resumen
    @PostMapping("/reporte/resumen")
    public ResponseEntity<ApiResponseWrapper<ResumenEstadisticasResponse>> generarReporteResumen(
            @Valid @RequestBody EstadisticasPeriodoRequest request) {

        log.info("Generando reporte resumen entre {} y {}", request.getFechaInicio(), request.getFechaFin());

        // Validar fechas
        if (!request.fechasValidas()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("La fecha de fin no puede ser anterior a la fecha de inicio",
                            "INVALID_DATE_RANGE")
            );
        }

        if (!request.rangoValido()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("El período no puede exceder 6 meses",
                            "DATE_RANGE_TOO_LARGE")
            );
        }

        // Obtener estadísticas completas (generales y de empleados)
        List<EstadisticaTurno> estadisticasGenerales = estadisticaTurnoService.obtenerEstadisticasGenerales(
                request.getFechaInicio(), request.getFechaFin());

        // Para el resumen necesitamos todas las estadísticas
        List<EstadisticaTurno> todasEstadisticas = new ArrayList<>(estadisticasGenerales);

        ResumenEstadisticasResponse resumen = EstadisticaTurnoMapper.toResumenResponse(
                todasEstadisticas, request.getFechaInicio(), request.getFechaFin());

        log.info("Reporte resumen generado: {} días, {} turnos totales",
                resumen.getTotalDias(), resumen.getTotalTurnosGenerados());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(resumen, "Reporte resumen generado exitosamente")
        );
    }

    //Genera reporte del día actual
    //POST /api/estadisticas/reporte/hoy
    @PostMapping("/reporte/hoy")
    public ResponseEntity<ApiResponseWrapper<String>> generarReporteHoy() {

        log.info("Generando reporte del día actual");

        LocalDate hoy = LocalDate.now();
        estadisticaTurnoService.generarReporteDelDia(hoy);

        return ResponseEntity.ok(
                ApiResponseWrapper.success("OK",
                        String.format("Reporte del día %s generado exitosamente", hoy))
        );
    }

    //Genera reporte de un día específico
    //POST /api/estadisticas/reporte/fecha/{fecha}
    @PostMapping("/reporte/fecha/{fecha}")
    public ResponseEntity<ApiResponseWrapper<String>> generarReporteFecha(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha) {

        log.info("Generando reporte para fecha: {}", fecha);

        estadisticaTurnoService.generarReporteDelDia(fecha);

        return ResponseEntity.ok(
                ApiResponseWrapper.success("OK",
                        String.format("Reporte del día %s generado exitosamente", fecha))
        );
    }

    // ==========================================
    // ENDPOINTS DE ANÁLISIS ESPECÍFICOS
    // ==========================================

    //Calcula y actualiza la hora pico de un sector para una fecha
    //PUT /api/estadisticas/sector/{sectorId}/hora-pico/fecha/{fecha}
    @PutMapping("/sector/{sectorId}/hora-pico/fecha/{fecha}")
    public ResponseEntity<ApiResponseWrapper<EstadisticaTurnoResponse>> calcularHoraPico(
            @PathVariable Long sectorId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha) {

        log.info("Calculando hora pico para sector {} en fecha {}", sectorId, fecha);

        // Verificar que el sector existe
        Sector sector = sectorService.buscarPorId(sectorId)
                .orElseThrow(() -> ResourceNotFoundException.sector(sectorId));

        estadisticaTurnoService.calcularHoraPico(sectorId, fecha);

        // Obtener estadística actualizada
        EstadisticaTurno estadistica = estadisticaTurnoService.obtenerEstadisticaDelDia(sectorId, fecha);
        EstadisticaTurnoResponse response = EstadisticaTurnoMapper.toResponse(estadistica);

        log.info("Hora pico calculada para sector {}: {} con {} turnos",
                sector.getCodigo(), estadistica.getHoraPico(), estadistica.getCantidadPico());

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Hora pico calculada para sector %s", sector.getCodigo()))
        );
    }

    //Obtiene estadísticas de la semana actual
    //GET /api/estadisticas/semana-actual
    @GetMapping("/semana-actual")
    public ResponseEntity<ApiResponseWrapper<List<EstadisticaSummaryResponse>>> obtenerEstadisticasSemanaActual() {

        log.debug("Obteniendo estadísticas de la semana actual");

        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1); // Lunes de esta semana
        LocalDate finSemana = inicioSemana.plusDays(6); // Domingo de esta semana

        List<EstadisticaTurno> estadisticas = estadisticaTurnoService.obtenerEstadisticasGenerales(
                inicioSemana, finSemana);

        List<EstadisticaSummaryResponse> response = EstadisticaTurnoMapper.toSummaryResponseList(estadisticas);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Estadísticas de la semana del %s al %s (%d registros)",
                                inicioSemana, finSemana, estadisticas.size()))
        );
    }

    //Obtiene estadísticas del mes actual
    //GET /api/estadisticas/mes-actual
    @GetMapping("/mes-actual")
    public ResponseEntity<ApiResponseWrapper<List<EstadisticaSummaryResponse>>> obtenerEstadisticasMesActual() {

        log.debug("Obteniendo estadísticas del mes actual");

        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        List<EstadisticaTurno> estadisticas = estadisticaTurnoService.obtenerEstadisticasGenerales(
                inicioMes, finMes);

        List<EstadisticaSummaryResponse> response = EstadisticaTurnoMapper.toSummaryResponseList(estadisticas);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Estadísticas del mes %d/%d (%d registros)",
                                hoy.getMonthValue(), hoy.getYear(), estadisticas.size()))
        );
    }

    // ==========================================
    // ENDPOINTS DE COMPARACIÓN
    // ==========================================

    //Compara estadísticas de dos sectores en un período
    //GET /api/estadisticas/comparar/sectores?sector1=1&sector2=2&fechaInicio=2024-01-01&fechaFin=2024-01-31
    @GetMapping("/comparar/sectores")
    public ResponseEntity<ApiResponseWrapper<List<EstadisticaSummaryResponse>>> compararSectores(
            @RequestParam Long sector1,
            @RequestParam Long sector2,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin) {

        log.debug("Comparando sectores {} y {} entre {} y {}", sector1, sector2, fechaInicio, fechaFin);

        // Validar fechas
        if (fechaFin.isBefore(fechaInicio)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("La fecha de fin no puede ser anterior a la fecha de inicio",
                            "INVALID_DATE_RANGE")
            );
        }

        // Verificar que los sectores existen
        Sector sectorUno = sectorService.buscarPorId(sector1)
                .orElseThrow(() -> ResourceNotFoundException.sector(sector1));
        Sector sectorDos = sectorService.buscarPorId(sector2)
                .orElseThrow(() -> ResourceNotFoundException.sector(sector2));

        // Obtener estadísticas de ambos sectores
        List<EstadisticaTurno> estadisticasSector1 = estadisticaTurnoService.obtenerEstadisticasSector(
                sector1, fechaInicio, fechaFin);
        List<EstadisticaTurno> estadisticasSector2 = estadisticaTurnoService.obtenerEstadisticasSector(
                sector2, fechaInicio, fechaFin);

        // Combinar ambas listas para la respuesta
        List<EstadisticaTurno> estadisticasComparadas = new ArrayList<>();
        estadisticasComparadas.addAll(estadisticasSector1);
        estadisticasComparadas.addAll(estadisticasSector2);

        List<EstadisticaSummaryResponse> response = EstadisticaTurnoMapper.toSummaryResponseList(estadisticasComparadas);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Comparación entre %s (%d registros) y %s (%d registros)",
                                sectorUno.getCodigo(), estadisticasSector1.size(),
                                sectorDos.getCodigo(), estadisticasSector2.size()))
        );
    }

    //Compara estadísticas de dos empleados en un período
    //GET /api/estadisticas/comparar/empleados?empleado1=1&empleado2=2&fechaInicio=2024-01-01&fechaFin=2024-01-31
    @GetMapping("/comparar/empleados")
    public ResponseEntity<ApiResponseWrapper<List<EstadisticaSummaryResponse>>> compararEmpleados(
            @RequestParam Long empleado1,
            @RequestParam Long empleado2,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin) {

        log.debug("Comparando empleados {} y {} entre {} y {}", empleado1, empleado2, fechaInicio, fechaFin);

        // Validar fechas
        if (fechaFin.isBefore(fechaInicio)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("La fecha de fin no puede ser anterior a la fecha de inicio",
                            "INVALID_DATE_RANGE")
            );
        }

        // Verificar que los empleados existen
        Empleado empleadoUno = empleadoService.buscarPorId(empleado1)
                .orElseThrow(() -> ResourceNotFoundException.empleado(empleado1));
        Empleado empleadoDos = empleadoService.buscarPorId(empleado2)
                .orElseThrow(() -> ResourceNotFoundException.empleado(empleado2));

        // Obtener estadísticas de ambos empleados
        List<EstadisticaTurno> estadisticasEmpleado1 = estadisticaTurnoService.obtenerEstadisticasEmpleado(
                empleado1, fechaInicio, fechaFin);
        List<EstadisticaTurno> estadisticasEmpleado2 = estadisticaTurnoService.obtenerEstadisticasEmpleado(
                empleado2, fechaInicio, fechaFin);

        // Combinar ambas listas para la respuesta
        List<EstadisticaTurno> estadisticasComparadas = new ArrayList<>();
        estadisticasComparadas.addAll(estadisticasEmpleado1);
        estadisticasComparadas.addAll(estadisticasEmpleado2);

        List<EstadisticaSummaryResponse> response = EstadisticaTurnoMapper.toSummaryResponseList(estadisticasComparadas);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Comparación entre %s (%d registros) y %s (%d registros)",
                                empleadoUno.getNombreCompleto(), estadisticasEmpleado1.size(),
                                empleadoDos.getNombreCompleto(), estadisticasEmpleado2.size()))
        );
    }

    // ==========================================
    // ENDPOINTS DE CONSULTAS RÁPIDAS
    // ==========================================

    //Obtiene las últimas 7 fechas con estadísticas de un sector
    //GET /api/estadisticas/sector/{sectorId}/ultimos-dias
    @GetMapping("/sector/{sectorId}/ultimos-dias")
    public ResponseEntity<ApiResponseWrapper<List<EstadisticaSummaryResponse>>> obtenerUltimosDiasSector(
            @PathVariable Long sectorId,
            @RequestParam(defaultValue = "7") int dias) {

        log.debug("Obteniendo últimos {} días del sector {}", dias, sectorId);

        // Verificar que el sector existe
        Sector sector = sectorService.buscarPorId(sectorId)
                .orElseThrow(() -> ResourceNotFoundException.sector(sectorId));

        // Validar parámetro días
        if (dias < 1 || dias > 30) {
            return ResponseEntity.badRequest().body(
                    ApiResponseWrapper.error("El número de días debe estar entre 1 y 30",
                            "INVALID_DAYS_PARAMETER")
            );
        }

        LocalDate fechaFin = LocalDate.now();
        LocalDate fechaInicio = fechaFin.minusDays(dias - 1);

        List<EstadisticaTurno> estadisticas = estadisticaTurnoService.obtenerEstadisticasSector(
                sectorId, fechaInicio, fechaFin);

        List<EstadisticaSummaryResponse> response = EstadisticaTurnoMapper.toSummaryResponseList(estadisticas);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Últimos %d días del sector %s (%d registros)",
                                dias, sector.getCodigo(), estadisticas.size()))
        );
    }

    //Obtiene estadísticas resumidas de todos los sectores para una fecha
    //GET /api/estadisticas/resumen-diario/{fecha}
    @GetMapping("/resumen-diario/{fecha}")
    public ResponseEntity<ApiResponseWrapper<List<EstadisticaSummaryResponse>>> obtenerResumenDiario(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha) {

        log.debug("Obteniendo resumen diario para fecha {}", fecha);

        List<EstadisticaTurno> estadisticas = estadisticaTurnoService.obtenerEstadisticasGenerales(fecha, fecha);
        List<EstadisticaSummaryResponse> response = EstadisticaTurnoMapper.toSummaryResponseList(estadisticas);

        return ResponseEntity.ok(
                ApiResponseWrapper.success(response,
                        String.format("Resumen del día %s (%d sectores)", fecha, estadisticas.size()))
        );
    }

    //Obtiene estadísticas resumidas de hoy de todos los sectores
    //GET /api/estadisticas/resumen-hoy
    @GetMapping("/resumen-hoy")
    public ResponseEntity<ApiResponseWrapper<List<EstadisticaSummaryResponse>>> obtenerResumenHoy() {

        log.debug("Obteniendo resumen de hoy");

        return obtenerResumenDiario(LocalDate.now());
    }
}