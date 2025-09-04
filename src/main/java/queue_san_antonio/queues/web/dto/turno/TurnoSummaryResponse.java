package queue_san_antonio.queues.web.dto.turno;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.models.EstadoTurno;
import queue_san_antonio.queues.models.TipoTurno;

import java.time.LocalDateTime;

//DTO para listados de turnos (información resumida)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnoSummaryResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("estado")
    private EstadoTurno estado;

    @JsonProperty("tipo")
    private TipoTurno tipo;

    @JsonProperty("prioridad")
    private Integer prioridad;

    //Información básica del ciudadano
    @JsonProperty("ciudadano")
    private String ciudadanoNombre;

    @JsonProperty("ciudadanoDni")
    private String ciudadanoDni;

    //Información básica del sector
    @JsonProperty("sector")
    private String sectorCodigo;

    @JsonProperty("sectorNombre")
    private String sectorNombre;

    //Empleado que atiende
    @JsonProperty("empleadoAtencion")
    private String empleadoUsername;

    //Fecha de generación
    @JsonProperty("fechaGeneracion")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaGeneracion;

    //Tiempo de espera en minutos (calculado)
    @JsonProperty("tiempoEspera")
    private Long tiempoEspera;
}