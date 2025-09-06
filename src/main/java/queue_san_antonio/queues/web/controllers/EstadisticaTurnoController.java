package queue_san_antonio.queues.web.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import queue_san_antonio.queues.models.*;
import queue_san_antonio.queues.services.EstadisticaTurnoService;
import queue_san_antonio.queues.web.dto.common.ApiResponseWrapper;
import queue_san_antonio.queues.web.dto.estadistica.*;
import queue_san_antonio.queues.web.dto.mapper.EstadisticaTurnoMapper;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

//Controlador REST para la gestión de estadísticas de turnos
//Proporciona endpoints para consultas y reportes estadísticos
@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RESPONSABLE_SECTOR', 'ADMIN')")
public class EstadisticaTurnoController {

    private static final ZoneId CBA_TZ = ZoneId.of("America/Argentina/Cordoba");

    private final EstadisticaTurnoService estadisticaTurnoService;

    // ======================================================
    // 1) DIARIO (hoy/fecha) - resumen o detalle
    // ======================================================
    // prueba
    // Ejemplos:
    //  - GET /api/estadisticas/diario
    //  - GET /api/estadisticas/diario?fecha=2025-09-02
    //  - GET /api/estadisticas/diario?sectorId=1
    //  - GET /api/estadisticas/diario?sectorId=1&fecha=2025-09-02
    //  - GET /api/estadisticas/diario?sectorId=1&empleadoId=7&fecha=2025-09-02
    //
    @GetMapping("/diario")
    @PreAuthorize("hasAnyRole('OPERADOR','RESPONSABLE_SECTOR','ADMIN')")
    public ResponseEntity<ApiResponseWrapper<?>> getDiario(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Long sectorId,
            @RequestParam(required = false) Long empleadoId
    ) {
        LocalDate dia = (fecha != null) ? fecha : LocalDate.now(CBA_TZ);

        if (sectorId != null && empleadoId != null) {
            // Detalle por empleado en un sector (día)
            EstadisticaTurno est = estadisticaTurnoService.obtenerEstadisticaEmpleado(empleadoId, sectorId, dia);
            EstadisticaTurnoResponse resp = EstadisticaTurnoMapper.toResponse(est);
            return ResponseEntity.ok(ApiResponseWrapper.success(resp, "Detalle empleado/sector del día"));
        }

        if (sectorId != null) {
            // Detalle de un sector (día)
            EstadisticaTurno est = estadisticaTurnoService.obtenerEstadisticaDelDia(sectorId, dia);
            EstadisticaTurnoResponse resp = EstadisticaTurnoMapper.toResponse(est);
            return ResponseEntity.ok(ApiResponseWrapper.success(resp, "Detalle sector del día"));
        }

        // Resumen por sector (día)
        List<EstadisticaTurno> lista = estadisticaTurnoService.obtenerEstadisticasGenerales(dia, dia);
        List<EstadisticaSummaryResponse> resumen = EstadisticaTurnoMapper.toSummaryResponseList(lista);
        return ResponseEntity.ok(ApiResponseWrapper.success(resumen, "Resumen diario"));
    }

    // ======================================================
    // 2) PERIODO (rango) - múltiples sectores y agrupación
    // ======================================================
    //
    // Ejemplos:
    //  - GET /api/estadisticas/periodo?desde=2025-08-10&hasta=2025-09-02
    //  - GET /api/estadisticas/periodo?desde=...&hasta=...&sectorId=1,2
    //  - GET /api/estadisticas/periodo?desde=...&hasta=...&groupBy=FECHA
    //  - GET /api/estadisticas/periodo?desde=...&hasta=...&groupBy=SECTOR
    //
    @GetMapping("/periodo")
    @PreAuthorize("hasAnyRole('OPERADOR','RESPONSABLE_SECTOR','ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<EstadisticaTurnoResponse>>> getPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false, name = "sectorId") String sectorIdsCsv, // CSV: "1,2,3"
            @RequestParam(required = false, defaultValue = "FECHA_SECTOR") GroupBy groupBy
    ) {
        List<Long> sectorIds = parseCsvIds(sectorIdsCsv);

        // 1) Traer entities con TU service actual
        List<EstadisticaTurno> baseEntities = new ArrayList<>();
        if (!sectorIds.isEmpty()) {
            for (Long id : sectorIds) {
                baseEntities.addAll(estadisticaTurnoService.obtenerEstadisticasSector(id, desde, hasta));
            }
        } else {
            baseEntities = estadisticaTurnoService.obtenerEstadisticasGenerales(desde, hasta);
        }

        // 2) Mapear a DTOs completos
        List<EstadisticaTurnoResponse> base = EstadisticaTurnoMapper.toResponseList(baseEntities);

        // 3) Agrupar en memoria según parámetro
        List<EstadisticaTurnoResponse> salida = agrupar(base, groupBy);

        return ResponseEntity.ok(ApiResponseWrapper.success(salida, "Estadísticas del período"));
    }

