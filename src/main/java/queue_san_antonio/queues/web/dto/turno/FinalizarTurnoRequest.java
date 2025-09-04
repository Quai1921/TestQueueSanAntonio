package queue_san_antonio.queues.web.dto.turno;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizarTurnoRequest {

    //Observaciones de la finalización (opcional)
    @JsonProperty("observaciones")
    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres")
    private String observaciones;
}