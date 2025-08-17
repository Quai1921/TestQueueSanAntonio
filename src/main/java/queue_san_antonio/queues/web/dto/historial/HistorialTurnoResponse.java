package queue_san_antonio.queues.web.dto.historial;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.models.AccionTurno;
import queue_san_antonio.queues.models.EstadoTurno;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialTurnoResponse {

    private Long id;

    // Información del turno
    private TurnoInfo turno;

    // Acción realizada
    private AccionTurno accion;
    private String descripcionAccion;

    // Empleado que realizó la acción
    private EmpleadoInfo empleado;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaHora;

    private String observaciones;
    private String motivo;

    // Estados (para cambios de estado)
    private EstadoTurno estadoAnterior;
    private EstadoTurno estadoNuevo;
    private Boolean esCambioEstado;

    // Prioridades (para cambios de prioridad)
    private Integer prioridadAnterior;
    private Integer prioridadNueva;
    private Boolean esCambioPrioridad;

    // Sectores (para redirecciones)
    private SectorInfo sectorOrigen;
    private SectorInfo sectorDestino;
    private Boolean esRedireccion;

    // Información anidada para turno
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TurnoInfo {
        private Long id;
        private String codigo;
        private EstadoTurno estadoActual;
        private String ciudadanoDni;
        private String ciudadanoNombre;
    }

    // Información anidada para empleado
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmpleadoInfo {
        private Long id;
        private String username;
        private String nombreCompleto;
        private String rol;
    }

    // Información anidada para sector
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorInfo {
        private Long id;
        private String codigo;
        private String nombre;
    }
}