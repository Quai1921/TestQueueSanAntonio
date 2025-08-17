package queue_san_antonio.queues.web.dto.mensaje;

import jakarta.validation.constraints.*;
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
public class MensajeInstitucionalRequest {

    @NotNull(message = "El tipo de mensaje es obligatorio")
    private TipoMensaje tipo;

    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String titulo;

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    @Size(max = 1000, message = "El contenido no puede exceder 1000 caracteres")
    private String contenido;

    @NotNull(message = "La duración del mensaje es obligatoria")
    @Min(value = 3, message = "La duración mínima es 3 segundos")
    @Max(value = 60, message = "La duración máxima es 60 segundos")
    private Integer duracion;

    @Min(value = 0, message = "El orden no puede ser negativo")
    @Max(value = 100, message = "El orden máximo es 100")
    @Builder.Default
    private Integer orden = 0;

    // Campos opcionales para vigencia
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}