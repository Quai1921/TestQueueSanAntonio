package queue_san_antonio.queues.web.dto.mapper;

import queue_san_antonio.queues.models.HorarioAtencion;
import queue_san_antonio.queues.web.dto.horario.HorarioAtencionRequest;
import queue_san_antonio.queues.web.dto.horario.HorarioAtencionResponse;

import java.time.Duration;
import java.util.List;

//Mapper para conversiones entre HorarioAtencion y DTOs
public class HorarioAtencionMapper {

    // ==========================================
    // CONVERSIONES ENTITY ↔ REQUEST
    // ==========================================

    //Convierte HorarioAtencionRequest a HorarioAtencion
    //Nota: No incluye sector, debe asignarse después
    public static HorarioAtencion toEntity(HorarioAtencionRequest request) {
        if (request == null) return null;

        return HorarioAtencion.builder()
                .diaSemana(request.getDiaSemana())
                .horaInicio(request.getHoraInicio())
                .horaFin(request.getHoraFin())
                .intervaloCitas(request.getIntervaloCitas() != null ? request.getIntervaloCitas() : 30)
                .capacidadMaxima(request.getCapacidadMaxima() != null ? request.getCapacidadMaxima() : 1)
                .observaciones(request.getObservacionesLimpias())
                .activo(true) // Los horarios nuevos se crean activos
                .build();
    }

    //Actualiza HorarioAtencion existente con datos del request
    public static void updateEntity(HorarioAtencion horario, HorarioAtencionRequest request) {
        if (horario == null || request == null) return;

        if (request.getDiaSemana() != null) {
            horario.setDiaSemana(request.getDiaSemana());
        }
        if (request.getHoraInicio() != null) {
            horario.setHoraInicio(request.getHoraInicio());
        }
        if (request.getHoraFin() != null) {
            horario.setHoraFin(request.getHoraFin());
        }
        if (request.getIntervaloCitas() != null) {
            horario.setIntervaloCitas(request.getIntervaloCitas());
        }
        if (request.getCapacidadMaxima() != null) {
            horario.setCapacidadMaxima(request.getCapacidadMaxima());
        }
        if (request.getObservacionesLimpias() != null) {
            horario.setObservaciones(request.getObservacionesLimpias());
        }
    }

    // ==========================================
    // CONVERSIONES ENTITY → RESPONSE
    // ==========================================

    //Convierte HorarioAtencion a HorarioAtencionResponse completo
    public static HorarioAtencionResponse toResponse(HorarioAtencion horario) {
        if (horario == null) return null;

        return HorarioAtencionResponse.builder()
                .id(horario.getId())
                .diaSemana(horario.getDiaSemana())
                .diaSemanaTexto(horario.getDiaSemanaTexto())
                .horaInicio(horario.getHoraInicio())
                .horaFin(horario.getHoraFin())
                .intervaloCitas(horario.getIntervaloCitas())
                .capacidadMaxima(horario.getCapacidadMaxima())
                .activo(horario.getActivo())
                .observaciones(horario.getObservaciones())
                .fechaCreacion(horario.getFechaCreacion())
                .sector(toSectorInfo(horario))
                .horariosDisponibles(horario.getHorariosDisponibles())
                .duracionMinutos(calcularDuracionMinutos(horario))
                .cantidadTeoricaCitas(calcularCantidadTeoricaCitas(horario))
                .descripcionCompleta(horario.getDescripcionCompleta())
                .build();
    }

    //Convierte HorarioAtencion a HorarioAtencionResponse resumido (sin horarios disponibles)
    public static HorarioAtencionResponse toSummaryResponse(HorarioAtencion horario) {
        if (horario == null) return null;

        return HorarioAtencionResponse.builder()
                .id(horario.getId())
                .diaSemana(horario.getDiaSemana())
                .diaSemanaTexto(horario.getDiaSemanaTexto())
                .horaInicio(horario.getHoraInicio())
                .horaFin(horario.getHoraFin())
                .intervaloCitas(horario.getIntervaloCitas())
                .capacidadMaxima(horario.getCapacidadMaxima())
                .activo(horario.getActivo())
                .observaciones(horario.getObservaciones())
                .fechaCreacion(horario.getFechaCreacion())
                .sector(toSectorInfo(horario))
                .duracionMinutos(calcularDuracionMinutos(horario))
                .cantidadTeoricaCitas(calcularCantidadTeoricaCitas(horario))
                .descripcionCompleta(horario.getDescripcionCompleta())
                .build();
    }

    //Convierte lista de HorarioAtencion a HorarioAtencionResponse
    public static List<HorarioAtencionResponse> toResponseList(List<HorarioAtencion> horarios) {
        return horarios.stream()
                .map(HorarioAtencionMapper::toResponse)
                .toList();
    }

    //Convierte lista de HorarioAtencion a HorarioAtencionResponse resumido
    public static List<HorarioAtencionResponse> toSummaryResponseList(List<HorarioAtencion> horarios) {
        return horarios.stream()
                .map(HorarioAtencionMapper::toSummaryResponse)
                .toList();
    }

    // ==========================================
    // MÉTODOS PRIVADOS HELPER
    // ==========================================

    //Crea información básica del sector
    private static HorarioAtencionResponse.SectorInfo toSectorInfo(HorarioAtencion horario) {
        if (horario.getSector() == null) return null;

        return HorarioAtencionResponse.SectorInfo.builder()
                .id(horario.getSector().getId())
                .codigo(horario.getSector().getCodigo())
                .nombre(horario.getSector().getNombre())
                .nombreCompleto(horario.getSector().getNombreCompleto())
                .tipoSector(horario.getSector().getTipoSector().name())
                .requiereCitaPrevia(horario.getSector().getRequiereCitaPrevia())
                .build();
    }

    //Calcula la duración en minutos del horario
    private static Long calcularDuracionMinutos(HorarioAtencion horario) {
        if (horario.getHoraInicio() == null || horario.getHoraFin() == null) {
            return 0L;
        }
        return Duration.between(horario.getHoraInicio(), horario.getHoraFin()).toMinutes();
    }

    //Calcula la cantidad teórica de citas
    private static Integer calcularCantidadTeoricaCitas(HorarioAtencion horario) {
        Long duracion = calcularDuracionMinutos(horario);
        Integer intervalo = horario.getIntervaloCitas();

        if (duracion == null || duracion <= 0 || intervalo == null || intervalo <= 0) {
            return 0;
        }

        return (int) (duracion / intervalo);
    }
}