package queue_san_antonio.queues.web.dto.sector;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.models.TipoSector;

//DTO para crear/actualizar sector
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectorUpdateRequest {

    //Nombre del sector
    @NotBlank(message = "El nombre del sector es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @JsonProperty("nombre")
    private String nombre;

    //Descripción del sector
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    @JsonProperty("descripcion")
    private String descripcion;

    //Tipo de sector
    @NotNull(message = "El tipo de sector es obligatorio")
    @JsonProperty("tipoSector")
    private TipoSector tipoSector;

    //Configuración del sector
    @JsonProperty("requiereCitaPrevia")
    @Builder.Default
    private Boolean requiereCitaPrevia = false;

    @Min(value = 1, message = "La capacidad máxima debe ser mayor a 0")
    @JsonProperty("capacidadMaxima")
    @Builder.Default
    private Integer capacidadMaxima = 1;

    @Min(value = 1, message = "El tiempo estimado debe ser mayor a 0")
    @JsonProperty("tiempoEstimadoAtencion")
    @Builder.Default
    private Integer tiempoEstimadoAtencion = 15;

    //Configuración visual
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe ser un código hexadecimal válido (#RRGGBB)")
    @JsonProperty("color")
    private String color;

    @Min(value = 0, message = "El orden de visualización debe ser mayor o igual a 0")
    @JsonProperty("ordenVisualizacion")
    private Integer ordenVisualizacion;

    //Observaciones adicionales
    @JsonProperty("observaciones")
    private String observaciones;
}