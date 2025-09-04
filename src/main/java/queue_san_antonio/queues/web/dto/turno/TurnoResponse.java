package queue_san_antonio.queues.web.dto.turno;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.models.EstadoTurno;
import queue_san_antonio.queues.models.TipoTurno;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

//DTO para respuesta completa de turno
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnoResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("estado")
    private EstadoTurno estado;

    @JsonProperty("tipo")
    private TipoTurno tipo;

    @JsonProperty("prioridad")
    private Integer prioridad;

    //Información del ciudadano (datos públicos)
    @JsonProperty("ciudadano")
    private CiudadanoInfo ciudadano;

    //Información del sector
    @JsonProperty("sector")
    private SectorInfo sector;

    //Empleado que atiende (si aplica)
    @JsonProperty("empleadoAtencion")
    private EmpleadoInfo empleadoAtencion;

    //Fechas y tiempos
    @JsonProperty("fechaGeneracion")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaGeneracion;

    @JsonProperty("fechaLlamado")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaLlamado;

    @JsonProperty("fechaAtencion")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaAtencion;

    @JsonProperty("fechaFinalizacion")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaFinalizacion;

    //Para turnos especiales
    @JsonProperty("fechaCita")
    private LocalDate fechaCita;

    @JsonProperty("horaCita")
    private LocalTime horaCita;

    @JsonProperty("observaciones")
    private String observaciones;

    //Tiempos calculados (en minutos)
    @JsonProperty("tiempoEspera")
    private Long tiempoEspera;

    @JsonProperty("tiempoAtencion")
    private Long tiempoAtencion;

    // ==========================================
    // CLASES ANIDADAS PARA INFORMACIÓN BÁSICA
    // ==========================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CiudadanoInfo {

        @JsonProperty("id")
        private Long id;

        @JsonProperty("dni")
        private String dni;

        @JsonProperty("nombreCompleto")
        private String nombreCompleto;

        @JsonProperty("esPrioritario")
        private Boolean esPrioritario;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorInfo {

        @JsonProperty("id")
        private Long id;

        @JsonProperty("codigo")
        private String codigo;

        @JsonProperty("nombre")
        private String nombre;

        @JsonProperty("tipo")
        private String tipo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmpleadoInfo {

        @JsonProperty("id")
        private Long id;

        @JsonProperty("username")
        private String username;

        @JsonProperty("nombreCompleto")
        private String nombreCompleto;
    }
}