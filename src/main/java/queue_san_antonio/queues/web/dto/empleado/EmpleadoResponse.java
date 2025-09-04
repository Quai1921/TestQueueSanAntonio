package queue_san_antonio.queues.web.dto.empleado;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.models.RolEmpleado;

import java.time.LocalDateTime;

//DTO para respuesta completa de empleado
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("apellido")
    private String apellido;

    @JsonProperty("nombreCompleto")
    private String nombreCompleto;

    @JsonProperty("email")
    private String email;

    @JsonProperty("dni")
    private String dni;

    @JsonProperty("telefono")
    private String telefono;

    @JsonProperty("rol")
    private RolEmpleado rol;

    @JsonProperty("activo")
    private Boolean activo;

    @JsonProperty("observaciones")
    private String observaciones;

    @JsonProperty("fechaCreacion")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    @JsonProperty("ultimoAcceso")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ultimoAcceso;

    //Información del sector asignado
    @JsonProperty("sector")
    private SectorInfo sector;

    //Estadísticas del empleado
    @JsonProperty("cantidadTurnosAtendidos")
    private Integer cantidadTurnosAtendidos;

    @JsonProperty("puedeAcceder")
    private Boolean puedeAcceder;

    @JsonProperty("esAdministrador")
    private Boolean esAdministrador;

    @JsonProperty("esResponsable")
    private Boolean esResponsable;

    // ==========================================
    // CLASE ANIDADA PARA INFORMACIÓN DEL SECTOR
    // ==========================================

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

        @JsonProperty("nombreCompleto")
        private String nombreCompleto;

        @JsonProperty("activo")
        private Boolean activo;
    }
}