package queue_san_antonio.queues.web.dto.turno;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.models.TipoTurno;

import java.time.LocalDate;
import java.time.LocalTime;

//DTO para solicitud de generación de turno
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerarTurnoRequest {

    //DNI del ciudadano (puede ser nuevo o existente)
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener entre 7 y 8 dígitos")
    @JsonProperty("dni")
    private String dni;

    //Datos del ciudadano (obligatorios si es nuevo)
    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("apellido")
    private String apellido;

    @JsonProperty("telefono")
    private String telefono;

    @JsonProperty("direccion")
    private String direccion;

    @JsonProperty("esPrioritario")
    @Builder.Default
    private Boolean esPrioritario = false;

    @Size(max = 100, message = "El motivo de prioridad no puede exceder 100 caracteres")
    @JsonProperty("motivoPrioridad")
    private String motivoPrioridad;

    //ID del sector donde se genera el turno
    @NotNull(message = "El sector es obligatorio")
    @JsonProperty("sectorId")
    private Long sectorId;

    //Tipo de turno (por defecto NORMAL)
    @JsonProperty("tipo")
    @Builder.Default
    private TipoTurno tipo = TipoTurno.NORMAL;

    //Para turnos especiales con cita previa
    @JsonProperty("fechaCita")
    private LocalDate fechaCita;

    @JsonProperty("horaCita")
    private LocalTime horaCita;

    //Observaciones opcionales
    @JsonProperty("observaciones")
    private String observaciones;

    // ==========================================
    // MÉTODOS DE CONVENIENCIA (SIN LÓGICA DE NEGOCIO)
    // ==========================================

    //Verifica si es turno especial
    public boolean esEspecial() {
        return tipo == TipoTurno.ESPECIAL || (fechaCita != null && horaCita != null);
    }

    //Verifica si tiene datos completos del ciudadano
    public boolean tieneDatosCiudadano() {
        return nombre != null && apellido != null && telefono != null && direccion != null;
    }
}