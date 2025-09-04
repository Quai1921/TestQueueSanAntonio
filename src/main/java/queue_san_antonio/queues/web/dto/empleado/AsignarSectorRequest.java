package queue_san_antonio.queues.web.dto.empleado;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//DTO para asignar empleado a sector
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignarSectorRequest {

    //ID del sector (null para desasignar)
    @JsonProperty("sectorId")
    private Long sectorId;

    //Motivo de la asignación/desasignación
    @Size(max = 200, message = "El motivo no puede exceder 200 caracteres")
    @JsonProperty("motivo")
    private String motivo;

    // ==========================================
    // MÉTODOS DE CONVENIENCIA
    // ==========================================

    //Verifica si es desasignación (sectorId null)
    public boolean esDesasignacion() {
        return sectorId == null;
    }

    //Obtiene el motivo limpio
    public String getMotivoLimpio() {
        return motivo != null ? motivo.trim() : null;
    }

    //Verifica si tiene motivo especificado
    public boolean tieneMotivo() {
        return motivo != null && !motivo.trim().isEmpty();
    }
}