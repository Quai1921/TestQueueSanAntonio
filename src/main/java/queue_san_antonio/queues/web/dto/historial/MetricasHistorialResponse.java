package queue_san_antonio.queues.web.dto.historial;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para métricas rápidas del historial
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasHistorialResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    private Long totalAcciones;
    private Long turnosGenerados;
    private Long turnosLlamados;
    private Long turnosFinalizados;
    private Long redirecciones;
    private Long ausentes;
    private Long empleadosActivos;
}