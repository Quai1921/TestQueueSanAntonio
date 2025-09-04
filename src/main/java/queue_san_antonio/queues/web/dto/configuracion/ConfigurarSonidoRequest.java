package queue_san_antonio.queues.web.dto.configuracion;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigurarSonidoRequest {

    @NotNull(message = "Debe especificar si el sonido está activo")
    private Boolean activo;

    @Size(max = 200, message = "La ruta del archivo de sonido no puede exceder 200 caracteres")
    private String archivo;

    @NotNull(message = "El volumen es obligatorio")
    @Min(value = 0, message = "El volumen mínimo es 0")
    @Max(value = 100, message = "El volumen máximo es 100")
    private Integer volumen;
}