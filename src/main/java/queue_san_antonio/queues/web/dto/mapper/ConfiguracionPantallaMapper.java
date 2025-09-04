package queue_san_antonio.queues.web.dto.mapper;

import queue_san_antonio.queues.models.ConfiguracionPantalla;
import queue_san_antonio.queues.web.dto.configuracion.ConfiguracionPantallaRequest;
import queue_san_antonio.queues.web.dto.configuracion.ConfiguracionPantallaResponse;
import queue_san_antonio.queues.web.dto.configuracion.ConfiguracionPantallaSummaryResponse;

import java.util.List;

//Mapper para conversiones entre ConfiguracionPantalla y DTOs
public class ConfiguracionPantallaMapper {

    //Convierte ConfiguracionPantallaRequest a ConfiguracionPantalla
    public static ConfiguracionPantalla toEntity(ConfiguracionPantallaRequest request) {
        if (request == null) return null;

        return ConfiguracionPantalla.builder()
                .nombre(request.getNombre() != null ? request.getNombre().trim() : null)
                .tiempoMensaje(request.getTiempoMensaje())
                .tiempoTurno(request.getTiempoTurno())
                .textoEncabezado(request.getTextoEncabezado() != null ? request.getTextoEncabezado().trim() : null)
                .sonidoActivo(request.getSonidoActivo() != null ? request.getSonidoActivo() : true)
                .archivoSonido(request.getArchivoSonido() != null ? request.getArchivoSonido().trim() : null)
                .volumenSonido(request.getVolumenSonido() != null ? request.getVolumenSonido() : 70)
                .animacionesActivas(request.getAnimacionesActivas() != null ? request.getAnimacionesActivas() : true)
                .activo(false) // Nueva configuración inactiva por defecto
                .build();
    }

    //Actualiza ConfiguracionPantalla existente con datos del request
    public static void updateEntity(ConfiguracionPantalla configuracion, ConfiguracionPantallaRequest request) {
        if (configuracion == null || request == null) return;

        if (request.getNombre() != null) {
            configuracion.setNombre(request.getNombre().trim());
        }
        if (request.getTiempoMensaje() != null) {
            configuracion.setTiempoMensaje(request.getTiempoMensaje());
        }
        if (request.getTiempoTurno() != null) {
            configuracion.setTiempoTurno(request.getTiempoTurno());
        }
        if (request.getTextoEncabezado() != null) {
            configuracion.setTextoEncabezado(request.getTextoEncabezado().trim());
        }
        if (request.getSonidoActivo() != null) {
            configuracion.setSonidoActivo(request.getSonidoActivo());
        }
        if (request.getArchivoSonido() != null) {
            configuracion.setArchivoSonido(request.getArchivoSonido().trim());
        }
        if (request.getVolumenSonido() != null) {
            configuracion.setVolumenSonido(request.getVolumenSonido());
        }
        if (request.getAnimacionesActivas() != null) {
            configuracion.setAnimacionesActivas(request.getAnimacionesActivas());
        }

    }

    //Convierte ConfiguracionPantalla a ConfiguracionPantallaResponse
    public static ConfiguracionPantallaResponse toResponse(ConfiguracionPantalla configuracion) {
        if (configuracion == null) return null;

        return ConfiguracionPantallaResponse.builder()
                .id(configuracion.getId())
                .nombre(configuracion.getNombre())
                .tiempoMensaje(configuracion.getTiempoMensaje())
                .tiempoTurno(configuracion.getTiempoTurno())
                .textoEncabezado(configuracion.getTextoEncabezado())
                .sonidoActivo(configuracion.getSonidoActivo())
                .archivoSonido(configuracion.getArchivoSonido())
                .volumenSonido(configuracion.getVolumenSonido())
                .animacionesActivas(configuracion.getAnimacionesActivas())
                .activo(configuracion.getActivo())
                .fechaCreacion(configuracion.getFechaCreacion())
                .fechaModificacion(configuracion.getFechaActualizacion())
                .mensajes(configuracion.getMensajes() != null ?
                        MensajeInstitucionalMapper.toSummaryResponseList(configuracion.getMensajesActivos()) : null)
                .totalMensajes(configuracion.getMensajes() != null ? configuracion.getMensajes().size() : 0)
                .tieneMensajes(configuracion.tieneMensajes())
                .build();
    }

    //Convierte ConfiguracionPantalla a ConfiguracionPantallaSummaryResponse
    public static ConfiguracionPantallaSummaryResponse toSummaryResponse(ConfiguracionPantalla configuracion) {
        if (configuracion == null) return null;

        return ConfiguracionPantallaSummaryResponse.builder()
                .id(configuracion.getId())
                .nombre(configuracion.getNombre())
                .tiempoMensaje(configuracion.getTiempoMensaje())
                .tiempoTurno(configuracion.getTiempoTurno())
                .activo(configuracion.getActivo())
                .totalMensajes(configuracion.getMensajes() != null ? configuracion.getMensajes().size() : 0)
                .tieneMensajes(configuracion.tieneMensajes())
                .fechaCreacion(configuracion.getFechaCreacion())
                .build();
    }

    //Convierte lista de ConfiguracionPantalla a lista de Response
    public static List<ConfiguracionPantallaResponse> toResponseList(List<ConfiguracionPantalla> configuraciones) {
        if (configuraciones == null) return null;
        return configuraciones.stream()
                .map(ConfiguracionPantallaMapper::toResponse)
                .toList();
    }

    //Convierte lista de ConfiguracionPantalla a lista de SummaryResponse
    public static List<ConfiguracionPantallaSummaryResponse> toSummaryResponseList(List<ConfiguracionPantalla> configuraciones) {
        if (configuraciones == null) return null;
        return configuraciones.stream()
                .map(ConfiguracionPantallaMapper::toSummaryResponse)
                .toList();
    }
}