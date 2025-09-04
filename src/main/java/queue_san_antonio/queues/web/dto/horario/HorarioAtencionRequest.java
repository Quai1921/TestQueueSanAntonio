package queue_san_antonio.queues.web.dto.horario;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

//DTO para crear/actualizar horarios de atención
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HorarioAtencionRequest {

    //Día de la semana
    @NotNull(message = "El día de la semana es obligatorio")
    @JsonProperty("diaSemana")
    private DayOfWeek diaSemana;

    //Hora de inicio
    @NotNull(message = "La hora de inicio es obligatoria")
    @JsonProperty("horaInicio")
    private LocalTime horaInicio;

    //Hora de fin
    @NotNull(message = "La hora de fin es obligatoria")
    @JsonProperty("horaFin")
    private LocalTime horaFin;

    //Intervalo entre citas en minutos
    @Min(value = 5, message = "El intervalo mínimo entre citas es 5 minutos")
    @Max(value = 120, message = "El intervalo máximo entre citas es 120 minutos")
    @JsonProperty("intervaloCitas")
    @Builder.Default
    private Integer intervaloCitas = 30;

    //Capacidad máxima de citas simultáneas
    @Min(value = 1, message = "La capacidad mínima es 1")
    @Max(value = 10, message = "La capacidad máxima es 10")
    @JsonProperty("capacidadMaxima")
    @Builder.Default
    private Integer capacidadMaxima = 1;

    //Observaciones del horario
    @Size(max = 200, message = "Las observaciones no pueden exceder 200 caracteres")
    @JsonProperty("observaciones")
    private String observaciones;

    // ==========================================
    // VALIDACIONES PERSONALIZADAS
    // ==========================================

    //Valida que la hora de inicio sea anterior a la hora de fin
    public boolean isHorarioValido() {
        if (horaInicio == null || horaFin == null) {
            return false;
        }
        return horaInicio.isBefore(horaFin);
    }

    //Calcula la duración del horario en minutos
    public long getDuracionMinutos() {
        if (horaInicio == null || horaFin == null) {
            return 0;
        }
        return java.time.Duration.between(horaInicio, horaFin).toMinutes();
    }

    //Calcula la cantidad teórica de citas que se pueden agendar
    public int getCantidadTeoricaCitas() {
        if (!isHorarioValido() || intervaloCitas == null || intervaloCitas <= 0) {
            return 0;
        }
        return (int) (getDuracionMinutos() / intervaloCitas);
    }

    //Obtiene las observaciones limpias
    public String getObservacionesLimpias() {
        return observaciones != null ? observaciones.trim() : null;
    }
}