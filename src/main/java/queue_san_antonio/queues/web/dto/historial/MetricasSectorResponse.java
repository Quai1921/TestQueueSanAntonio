package queue_san_antonio.queues.web.dto.historial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para métricas de un sector específico
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasSectorResponse {

    private Long sectorId;
    private String sectorCodigo;
    private String sectorNombre;
    private Long totalTurnos;
    private Long turnosFinalizados;
    private Long turnosAusentes;
    private Long turnosRedirigidos;
}