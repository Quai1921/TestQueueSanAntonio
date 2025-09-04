package queue_san_antonio.queues.web.dto.mensaje;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.models.TipoMensaje;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeInstitucionalResponse {

    private Long id;
    private TipoMensaje tipo;
    private String titulo;
    private String contenido;
    private String rutaArchivo;
    private Integer duracion;
    private Integer orden;
    private Boolean activo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    // Información de la configuración asociada
    private Long configuracionId;
    private String configuracionNombre;

    // Estado calculado
    private Boolean estaVigente;
    private String estadoVigencia; // "VIGENTE", "PENDIENTE", "VENCIDO"
}