package queue_san_antonio.queues.web.dto.mensaje;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstablecerVigenciaRequest {

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    // Validación personalizada en el controller
    public boolean fechasValidas() {
        if (fechaInicio == null || fechaFin == null) {
            return false;
        }
        return !fechaFin.isBefore(fechaInicio);
    }
}