package queue_san_antonio.queues.web.dto.historial;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.models.AccionTurno;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialSummaryResponse {

    private Long id;
    private String turnoCodigo;
    private AccionTurno accion;
    private String descripcionAccion;
    private String empleadoNombre;
    private String empleadoUsername;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaHora;

    private String observaciones;
    private String motivo;

    // Campos específicos según el tipo de acción
    private String sectorOrigenCodigo;
    private String sectorDestinoCodigo;
    private String cambioEstado; // "GENERADO → LLAMADO"
    private String cambioPrioridad; // "2 → 5"
}