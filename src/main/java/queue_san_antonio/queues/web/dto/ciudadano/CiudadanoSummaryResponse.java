package queue_san_antonio.queues.web.dto.ciudadano;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//DTO para listados de ciudadanos (información resumida)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CiudadanoSummaryResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("dni")
    private String dni;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("apellido")
    private String apellido;

    @JsonProperty("telefono")
    private String telefono;

    @JsonProperty("direccion")
    private String direccion;

    @JsonProperty("observaciones")
    private String observaciones;

    @JsonProperty("esPrioritario")
    private Boolean esPrioritario;

    @JsonProperty("motivoPrioridad")
    private String motivoPrioridad;

    @JsonProperty("cantidadTurnos")
    private Integer cantidadTurnos;

    @JsonProperty("tieneTurnoPendiente")
    private Boolean tieneTurnoPendiente;
}