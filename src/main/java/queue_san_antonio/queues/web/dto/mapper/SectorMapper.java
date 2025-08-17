package queue_san_antonio.queues.web.dto.mapper;

import queue_san_antonio.queues.models.Sector;
import queue_san_antonio.queues.web.dto.sector.SectorRequest;
import queue_san_antonio.queues.web.dto.sector.SectorResponse;
import queue_san_antonio.queues.web.dto.sector.SectorUpdateRequest;

import java.util.List;

//Mapper para conversiones entre Sector y DTOs
public class SectorMapper {

    //Convierte SectorRequest a Sector
    public static Sector toEntity(SectorRequest request) {
        if (request == null) return null;

        return Sector.builder()
                .codigo(request.getCodigo() != null ? request.getCodigo().trim().toUpperCase() : null)
                .nombre(request.getNombre() != null ? request.getNombre().trim() : null)
                .descripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null)
                .tipoSector(request.getTipoSector())
                .requiereCitaPrevia(request.getRequiereCitaPrevia() != null ? request.getRequiereCitaPrevia() : false)
                .capacidadMaxima(request.getCapacidadMaxima() != null ? request.getCapacidadMaxima() : 1)
                .tiempoEstimadoAtencion(request.getTiempoEstimadoAtencion() != null ? request.getTiempoEstimadoAtencion() : 15)
                .color(request.getColor())
                .ordenVisualizacion(request.getOrdenVisualizacion())
                .observaciones(request.getObservaciones() != null ? request.getObservaciones().trim() : null)
                .activo(true) // Los sectores nuevos se crean activos
                .build();
    }

    //Actualiza Sector existente con datos del request
//    public static void updateEntity(Sector sector, SectorRequest request) {
//        if (sector == null || request == null) return;
//
//        if (request.getNombre() != null) {
//            sector.setNombre(request.getNombre().trim());
//        }
//        if (request.getDescripcion() != null) {
//            sector.setDescripcion(request.getDescripcion().trim());
//        }
//        if (request.getTipoSector() != null) {
//            sector.setTipoSector(request.getTipoSector());
//        }
//        if (request.getRequiereCitaPrevia() != null) {
//            sector.setRequiereCitaPrevia(request.getRequiereCitaPrevia());
//        }
//        if (request.getCapacidadMaxima() != null) {
//            sector.setCapacidadMaxima(request.getCapacidadMaxima());
//        }
//        if (request.getTiempoEstimadoAtencion() != null) {
//            sector.setTiempoEstimadoAtencion(request.getTiempoEstimadoAtencion());
//        }
//        if (request.getColor() != null) {
//            sector.setColor(request.getColor());
//        }
//        if (request.getOrdenVisualizacion() != null) {
//            sector.setOrdenVisualizacion(request.getOrdenVisualizacion());
//        }
//        if (request.getObservaciones() != null) {
//            sector.setObservaciones(request.getObservaciones().trim());
//        }
//    }

    //Actualiza Sector existente con datos del SectorUpdateRequest (sin código)
    public static void updateEntity(Sector sector, SectorUpdateRequest request) {
        if (sector == null || request == null) return;

        if (request.getNombre() != null) {
            sector.setNombre(request.getNombre().trim());
        }
        if (request.getDescripcion() != null) {
            sector.setDescripcion(request.getDescripcion().trim());
        }
        if (request.getTipoSector() != null) {
            sector.setTipoSector(request.getTipoSector());
        }
        if (request.getRequiereCitaPrevia() != null) {
            sector.setRequiereCitaPrevia(request.getRequiereCitaPrevia());
        }
        if (request.getCapacidadMaxima() != null) {
            sector.setCapacidadMaxima(request.getCapacidadMaxima());
        }
        if (request.getTiempoEstimadoAtencion() != null) {
            sector.setTiempoEstimadoAtencion(request.getTiempoEstimadoAtencion());
        }
        if (request.getColor() != null) {
            sector.setColor(request.getColor().trim());
        }
        if (request.getOrdenVisualizacion() != null) {
            sector.setOrdenVisualizacion(request.getOrdenVisualizacion());
        }
        if (request.getObservaciones() != null) {
            sector.setObservaciones(request.getObservaciones().trim());
        }
    }

    //Convierte Sector a SectorResponse
    public static SectorResponse toResponse(Sector sector) {
        if (sector == null) return null;

        return SectorResponse.builder()
                .codigo(sector.getCodigo())
                .nombre(sector.getNombre())
                .nombreCompleto(sector.getNombreCompleto())
                .descripcion(sector.getDescripcion())
                .tipoSector(sector.getTipoSector())
                .activo(sector.getActivo())
                .requiereCitaPrevia(sector.getRequiereCitaPrevia())
                .capacidadMaxima(sector.getCapacidadMaxima())
                .tiempoEstimadoAtencion(sector.getTiempoEstimadoAtencion())
                .color(sector.getColor())
                .ordenVisualizacion(sector.getOrdenVisualizacion())
                .observaciones(sector.getObservaciones())
                .fechaCreacion(sector.getFechaCreacion())
                .responsable(toResponsableInfo(sector))
                .cantidadEmpleados(sector.getCantidadEmpleadosActivos())
                .turnosPendientes(sector.getTurnosPendientes())
                .tieneHorarios(sector.tieneHorariosConfigurados())
                .build();
    }

    //Convierte lista de Sectores a SectorResponse
    public static List<SectorResponse> toResponseList(List<Sector> sectores) {
        return sectores.stream()
                .map(SectorMapper::toResponse)
                .toList();
    }

    // ==========================================
    // MÉTODOS PRIVADOS
    // ==========================================

    private static SectorResponse.ResponsableInfo toResponsableInfo(Sector sector) {
        if (sector.getResponsable() == null) return null;

        return SectorResponse.ResponsableInfo.builder()
                .username(sector.getResponsable().getUsername())
                .nombreCompleto(sector.getResponsable().getNombreCompleto())
                .email(sector.getResponsable().getEmail())
                .build();
    }
}