package queue_san_antonio.queues.web.dto.ciudadano;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//DTO para establecer o quitar prioridad a un ciudadano
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstablecerPrioridadRequest {

    //Estado de prioridad a establecer
    @NotNull(message = "Debe especificar si el ciudadano es prioritario o no")
    @JsonProperty("esPrioritario")
    private Boolean esPrioritario;

    //Motivo de la prioridad (obligatorio si es prioritario)
    @Size(max = 100, message = "El motivo no puede exceder 100 caracteres")
    @JsonProperty("motivo")
    private String motivo;

    // ==========================================
    // VALIDACIONES PERSONALIZADAS
    // ==========================================

    //Valida que si es prioritario, debe tener motivo
    //Se usará en validación adicional del controller
    public boolean isValid() {
        if (esPrioritario != null && esPrioritario) {
            return motivo != null && !motivo.trim().isEmpty();
        }
        return true; // Si no es prioritario, no necesita motivo
    }

    //Obtiene el motivo limpio (sin espacios extra)
    public String getMotivoLimpio() {
        return motivo != null ? motivo.trim() : null;
    }
}