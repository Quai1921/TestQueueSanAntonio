package queue_san_antonio.queues.web.dto.sector;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.models.TipoSector;

import java.time.LocalDateTime;
import java.util.List;

//DTO para respuesta de sector
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectorResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("nombreCompleto")
    private String nombreCompleto;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("tipoSector")
    private TipoSector tipoSector;

    @JsonProperty("activo")
    private Boolean activo;

    @JsonProperty("requiereCitaPrevia")
    private Boolean requiereCitaPrevia;

    @JsonProperty("capacidadMaxima")
    private Integer capacidadMaxima;

    @JsonProperty("tiempoEstimadoAtencion")
    private Integer tiempoEstimadoAtencion;

    @JsonProperty("color")
    private String color;

    @JsonProperty("ordenVisualizacion")
    private Integer ordenVisualizacion;

    @JsonProperty("observaciones")
    private String observaciones;

    @JsonProperty("fechaCreacion")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    //Información del responsable
    @JsonProperty("responsable")
    private ResponsableInfo responsable;

    @JsonProperty("operadores")
    private List<OperadorInfo> operadores;

    @JsonProperty("turnosAtendidos")
    private Integer turnosAtendidos;

    //Estadísticas del sector
    @JsonProperty("cantidadEmpleados")
    private Integer cantidadEmpleados;

    @JsonProperty("turnosPendientes")
    private Integer turnosPendientes;

    @JsonProperty("tieneHorarios")
    private Boolean tieneHorarios;

    // ==========================================
    // CLASE ANIDADA PARA RESPONSABLE
    // ==========================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponsableInfo {
        @JsonProperty("username")
        private String username;

        @JsonProperty("nombreCompleto")
        private String nombreCompleto;

        @JsonProperty("email")
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperadorInfo {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("username")
        private String username;

        @JsonProperty("nombreCompleto")
        private String nombreCompleto;

        @JsonProperty("cantidadTurnosAtendidos")
        private Integer cantidadTurnosAtendidos;

        @JsonProperty("activo")
        private Boolean activo;
    }
}