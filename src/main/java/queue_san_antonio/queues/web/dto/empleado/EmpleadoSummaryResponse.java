package queue_san_antonio.queues.web.dto.empleado;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.models.RolEmpleado;

import java.time.LocalDateTime;
import java.util.List;

//DTO para respuesta resumida de empleado (para listas)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoSummaryResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("nombreCompleto")
    private String nombreCompleto;

    @JsonProperty("email")
    private String email;

    @JsonProperty("rol")
    private RolEmpleado rol;

    @JsonProperty("activo")
    private Boolean activo;

    @JsonProperty("ultimoAcceso")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ultimoAcceso;

    //Información básica del sector
    @JsonProperty("sectorCodigo")
    private String sectorCodigo;

    @JsonProperty("sectorNombre")
    private String sectorNombre;

    @JsonProperty("sectoresResponsable")
    private List<SectorInfo> sectoresResponsable;


    //Estadísticas básicas
    @JsonProperty("cantidadTurnosAtendidos")
    private Integer cantidadTurnosAtendidos;

    @JsonProperty("puedeAcceder")
    private Boolean puedeAcceder;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorInfo {
        @JsonProperty("codigo")
        private String codigo;

        @JsonProperty("nombre")
        private String nombre;

        @JsonProperty("id")
        private Long id;
    }
}