package queue_san_antonio.queues.web.dto.mapper;

import queue_san_antonio.queues.models.AccionTurno;
import queue_san_antonio.queues.models.HistorialTurno;
import queue_san_antonio.queues.web.dto.historial.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//Mapper para conversiones entre HistorialTurno y DTOs
public class HistorialTurnoMapper {

    //Convierte HistorialTurno a HistorialTurnoResponse (información completa)
    public static HistorialTurnoResponse toResponse(HistorialTurno historial) {
        if (historial == null) return null;

        return HistorialTurnoResponse.builder()
                .id(historial.getId())
                .turno(toTurnoInfo(historial))
                .accion(historial.getAccion())
                .descripcionAccion(historial.getDescripcionAccion())
                .empleado(toEmpleadoInfo(historial))
                .fechaHora(historial.getFechaHora())
                .observaciones(historial.getObservaciones())
                .motivo(historial.getMotivo())
                .estadoAnterior(historial.getEstadoAnterior())
                .estadoNuevo(historial.getEstadoNuevo())
                .esCambioEstado(historial.esCambioEstado())
                .prioridadAnterior(historial.getPrioridadAnterior())
                .prioridadNueva(historial.getPrioridadNueva())
                .esCambioPrioridad(historial.esCambioPrioridad())
                .sectorOrigen(toSectorInfo(historial.getSectorOrigen()))
                .sectorDestino(toSectorInfo(historial.getSectorDestino()))
                .esRedireccion(historial.esRedireccion())
                .build();
    }

    //Convierte HistorialTurno a HistorialSummaryResponse (información resumida)
    public static HistorialSummaryResponse toSummaryResponse(HistorialTurno historial) {
        if (historial == null) return null;

        return HistorialSummaryResponse.builder()
                .id(historial.getId())
                .turnoCodigo(historial.getTurno() != null ? historial.getTurno().getCodigo() : null)
                .accion(historial.getAccion())
                .descripcionAccion(historial.getDescripcionAccion())
                .empleadoNombre(historial.getNombreEmpleado())
                .empleadoUsername(historial.getEmpleado() != null ? historial.getEmpleado().getUsername() : null)
                .fechaHora(historial.getFechaHora())
                .observaciones(historial.getObservaciones())
                .motivo(historial.getMotivo())
                .sectorOrigenCodigo(historial.getSectorOrigen() != null ? historial.getSectorOrigen().getCodigo() : null)
                .sectorDestinoCodigo(historial.getSectorDestino() != null ? historial.getSectorDestino().getCodigo() : null)
                .cambioEstado(formatearCambioEstado(historial))
                .cambioPrioridad(formatearCambioPrioridad(historial))
                .build();
    }

    //Convierte lista de HistorialTurno a lista de Response
    public static List<HistorialTurnoResponse> toResponseList(List<HistorialTurno> historiales) {
        if (historiales == null) return null;
        return historiales.stream()
                .map(HistorialTurnoMapper::toResponse)
                .toList();
    }

    //Convierte lista de HistorialTurno a lista de SummaryResponse
    public static List<HistorialSummaryResponse> toSummaryResponseList(List<HistorialTurno> historiales) {
        if (historiales == null) return null;
        return historiales.stream()
                .map(HistorialTurnoMapper::toSummaryResponse)
                .toList();
    }

