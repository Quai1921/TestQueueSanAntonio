package queue_san_antonio.queues.web.dto.mapper;

import queue_san_antonio.queues.models.Turno;
import queue_san_antonio.queues.web.dto.turno.TurnoResponse;
import queue_san_antonio.queues.web.dto.turno.TurnoSummaryResponse;

import java.util.List;

//Mapper para conversiones entre Turno y DTOs
public class TurnoMapper {

    //Convierte Turno a TurnoResponse (información completa)
    public static TurnoResponse toResponse(Turno turno) {
        if (turno == null) return null;

        return TurnoResponse.builder()
                .codigo(turno.getCodigo())
                .estado(turno.getEstado())
                .tipo(turno.getTipo())
                .prioridad(turno.getPrioridad())
                .ciudadano(toCiudadanoInfo(turno))
                .sector(toSectorInfo(turno))
                .empleadoAtencion(toEmpleadoInfo(turno))
                .fechaGeneracion(turno.getFechaHoraGeneracion())
                .fechaLlamado(turno.getFechaHoraLlamado())
                .fechaAtencion(turno.getFechaHoraAtencion())
                .fechaFinalizacion(turno.getFechaHoraFinalizacion())
                .fechaCita(turno.getFechaCita())
                .horaCita(turno.getHoraCita())
                .observaciones(turno.getObservaciones())
                .tiempoEspera(turno.getTiempoEsperaminutos())
                .tiempoAtencion(turno.getTiempoAtencionMinutos())
                .build();
    }

    //Convierte Turno a TurnoSummaryResponse (información resumida)
    public static TurnoSummaryResponse toSummaryResponse(Turno turno) {
        if (turno == null) return null;

        return TurnoSummaryResponse.builder()
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

    //Convierte lista de Turnos a TurnoResponse
    public static List<TurnoResponse> toResponseList(List<Turno> turnos) {
        return turnos.stream()
                .map(TurnoMapper::toResponse)
                .toList();
    }

    //Convierte lista de Turnos a TurnoSummaryResponse
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

        return TurnoResponse.CiudadanoInfo.builder()
                .dni(turno.getCiudadano().getDni())
                .nombreCompleto(turno.getCiudadano().getNombreCompleto())
                .esPrioritario(turno.getCiudadano().getEsPrioritario())
                .build();
    }

    private static TurnoResponse.SectorInfo toSectorInfo(Turno turno) {
        if (turno.getSector() == null) return null;

        return TurnoResponse.SectorInfo.builder()
                .codigo(turno.getSector().getCodigo())
                .nombre(turno.getSector().getNombre())
                .tipo(turno.getSector().getTipoSector().name())
                .build();
    }

    private static TurnoResponse.EmpleadoInfo toEmpleadoInfo(Turno turno) {
        if (turno.getEmpleadoAtencion() == null) return null;

        return TurnoResponse.EmpleadoInfo.builder()
                .username(turno.getEmpleadoAtencion().getUsername())
                .nombreCompleto(turno.getEmpleadoAtencion().getNombreCompleto())
                .build();
    }
}