    // ======================================================
    // Aliases de compatibilidad (opcional)
    // ======================================================

    @GetMapping("/resumen-hoy")
    @PreAuthorize("hasAnyRole('OPERADOR','RESPONSABLE_SECTOR','ADMIN')")
    public ResponseEntity<ApiResponseWrapper<?>> aliasResumenHoy() {
        return getDiario(null, null, null);
    }

    @GetMapping("/sector/{sectorId}/hoy")
    @PreAuthorize("hasAnyRole('OPERADOR','RESPONSABLE_SECTOR','ADMIN')")
    public ResponseEntity<ApiResponseWrapper<?>> aliasSectorHoy(@PathVariable Long sectorId) {
        return getDiario(null, sectorId, null);
    }

    @GetMapping("/sector/{sectorId}/fecha/{fecha}")
    @PreAuthorize("hasAnyRole('OPERADOR','RESPONSABLE_SECTOR','ADMIN')")
    public ResponseEntity<ApiResponseWrapper<?>> aliasSectorFecha(
            @PathVariable Long sectorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return getDiario(fecha, sectorId, null);
    }

    @GetMapping("/empleado/{empleadoId}/sector/{sectorId}/fecha/{fecha}")
    @PreAuthorize("hasAnyRole('OPERADOR','RESPONSABLE_SECTOR','ADMIN')")
    public ResponseEntity<ApiResponseWrapper<?>> aliasEmpleadoSectorFecha(
            @PathVariable Long empleadoId,
            @PathVariable Long sectorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return getDiario(fecha, sectorId, empleadoId);
    }

