package queue_san_antonio.queues.web.dto.mapper;

import queue_san_antonio.queues.models.Ciudadano;
import queue_san_antonio.queues.models.Turno;
import queue_san_antonio.queues.web.dto.turno.TurnoResponse;
import queue_san_antonio.queues.web.dto.turno.TurnoSummaryResponse;

import java.util.List;

// Mapper para conversiones entre Turno y DTOs
public class TurnoMapper {

    // Convierte Turno a TurnoResponse (información completa)
    public static TurnoResponse toResponse(Turno turno) {
        if (turno == null) return null;

        return TurnoResponse.builder()
                .id(turno.getId())
                .codigo(turno.getCodigo())
                .estado(turno.getEstado())
                .tipo(turno.getTipo())
                .prioridad(turno.getPrioridad())
                .ciudadano(toCiudadanoInfo(turno))
                .sector(toSectorInfo(turno))
                // Sólo empleado de atención (el DTO actual no expone empleadoLlamada)
                .empleadoAtencion(toEmpleadoInfo(turno))
                // Fechas
                .fechaGeneracion(turno.getFechaHoraGeneracion())
                .fechaLlamado(turno.getFechaHoraLlamado())
                .fechaAtencion(turno.getFechaHoraAtencion())
                .fechaFinalizacion(turno.getFechaHoraFinalizacion())
                // Cita (si aplica)
                .fechaCita(turno.getFechaCita())
                .horaCita(turno.getHoraCita())
                // Otros
                .observaciones(turno.getObservaciones())
                .tiempoEspera(turno.getTiempoEsperaminutos())
                .tiempoAtencion(turno.getTiempoAtencionMinutos())
                .build();
    }

    // Convierte Turno a TurnoSummaryResponse (información resumida)
    public static TurnoSummaryResponse toSummaryResponse(Turno turno) {
        if (turno == null) return null;

        return TurnoSummaryResponse.builder()
                .id(turno.getId())
                .codigo(turno.getCodigo())
                .estado(turno.getEstado())
                .tipo(turno.getTipo())
                .prioridad(turno.getPrioridad())
                .ciudadanoNombre(turno.getCiudadano() != null ? turno.getCiudadano().getNombreCompleto() : null)
                .ciudadanoDni(turno.getCiudadano() != null ? turno.getCiudadano().getDni() : null)
                .sectorCodigo(turno.getSector() != null ? turno.getSector().getCodigo() : null)
                .sectorNombre(turno.getSector() != null ? turno.getSector().getNombre() : null)
                .empleadoUsername(turno.getEmpleadoAtencion() != null ? turno.getEmpleadoAtencion().getUsername() : null)
                .fechaGeneracion(turno.getFechaHoraGeneracion())
                .tiempoEspera(turno.getTiempoEsperaminutos())
                .build();
    }

    // Convierte lista de Turnos a TurnoResponse
    public static List<TurnoResponse> toResponseList(List<Turno> turnos) {
        return turnos.stream()
                .map(TurnoMapper::toResponse)
                .toList();
    }

    // Convierte lista de Turnos a TurnoSummaryResponse
    public static List<TurnoSummaryResponse> toSummaryResponseList(List<Turno> turnos) {
        return turnos.stream()
                .map(TurnoMapper::toSummaryResponse)
                .toList();
    }

    // ==========================================
    // MÉTODOS PRIVADOS PARA INFORMACIÓN ANIDADA
    // ==========================================

    private static TurnoResponse.CiudadanoInfo toCiudadanoInfo(Turno turno) {
        if (turno.getCiudadano() == null) return null;

        Ciudadano ciudadano = turno.getCiudadano();

        // Fallback por si el nombre completo trae "null" o vacío
        String nombreCompleto;
        try {
            nombreCompleto = ciudadano.getNombreCompleto();
            if (nombreCompleto == null || nombreCompleto.contains("null") || nombreCompleto.trim().isEmpty()) {
                nombreCompleto = String.format("Ciudadano DNI: %s", ciudadano.getDni());
            }
        } catch (Exception e) {
            nombreCompleto = String.format("Ciudadano DNI: %s", ciudadano.getDni());
        }

        return TurnoResponse.CiudadanoInfo.builder()
                .id(ciudadano.getId())
                .dni(ciudadano.getDni())
                .nombreCompleto(nombreCompleto)
                .esPrioritario(ciudadano.getEsPrioritario())
                .build();
    }

    private static TurnoResponse.SectorInfo toSectorInfo(Turno turno) {
        if (turno.getSector() == null) return null;

        return TurnoResponse.SectorInfo.builder()
                .id(turno.getSector().getId())
                .codigo(turno.getSector().getCodigo())
                .nombre(turno.getSector().getNombre())
                .tipo(turno.getSector().getTipoSector().name())
                .build();
    }

    private static TurnoResponse.EmpleadoInfo toEmpleadoInfo(Turno turno) {
        if (turno.getEmpleadoAtencion() == null) return null;

        return TurnoResponse.EmpleadoInfo.builder()
                .id(turno.getEmpleadoAtencion().getId())
                .username(turno.getEmpleadoAtencion().getUsername())
                .nombreCompleto(turno.getEmpleadoAtencion().getNombreCompleto())
                .build();
    }
}
