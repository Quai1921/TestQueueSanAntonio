package queue_san_antonio.queues.web.dto.historial;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para comparación entre sectores
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparacionSectoresResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    private MetricasSectorResponse sector1;
    private MetricasSectorResponse sector2;
}