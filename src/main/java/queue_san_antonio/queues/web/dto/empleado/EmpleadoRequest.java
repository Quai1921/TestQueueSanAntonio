package queue_san_antonio.queues.web.dto.empleado;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.models.RolEmpleado;

//DTO para crear/actualizar empleados
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoRequest {

    //Username único del empleado
    @NotBlank(message = "El username es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{3,50}$", message = "El username debe tener 3-50 caracteres (letras, números, puntos, guiones)")
    @JsonProperty("username")
    private String username;

    //Contraseña (solo para creación)
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @JsonProperty("password")
    private String password;

    //Datos personales
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @JsonProperty("nombre")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    @JsonProperty("apellido")
    private String apellido;

    //Información de contacto
    @Email(message = "Formato de email inválido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    @JsonProperty("email")
    private String email;

    @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener entre 7 y 8 dígitos")
    @JsonProperty("dni")
    private String dni;

    @Pattern(regexp = "^[0-9\\s\\-\\(\\)]{8,20}$", message = "Formato de teléfono inválido")
    @JsonProperty("telefono")
    private String telefono;

    //Configuración del empleado
    @NotNull(message = "El rol es obligatorio")
    @JsonProperty("rol")
    private RolEmpleado rol;

    @JsonProperty("sectorId")
    private Long sectorId;

    @JsonProperty("observaciones")
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    // ==========================================
    // MÉTODOS DE CONVENIENCIA
    // ==========================================

    //Verifica si es creación (tiene password)
    public boolean esCreacion() {
        return password != null && !password.trim().isEmpty();
    }

    //Obtiene username limpio
    public String getUsernameLimpio() {
        return username != null ? username.trim().toLowerCase() : null;
    }

    //Verifica si tiene datos de contacto completos
    public boolean tieneContactoCompleto() {
        return email != null && !email.trim().isEmpty() &&
                telefono != null && !telefono.trim().isEmpty();
    }

    //Obtiene observaciones limpias
    public String getObservacionesLimpias() {
        return observaciones != null ? observaciones.trim() : null;
    }
}