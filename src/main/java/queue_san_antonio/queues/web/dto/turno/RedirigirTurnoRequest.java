package queue_san_antonio.queues.web.dto.turno;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedirigirTurnoRequest {

    //ID del nuevo sector de destino
    @NotNull(message = "El nuevo sector es obligatorio")
    @JsonProperty("nuevoSectorId")
    private Long nuevoSectorId;

    //Motivo de la redirección (obligatorio)
    @NotBlank(message = "El motivo de redirección es obligatorio")
    @Size(min = 10, max = 250, message = "El motivo debe tener entre 10 y 250 caracteres")
    @JsonProperty("motivo")
    private String motivo;

    //Observaciones adicionales (opcional)
    @JsonProperty("observaciones")
    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres")
    private String observaciones;
}