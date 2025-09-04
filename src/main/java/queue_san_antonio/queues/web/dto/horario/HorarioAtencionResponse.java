package queue_san_antonio.queues.web.dto.horario;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

//DTO para respuesta de horario de atención
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HorarioAtencionResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("diaSemana")
    private DayOfWeek diaSemana;

    @JsonProperty("diaSemanaTexto")
    private String diaSemanaTexto;

    @JsonProperty("horaInicio")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @JsonProperty("horaFin")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFin;

    @JsonProperty("intervaloCitas")
    private Integer intervaloCitas;

    @JsonProperty("capacidadMaxima")
    private Integer capacidadMaxima;

    @JsonProperty("activo")
    private Boolean activo;

    @JsonProperty("observaciones")
    private String observaciones;

    @JsonProperty("fechaCreacion")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    //Información del sector
    @JsonProperty("sector")
    private SectorInfo sector;

    //Horarios disponibles calculados
    @JsonProperty("horariosDisponibles")
    private List<LocalTime> horariosDisponibles;

    //Estadísticas calculadas
    @JsonProperty("duracionMinutos")
    private Long duracionMinutos;

    @JsonProperty("cantidadTeoricaCitas")
    private Integer cantidadTeoricaCitas;

    @JsonProperty("descripcionCompleta")
    private String descripcionCompleta;

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

        @JsonProperty("tipoSector")
        private String tipoSector;

        @JsonProperty("requiereCitaPrevia")
        private Boolean requiereCitaPrevia;
    }
}