    //Crea un resumen de auditoría para un empleado
    public static ResumenAuditoriaResponse toResumenAuditoria(List<HistorialTurno> historiales,
                                                              LocalDate fechaInicio,
                                                              LocalDate fechaFin,
                                                              String empleadoUsername,
                                                              String empleadoNombreCompleto,
                                                              String sectorCodigo,
                                                              String sectorNombre) {
        if (historiales == null || historiales.isEmpty()) {
            return ResumenAuditoriaResponse.builder()
                    .fechaInicio(fechaInicio)
                    .fechaFin(fechaFin)
                    .totalDias(0)
                    .empleadoUsername(empleadoUsername)
                    .empleadoNombreCompleto(empleadoNombreCompleto)
                    .sectorCodigo(sectorCodigo)
                    .sectorNombre(sectorNombre)
                    .totalAcciones(0)
                    .accionesPorTipo(Map.of())
                    .turnosGenerados(0)
                    .turnosAtendidos(0)
                    .turnosFinalizados(0)
                    .turnosRedirigidos(0)
                    .turnosAusentes(0)
                    .promedioAccionesPorDia(0.0)
                    .ultimasAcciones(List.of())
                    .build();
        }

        // Contadores por tipo de acción
        Map<AccionTurno, Integer> accionesPorTipo = historiales.stream()
                .collect(Collectors.groupingBy(
                        HistorialTurno::getAccion,
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

        // Contadores específicos
        int turnosGenerados = accionesPorTipo.getOrDefault(AccionTurno.GENERADO, 0);
        int turnosAtendidos = accionesPorTipo.getOrDefault(AccionTurno.INICIADA_ATENCION, 0);
        int turnosFinalizados = accionesPorTipo.getOrDefault(AccionTurno.FINALIZADA_ATENCION, 0);
        int turnosRedirigidos = accionesPorTipo.getOrDefault(AccionTurno.REDIRIGIDO, 0);
        int turnosAusentes = accionesPorTipo.getOrDefault(AccionTurno.MARCADO_AUSENTE, 0);

        // Análisis de patrones
        String diaConMasActividad = encontrarDiaConMasActividad(historiales);
        String accionMasFrecuente = encontrarAccionMasFrecuente(accionesPorTipo);

        // Calcular días únicos
        long diasUnicos = historiales.stream()
                .map(h -> h.getFechaHora().toLocalDate())
                .distinct()
                .count();

        double promedioAccionesPorDia = diasUnicos > 0 ? (double) historiales.size() / diasUnicos : 0.0;

        // Últimas 10 acciones
        List<HistorialSummaryResponse> ultimasAcciones = historiales.stream()
                .sorted((h1, h2) -> h2.getFechaHora().compareTo(h1.getFechaHora()))
                .limit(10)
                .map(HistorialTurnoMapper::toSummaryResponse)
                .toList();

        return ResumenAuditoriaResponse.builder()
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .totalDias((int) diasUnicos)
                .empleadoUsername(empleadoUsername)
                .empleadoNombreCompleto(empleadoNombreCompleto)
                .sectorCodigo(sectorCodigo)
                .sectorNombre(sectorNombre)
                .totalAcciones(historiales.size())
                .accionesPorTipo(accionesPorTipo)
                .turnosGenerados(turnosGenerados)
                .turnosAtendidos(turnosAtendidos)
                .turnosFinalizados(turnosFinalizados)
                .turnosRedirigidos(turnosRedirigidos)
                .turnosAusentes(turnosAusentes)
                .diaConMasActividad(diaConMasActividad)
                .accionMasFrecuente(accionMasFrecuente)
                .promedioAccionesPorDia(Math.round(promedioAccionesPorDia * 100.0) / 100.0)
                .ultimasAcciones(ultimasAcciones)
                .build();
    }

    //Crea respuesta de historial legible
    public static HistorialTurnoLegibleResponse toHistorialLegible(List<String> historialLegible,
                                                                   String turnoCodigo,
                                                                   String estadoActual,
                                                                   String fechaGeneracion,
                                                                   String fechaFinalizacion,
                                                                   Boolean completado) {
        if (historialLegible == null) {
            historialLegible = List.of();
        }

        String resumen = generarResumenHistorial(historialLegible, completado);

        return HistorialTurnoLegibleResponse.builder()
                .turnoCodigo(turnoCodigo)
                .totalAcciones(historialLegible.size())
                .resumen(resumen)
                .historialLegible(historialLegible)
                .estadoActual(estadoActual)
                .fechaGeneracion(fechaGeneracion)
                .fechaFinalizacion(fechaFinalizacion)
                .completado(completado != null ? completado : false)
                .build();
    }

    // ==========================================
    // MÉTODOS HELPER PRIVADOS
    // ==========================================

    //Convierte información del turno
    private static HistorialTurnoResponse.TurnoInfo toTurnoInfo(HistorialTurno historial) {
        if (historial.getTurno() == null) return null;

        var turno = historial.getTurno();
        return HistorialTurnoResponse.TurnoInfo.builder()
                .id(turno.getId())
                .codigo(turno.getCodigo())
                .estadoActual(turno.getEstado())
                .ciudadanoDni(turno.getCiudadano() != null ? turno.getCiudadano().getDni() : null)
                .ciudadanoNombre(turno.getCiudadano() != null ? turno.getCiudadano().getNombreCompleto() : null)
                .build();
    }

    //Convierte información del empleado
    private static HistorialTurnoResponse.EmpleadoInfo toEmpleadoInfo(HistorialTurno historial) {
        if (historial.getEmpleado() == null) return null;

        var empleado = historial.getEmpleado();
        return HistorialTurnoResponse.EmpleadoInfo.builder()
                .id(empleado.getId())
                .username(empleado.getUsername())
                .nombreCompleto(empleado.getNombreCompleto())
                .rol(empleado.getRol().name())
                .build();
    }

    //Convierte información del sector
    private static HistorialTurnoResponse.SectorInfo toSectorInfo(queue_san_antonio.queues.models.Sector sector) {
        if (sector == null) return null;

        return HistorialTurnoResponse.SectorInfo.builder()
                .id(sector.getId())
                .codigo(sector.getCodigo())
                .nombre(sector.getNombre())
                .build();
    }

    //Formatea cambio de estado para el summary
    private static String formatearCambioEstado(HistorialTurno historial) {
        if (!historial.esCambioEstado()) return null;

        return String.format("%s → %s",
                historial.getEstadoAnterior(),
                historial.getEstadoNuevo());
    }

    //Formatea cambio de prioridad para el summary
    private static String formatearCambioPrioridad(HistorialTurno historial) {
        if (!historial.esCambioPrioridad()) return null;

        return String.format("%d → %d",
                historial.getPrioridadAnterior(),
                historial.getPrioridadNueva());
    }

    //Encuentra el día con más actividad
    private static String encontrarDiaConMasActividad(List<HistorialTurno> historiales) {
        Map<LocalDate, Long> accionesPorDia = historiales.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getFechaHora().toLocalDate(),
                        Collectors.counting()
                ));

        return accionesPorDia.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .orElse("N/A");
    }

    //Encuentra la acción más frecuente
    private static String encontrarAccionMasFrecuente(Map<AccionTurno, Integer> accionesPorTipo) {
        return accionesPorTipo.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey().name())
                .orElse("N/A");
    }

    //Genera un resumen textual del historial
    private static String generarResumenHistorial(List<String> historialLegible, Boolean completado) {
        if (historialLegible.isEmpty()) {
            return "Sin acciones registradas";
        }

        StringBuilder resumen = new StringBuilder();
        resumen.append("Turno con ").append(historialLegible.size()).append(" acciones registradas. ");

        if (completado != null && completado) {
            resumen.append("Procesamiento completado.");
        } else {
            resumen.append("En proceso.");
        }

        return resumen.toString();
    }
}