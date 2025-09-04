package queue_san_antonio.queues.web.dto.mensaje;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.models.TipoMensaje;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeInstitucionalSummaryResponse {

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
    private Boolean estaVigente;
    private String estadoVigencia;
}