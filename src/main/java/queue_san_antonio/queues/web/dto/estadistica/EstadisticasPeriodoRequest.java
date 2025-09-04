package queue_san_antonio.queues.web.dto.estadistica;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasPeriodoRequest {

    @NotNull(message = "La fecha de inicio es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    // Validación personalizada
    public boolean fechasValidas() {
        if (fechaInicio == null || fechaFin == null) {
            return false;
        }
        return !fechaFin.isBefore(fechaInicio);
    }

    // Validación de rango máximo
    public boolean rangoValido() {
        if (!fechasValidas()) {
            return false;
        }
        return fechaInicio.plusMonths(6).isAfter(fechaFin); // Máximo 6 meses
    }
}