package queue_san_antonio.queues.web.dto.sector;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//DTO para asignar responsable a un sector
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignarResponsableRequest {

    //ID del empleado que será responsable
    @NotNull(message = "El ID del empleado es obligatorio")
    @JsonProperty("empleadoId")
    private Long empleadoId;

//    //ID del administrador que realiza la asignación
//    @NotNull(message = "El ID del administrador es obligatorio")
//    @JsonProperty("adminId")
//    private Long adminId;
//
//    //Motivo de la asignación (opcional)
//    @JsonProperty("motivo")
//    private String motivo;
//
//    // ==========================================
//    // MÉTODOS DE CONVENIENCIA
//    // ==========================================
//
//    //Obtiene el motivo limpio (sin espacios extra)
//    public String getMotivoLimpio() {
//        return motivo != null ? motivo.trim() : null;
//    }
//
//    //Verifica si tiene motivo especificado
//    public boolean tieneMotivo() {
//        return motivo != null && !motivo.trim().isEmpty();
//    }
}