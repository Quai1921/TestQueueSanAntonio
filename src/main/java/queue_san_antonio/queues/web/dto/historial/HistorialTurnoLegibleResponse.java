package queue_san_antonio.queues.web.dto.historial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialTurnoLegibleResponse {

    private String turnoCodigo;
    private Integer totalAcciones;
    private String resumen;

    // Historial completo en formato legible
    private List<String> historialLegible;

    // Información adicional
    private String estadoActual;
    private String fechaGeneracion;
    private String fechaFinalizacion;
    private Boolean completado;
}