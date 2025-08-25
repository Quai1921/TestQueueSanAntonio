package queue_san_antonio.queues.web.dto.mapper;

import queue_san_antonio.queues.models.Empleado;
import queue_san_antonio.queues.web.dto.empleado.EmpleadoRequest;
import queue_san_antonio.queues.web.dto.empleado.EmpleadoResponse;
import queue_san_antonio.queues.web.dto.empleado.EmpleadoSummaryResponse;

import java.util.List;

//Mapper para conversiones entre Empleado y DTOs
public class EmpleadoMapper {

    // ==========================================
    // CONVERSIONES ENTITY ↔ REQUEST
    // ==========================================

    /**
     * Convierte EmpleadoRequest a Empleado (para creación)
     * Nota: No incluye password ni sector, deben asignarse después
     */
    public static Empleado toEntity(EmpleadoRequest request) {
        if (request == null) return null;

        return Empleado.builder()
                .username(request.getUsernameLimpio())
                .nombre(request.getNombre() != null ? request.getNombre().trim() : null)
                .apellido(request.getApellido() != null ? request.getApellido().trim() : null)
                .email(request.getEmail() != null ? request.getEmail().trim() : null)
                .dni(request.getDni() != null ? request.getDni().trim() : null)
                .rol(request.getRol())
                .activo(true) // Los empleados nuevos se crean activos
                .build();
    }

    //Actualiza Empleado existente con datos del request
    public static void updateEntity(Empleado empleado, EmpleadoRequest request) {
        if (empleado == null || request == null) return;

        if (request.getNombre() != null) {
            empleado.setNombre(request.getNombre().trim());
        }
        if (request.getApellido() != null) {
            empleado.setApellido(request.getApellido().trim());
        }
        if (request.getEmail() != null) {
            empleado.setEmail(request.getEmail().trim());
        }
        if (request.getDni() != null) {
            empleado.setDni(request.getDni().trim());
        }
        if (request.getRol() != null) {
            empleado.setRol(request.getRol());
        }
    }

    // ==========================================
    // CONVERSIONES ENTITY → RESPONSE
    // ==========================================

    //Convierte Empleado a EmpleadoResponse completo
    public static EmpleadoResponse toResponse(Empleado empleado) {
        if (empleado == null) return null;

        return EmpleadoResponse.builder()
                .id(empleado.getId())
                .username(empleado.getUsername())
                .nombre(empleado.getNombre())
                .apellido(empleado.getApellido())
                .nombreCompleto(empleado.getNombreCompleto())
                .email(empleado.getEmail())
                .dni(empleado.getDni())
                .rol(empleado.getRol())
                .activo(empleado.getActivo())
                .fechaCreacion(empleado.getFechaCreacion())
                .ultimoAcceso(empleado.getUltimoAcceso())
                .sector(toSectorInfo(empleado))
                .cantidadTurnosAtendidos(empleado.getCantidadTurnosAtendidos())
                .puedeAcceder(empleado.puedeAcceder())
                .esAdministrador(empleado.esAdministrador())
                .esResponsable(empleado.esResponsable())
                .build();
    }

    //Convierte Empleado a EmpleadoSummaryResponse (para listas)
    public static EmpleadoSummaryResponse toSummaryResponse(Empleado empleado) {
        if (empleado == null) return null;

        return EmpleadoSummaryResponse.builder()
                .id(empleado.getId())
                .username(empleado.getUsername())
                .nombreCompleto(empleado.getNombreCompleto())
                .email(empleado.getEmail())
                .rol(empleado.getRol())
                .activo(empleado.getActivo())
                .ultimoAcceso(empleado.getUltimoAcceso())
                .sectorCodigo(empleado.getSector() != null ? empleado.getSector().getCodigo() : null)
                .sectorNombre(empleado.getSector() != null ? empleado.getSector().getNombre() : null)
                .sectoresResponsable(toSectoresResponsable(empleado))
                .cantidadTurnosAtendidos(empleado.getCantidadTurnosAtendidos())
                .puedeAcceder(empleado.puedeAcceder())
                .build();
    }

    //Convierte lista de Empleados a EmpleadoResponse
    public static List<EmpleadoResponse> toResponseList(List<Empleado> empleados) {
        return empleados.stream()
                .map(EmpleadoMapper::toResponse)
                .toList();
    }

    //Convierte lista de Empleados a EmpleadoSummaryResponse
    public static List<EmpleadoSummaryResponse> toSummaryResponseList(List<Empleado> empleados) {
        return empleados.stream()
                .map(EmpleadoMapper::toSummaryResponse)
                .toList();
    }

    // ==========================================
    // MÉTODOS PRIVADOS HELPER
    // ==========================================

    //Crea información básica del sector
    private static EmpleadoResponse.SectorInfo toSectorInfo(Empleado empleado) {
        if (empleado.getSector() == null) return null;

        return EmpleadoResponse.SectorInfo.builder()
                .id(empleado.getSector().getId())
                .codigo(empleado.getSector().getCodigo())
                .nombre(empleado.getSector().getNombre())
                .nombreCompleto(empleado.getSector().getNombreCompleto())
                .activo(empleado.getSector().getActivo())
                .build();
    }

    private static List<EmpleadoSummaryResponse.SectorInfo> toSectoresResponsable(Empleado empleado) {
        if (empleado == null || !empleado.esResponsable()) {
            return List.of();
        }

        // Buscar sectores donde este empleado es responsable
        // Esto requiere una consulta al repositorio de sectores
        return empleado.getSectoresResponsable().stream()
                .map(sector -> EmpleadoSummaryResponse.SectorInfo.builder()
                        .id(sector.getId())
                        .codigo(sector.getCodigo())
                        .nombre(sector.getNombre())
                        .build())
                .toList();
    }
}