    @GetMapping("/general/periodo")
    @PreAuthorize("hasAnyRole('OPERADOR','RESPONSABLE_SECTOR','ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<EstadisticaTurnoResponse>>> aliasGeneralPeriodo(
            @RequestParam(name = "fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return getPeriodo(fechaInicio, fechaFin, null, GroupBy.FECHA_SECTOR);
    }

    @GetMapping("/comparar/sectores")
    @PreAuthorize("hasAnyRole('OPERADOR','RESPONSABLE_SECTOR','ADMIN')")
    public ResponseEntity<ApiResponseWrapper<List<EstadisticaTurnoResponse>>> aliasCompararSectores(
            @RequestParam String sector1,
            @RequestParam String sector2,
            @RequestParam(name = "fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        String csv = sector1 + "," + sector2;
        return getPeriodo(fechaInicio, fechaFin, csv, GroupBy.FECHA_SECTOR);
    }

    // ======================================================
    // Helpers
    // ======================================================

    private List<Long> parseCsvIds(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * Agrupa lista por:
     *  - FECHA        -> suma por fecha
     *  - SECTOR       -> suma por sector (across fechas)
     *  - FECHA_SECTOR -> (default) deja una fila por fecha/sector (sin re-agrupación)
     */
    private List<EstadisticaTurnoResponse> agrupar(List<EstadisticaTurnoResponse> base, GroupBy groupBy) {
        if (groupBy == GroupBy.FECHA_SECTOR) return base;

        record Key(LocalDate fecha, String sectorCodigo, String sectorNombre) {}
        Map<Key, Acum> mapa = new LinkedHashMap<>();

        for (var r : base) {
            LocalDate f = r.getFecha();
            String sc = (r.getSector() != null) ? r.getSector().getCodigo() : null;
            String sn = (r.getSector() != null) ? r.getSector().getNombre() : null;

            Key key = switch (groupBy) {
                case FECHA  -> new Key(f, null, null);
                case SECTOR -> new Key(null, sc, sn);
                default     -> new Key(f, sc, sn);
            };

            mapa.computeIfAbsent(key, k -> new Acum(sc, sn)).add(r);
        }

        List<EstadisticaTurnoResponse> out = new ArrayList<>();
        for (var e : mapa.entrySet()) {
            var k = e.getKey();
            var a = e.getValue();

            EstadisticaTurnoResponse dto = new EstadisticaTurnoResponse();
            dto.setFecha(k.fecha());

            if (k.sectorCodigo() != null || k.sectorNombre() != null) {
                var si = new EstadisticaTurnoResponse.SectorInfo();
                si.setCodigo(k.sectorCodigo());
                si.setNombre(k.sectorNombre());
                dto.setSector(si);
            }

            // Conteos
            dto.setTurnosGenerados((int) a.gen);
            dto.setTurnosAtendidos((int) a.att);
            dto.setTurnosAusentes((int) a.aus);
            dto.setTurnosCancelados((int) a.can);
            dto.setTurnosRedirigidos((int) a.red);

            // Promedios (ponderados donde tiene sentido)
            dto.setTiempoPromedioEspera(a.promEspera());        // simple, sobre los que aportan
            dto.setTiempoPromedioAtencion(a.promAtencion());    // ponderado por atendidos
            dto.setTiempoTotalAtencion(a.totalAtencion());      // suma(promAtencion * atendidos)

            // Derivados útiles para FECHA/SECTOR (evitá nulls)
            int generados = (int) a.gen;
            int atendidos = (int) a.att;
            int ausentes  = (int) a.aus;

            dto.setTotalTurnos(generados);  // mismo criterio que en diario
            if (generados > 0) {
                dto.setPorcentajeEficiencia( (atendidos * 100.0) / generados );
                dto.setPorcentajeAusencias( (ausentes * 100.0) / generados );
            } else {
                dto.setPorcentajeEficiencia(0.0);
                dto.setPorcentajeAusencias(0.0);
            }

            // Pico: nos quedamos con el mayor
            dto.setHoraPico(a.horaPico);
            dto.setCantidadPico(a.cantPico);

            // Min/Max espera
            dto.setTiempoMinimoEspera(a.minEspera());
            dto.setTiempoMaximoEspera(a.maxEspera());

            // fechaActualizacion: al agrupar no tiene un único valor → lo dejamos null
            out.add(dto);
        }
        return out;
    }

    // Acumulador con ponderación de atención y min/max de espera
    static class Acum {
        final String sc; final String sn;
        long gen=0, att=0, aus=0, can=0, red=0;

        long sumEspera=0, cntEspera=0;
        Integer minEspera=null, maxEspera=null;

        long sumAtencionWeighted=0; // suma de (promAtencion * atendidos)
        long attConProm=0;          // para prom ponderado

        LocalTime horaPico = null;
        Integer cantPico = 0;

        Acum(String sc, String sn){ this.sc=sc; this.sn=sn; }

        void add(EstadisticaTurnoResponse r){
            int g = nz(r.getTurnosGenerados());
            int a = nz(r.getTurnosAtendidos());
            int u = nz(r.getTurnosAusentes());
            int c = nz(r.getTurnosCancelados());
            int d = nz(r.getTurnosRedirigidos());

            gen += g; att += a; aus += u; can += c; red += d;

            // Espera: si el dto trae promedio>0, lo promediamos simple y trackeamos min/max
            Integer pe = r.getTiempoPromedioEspera();
            if (pe != null && pe > 0) {
                sumEspera += pe;
                cntEspera++;
                if (minEspera == null || pe < minEspera) minEspera = pe;
                if (maxEspera == null || pe > maxEspera) maxEspera = pe;
            }

            // Atención: pondero por 'atendidos' para que escale
            Integer pa = r.getTiempoPromedioAtencion();
            if (pa != null && pa > 0 && a > 0) {
                sumAtencionWeighted += (long) pa * a;
                attConProm += a;
            }

            // Pico
            if (r.getCantidadPico() != null) {
                if (cantPico == null || r.getCantidadPico() > cantPico) {
                    cantPico = r.getCantidadPico();
                    horaPico = r.getHoraPico();
                }
            }
        }

        Integer promEspera(){ return cntEspera == 0 ? 0 : (int)(sumEspera / cntEspera); }
        Integer promAtencion(){ return attConProm == 0 ? 0 : (int)(sumAtencionWeighted / attConProm); }
        Integer totalAtencion(){ return (int) sumAtencionWeighted; } // suma total de minutos
        Integer minEspera(){ return minEspera; }
        Integer maxEspera(){ return maxEspera; }

        private static int nz(Integer v){ return v == null ? 0 : v; }
    }

    public enum GroupBy {
        FECHA, SECTOR, FECHA_SECTOR
    }
}