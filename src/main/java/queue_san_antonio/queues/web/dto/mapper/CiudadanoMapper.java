package queue_san_antonio.queues.web.dto.mapper;

import queue_san_antonio.queues.models.Ciudadano;
import queue_san_antonio.queues.web.dto.ciudadano.CiudadanoRequest;
import queue_san_antonio.queues.web.dto.ciudadano.CiudadanoResponse;
import queue_san_antonio.queues.web.dto.ciudadano.CiudadanoSummaryResponse;

import java.util.List;

//Mapper para conversiones entre Ciudadano y DTOs
public class CiudadanoMapper {

    //Convierte CiudadanoRequest a Ciudadano
    public static Ciudadano toEntity(CiudadanoRequest request) {
        if (request == null) return null;

        return Ciudadano.builder()
                .dni(request.getDni() != null ? request.getDni().trim() : null)
                .nombre(request.getNombre() != null ? request.getNombre().trim() : null)
                .apellido(request.getApellido() != null ? request.getApellido().trim() : null)
                .telefono(request.getTelefono() != null ? request.getTelefono().trim() : null)
                .direccion(request.getDireccion() != null ? request.getDireccion().trim() : null)
                .esPrioritario(request.getEsPrioritario() != null ? request.getEsPrioritario() : false)
                .motivoPrioridad(request.getMotivoPrioridad() != null ? request.getMotivoPrioridad().trim() : null)
                .observaciones(request.getObservaciones() != null ? request.getObservaciones().trim() : null)
                .build();
    }

    //Actualiza Ciudadano existente con datos del request
    public static void updateEntity(Ciudadano ciudadano, CiudadanoRequest request) {
        if (ciudadano == null || request == null) return;

        ciudadano.setDni(request.getDni());

        ciudadano.setNombre(request.getNombre() != null ? request.getNombre().trim() : ciudadano.getNombre());
        ciudadano.setApellido(request.getApellido() != null ? request.getApellido().trim() : ciudadano.getApellido());
        ciudadano.setTelefono(request.getTelefono() != null ? request.getTelefono().trim() : ciudadano.getTelefono());
        ciudadano.setDireccion(request.getDireccion() != null ? request.getDireccion().trim() : ciudadano.getDireccion());


        if (request.getEsPrioritario() != null) {
            ciudadano.establecerPrioridad(
                    request.getEsPrioritario(),
                    request.getMotivoPrioridad() != null ? request.getMotivoPrioridad().trim() : null
            );
        }

        if (request.getObservaciones() != null) {
            ciudadano.setObservaciones(request.getObservaciones().trim());
        }
    }

    //Convierte Ciudadano a CiudadanoResponse
    public static CiudadanoResponse toResponse(Ciudadano ciudadano) {
        if (ciudadano == null) return null;

        return CiudadanoResponse.builder()
                .id(ciudadano.getId())
                .dni(ciudadano.getDni())
                .nombre(ciudadano.getNombre())
                .apellido(ciudadano.getApellido())
                .nombreCompleto(ciudadano.getNombreCompleto())
                .telefono(ciudadano.getTelefono())
                .direccion(ciudadano.getDireccion())
                .esPrioritario(ciudadano.getEsPrioritario())
                .motivoPrioridad(ciudadano.getMotivoPrioridad())
                .observaciones(ciudadano.getObservaciones())
                .fechaRegistro(ciudadano.getFechaRegistro())
                .cantidadTurnos(ciudadano.getCantidadTurnos())
                .tieneTurnoPendiente(null) // Se calculará en el service
                .build();
    }

    //Convierte Ciudadano a CiudadanoSummaryResponse
    public static CiudadanoSummaryResponse toSummaryResponse(Ciudadano ciudadano) {
        if (ciudadano == null) return null;

        return CiudadanoSummaryResponse.builder()
                .id(ciudadano.getId())
                .dni(ciudadano.getDni())
                .nombre(ciudadano.getNombre())
                .apellido(ciudadano.getApellido())
                .direccion(ciudadano.getDireccion())
                .observaciones(ciudadano.getObservaciones())
                .telefono(ciudadano.getTelefono())
                .esPrioritario(ciudadano.getEsPrioritario())
                .motivoPrioridad(ciudadano.getMotivoPrioridad())
                .cantidadTurnos(ciudadano.getCantidadTurnos())
                .tieneTurnoPendiente(null) // Se calculará en el service
                .build();
    }

    //Convierte lista de Ciudadanos a CiudadanoResponse
    public static List<CiudadanoResponse> toResponseList(List<Ciudadano> ciudadanos) {
        return ciudadanos.stream()
                .map(CiudadanoMapper::toResponse)
                .toList();
    }

    //Convierte lista de Ciudadanos a CiudadanoSummaryResponse
    public static List<CiudadanoSummaryResponse> toSummaryResponseList(List<Ciudadano> ciudadanos) {
        return ciudadanos.stream()
                .map(CiudadanoMapper::toSummaryResponse)
                .toList();
    }
}