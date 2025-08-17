package queue_san_antonio.queues.web.dto.ciudadano;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//DTO para crear/actualizar ciudadano
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CiudadanoRequest {

    //DNI del ciudadano (único)
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener entre 7 y 8 dígitos")
    @JsonProperty("dni")
    private String dni;

    //Datos personales
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @JsonProperty("nombre")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    @JsonProperty("apellido")
    private String apellido;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{8,20}$", message = "Formato de teléfono inválido")
    @JsonProperty("telefono")
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    @JsonProperty("direccion")
    private String direccion;

    //Configuración de prioridad
    @JsonProperty("esPrioritario")
    @Builder.Default
    private Boolean esPrioritario = false;

    @Size(max = 100, message = "El motivo de prioridad no puede exceder 100 caracteres")
    @JsonProperty("motivoPrioridad")
    private String motivoPrioridad;

    //Observaciones adicionales
    @JsonProperty("observaciones")
    private String observaciones;
}