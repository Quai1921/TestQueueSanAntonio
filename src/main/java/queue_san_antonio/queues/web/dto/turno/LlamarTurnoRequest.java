package queue_san_antonio.queues.web.dto.turno;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//DTO para solicitud de llamar turno
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlamarTurnoRequest {

    //ID del turno a llamar
    @NotNull(message = "El ID del turno es obligatorio")
    @JsonProperty("turnoId")
    private Long turnoId;

    //Observaciones opcionales del llamado
    @JsonProperty("observaciones")
    private String observaciones;
}