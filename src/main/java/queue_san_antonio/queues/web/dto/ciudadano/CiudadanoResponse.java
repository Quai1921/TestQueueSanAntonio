package queue_san_antonio.queues.web.dto.ciudadano;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//DTO para respuesta de ciudadano
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CiudadanoResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("dni")
    private String dni;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("apellido")
    private String apellido;

    @JsonProperty("nombreCompleto")
    private String nombreCompleto;

    @JsonProperty("telefono")
    private String telefono;

    @JsonProperty("direccion")
    private String direccion;

    @JsonProperty("esPrioritario")
    private Boolean esPrioritario;

    @JsonProperty("motivoPrioridad")
    private String motivoPrioridad;

    @JsonProperty("observaciones")
    private String observaciones;

    @JsonProperty("fechaRegistro")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaRegistro;

    //Estadísticas del ciudadano
    @JsonProperty("cantidadTurnos")
    private Integer cantidadTurnos;

    @JsonProperty("tieneTurnoPendiente")
    private Boolean tieneTurnoPendiente;
}