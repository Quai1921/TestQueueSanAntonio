package queue_san_antonio.queues.web.dto.mapper;

import queue_san_antonio.queues.models.MensajeInstitucional;
import queue_san_antonio.queues.web.dto.mensaje.MensajeInstitucionalRequest;
import queue_san_antonio.queues.web.dto.mensaje.MensajeInstitucionalResponse;
import queue_san_antonio.queues.web.dto.mensaje.MensajeInstitucionalSummaryResponse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

//Mapper para conversiones entre MensajeInstitucional y DTOs
public class MensajeInstitucionalMapper {

    //Convierte MensajeInstitucionalRequest a MensajeInstitucional
    //Nota: No incluye configuracion, debe asignarse después
    public static MensajeInstitucional toEntity(MensajeInstitucionalRequest request) {
        if (request == null) return null;

        return MensajeInstitucional.builder()
                .tipo(request.getTipo())
                .titulo(request.getTitulo() != null ? request.getTitulo().trim() : null)
                .contenido(request.getContenido() != null ? request.getContenido().trim() : null)
                .duracion(request.getDuracion())
                .orden(request.getOrden() != null ? request.getOrden() : 0)
                .rutaArchivo(request.getRutaArchivo() != null ? request.getRutaArchivo().trim() : null)
                .fechaInicio(request.getFechaInicio() != null ? request.getFechaInicio().atStartOfDay() : null)
                .fechaFin(request.getFechaFin() != null ? request.getFechaFin().atTime(23, 59, 59) : null)
                .activo(true) // Nuevos mensajes activos por defecto
                .build();
    }

    //Convierte MensajeInstitucional a MensajeInstitucionalResponse
    public static MensajeInstitucionalResponse toResponse(MensajeInstitucional mensaje) {
        if (mensaje == null) return null;

        return MensajeInstitucionalResponse.builder()
                .id(mensaje.getId())
                .tipo(mensaje.getTipo())
                .titulo(mensaje.getTitulo())
                .contenido(mensaje.getContenido())
                .rutaArchivo(mensaje.getRutaArchivo())
                .duracion(mensaje.getDuracion())
                .orden(mensaje.getOrden())
                .activo(mensaje.getActivo())
                .fechaInicio(mensaje.getFechaInicio() != null ? mensaje.getFechaInicio().toLocalDate() : null)
                .fechaFin(mensaje.getFechaFin() != null ? mensaje.getFechaFin().toLocalDate() : null)
                .fechaCreacion(mensaje.getFechaCreacion())
                .fechaModificacion(mensaje.getFechaActualizacion())
                .configuracionId(mensaje.getConfiguracion() != null ? mensaje.getConfiguracion().getId() : null)
                .configuracionNombre(mensaje.getConfiguracion() != null ? mensaje.getConfiguracion().getNombre() : null)
                .estaVigente(mensaje.estaVigente())
                .estadoVigencia(getEstadoVigencia(mensaje))
                .build();
    }

    //Convierte MensajeInstitucional a MensajeInstitucionalSummaryResponse
    public static MensajeInstitucionalSummaryResponse toSummaryResponse(MensajeInstitucional mensaje) {
        if (mensaje == null) return null;

        return MensajeInstitucionalSummaryResponse.builder()
                .id(mensaje.getId())
                .tipo(mensaje.getTipo())
                .titulo(mensaje.getTitulo())
                .contenido(mensaje.getContenido())
                .rutaArchivo(mensaje.getRutaArchivo())
                .duracion(mensaje.getDuracion())
                .orden(mensaje.getOrden())
                .activo(mensaje.getActivo())
                .fechaInicio(mensaje.getFechaInicio() != null ? mensaje.getFechaInicio().toLocalDate() : null)
                .fechaFin(mensaje.getFechaFin() != null ? mensaje.getFechaFin().toLocalDate() : null)
                .estaVigente(mensaje.estaVigente())
                .estadoVigencia(getEstadoVigencia(mensaje))
                .build();
    }

    //Convierte lista de MensajeInstitucional a lista de Response
    public static List<MensajeInstitucionalResponse> toResponseList(List<MensajeInstitucional> mensajes) {
        if (mensajes == null) return null;
        return mensajes.stream()
                .map(MensajeInstitucionalMapper::toResponse)
                .toList();
    }

    //Convierte lista de MensajeInstitucional a lista de SummaryResponse
    public static List<MensajeInstitucionalSummaryResponse> toSummaryResponseList(List<MensajeInstitucional> mensajes) {
        if (mensajes == null) return null;
        return mensajes.stream()
                .map(MensajeInstitucionalMapper::toSummaryResponse)
                .toList();
    }

    //Obtiene el estado de vigencia como texto descriptivo
    private static String getEstadoVigencia(MensajeInstitucional mensaje) {
        if (mensaje.getFechaInicio() == null && mensaje.getFechaFin() == null) {
            return "PERMANENTE";
        }

        LocalDate hoy = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));

        if (mensaje.getFechaInicio() != null && hoy.isBefore(mensaje.getFechaInicio().toLocalDate())) {
            return "PENDIENTE";
        }

        if (mensaje.getFechaFin() != null && hoy.isAfter(mensaje.getFechaFin().toLocalDate())) {
            return "VENCIDO";
        }

        return "VIGENTE";
    }
}