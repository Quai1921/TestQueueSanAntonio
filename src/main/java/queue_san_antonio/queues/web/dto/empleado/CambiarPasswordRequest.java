package queue_san_antonio.queues.web.dto.empleado;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//DTO para cambiar contraseña de empleado
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambiarPasswordRequest {

    //Nueva contraseña
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @JsonProperty("nuevaPassword")
    private String nuevaPassword;

    //Confirmación de nueva contraseña
    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    @JsonProperty("confirmarPassword")
    private String confirmarPassword;

    // ==========================================
    // VALIDACIONES PERSONALIZADAS
    // ==========================================

    //Valida que las contraseñas coincidan
    public boolean passwordsCoinciden() {
        if (nuevaPassword == null || confirmarPassword == null) {
            return false;
        }
        return nuevaPassword.equals(confirmarPassword);
    }

    //Obtiene la nueva contraseña limpia
    public String getNuevaPasswordLimpia() {
        return nuevaPassword != null ? nuevaPassword.trim() : null;
    }
}