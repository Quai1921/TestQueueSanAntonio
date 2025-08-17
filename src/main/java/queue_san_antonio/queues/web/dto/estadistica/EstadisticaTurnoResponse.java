package queue_san_antonio.queues.web.dto.estadistica;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticaTurnoResponse {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    // Información del sector
    private SectorInfo sector;

    // Información del empleado (si es estadística específica)
    private EmpleadoInfo empleado;

    // Contadores de turnos
    private Integer turnosGenerados;
    private Integer turnosAtendidos;
    private Integer turnosAusentes;
    private Integer turnosRedirigidos;
    private Integer turnosCancelados;

    // Cálculos derivados
    private Integer totalTurnos;
    private Double porcentajeEficiencia;
    private Double porcentajeAusencias;

    // Tiempos promedio
    private Integer tiempoPromedioEspera; // minutos
    private Integer tiempoPromedioAtencion; // minutos
    private Integer tiempoTotalAtencion; // minutos

    // Análisis de picos
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaPico;
    private Integer cantidadPico;

    // Extremos de espera
    private Integer tiempoMaximoEspera;
    private Integer tiempoMinimoEspera;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaActualizacion;

    // Información anidada para sector
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorInfo {
        private Long id;
        private String codigo;
        private String nombre;
        private String tipo;
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
}