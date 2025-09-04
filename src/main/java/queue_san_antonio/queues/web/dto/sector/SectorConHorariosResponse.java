package queue_san_antonio.queues.web.dto.sector;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.web.dto.horario.HorarioAtencionResponse;

import java.util.List;

//DTO para respuesta completa de sector con sus horarios de atención
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectorConHorariosResponse {

    //Información completa del sector
    @JsonProperty("sector")
    private SectorResponse sector;

    //Lista de horarios de atención
    @JsonProperty("horarios")
    private List<HorarioAtencionResponse> horarios;

    //Estadísticas de horarios
    @JsonProperty("cantidadHorarios")
    private Integer cantidadHorarios;

    @JsonProperty("tieneHorariosActivos")
    private Boolean tieneHorariosActivos;

    // ==========================================
    // MÉTODOS DE CONVENIENCIA
    // ==========================================

    //Verifica si el sector tiene horarios configurados
    public boolean tieneHorarios() {
        return horarios != null && !horarios.isEmpty();
    }

    //Obtiene el número de horarios activos
    public long getHorariosActivosCount() {
        if (horarios == null) return 0;
        return horarios.stream()
                .filter(h -> h.getActivo() != null && h.getActivo())
                .count();
    }

    //Verifica si está completamente configurado (sector activo + horarios activos)
    public boolean estaComplementamenteConfigurado() {
        return sector != null &&
                sector.getActivo() != null &&
                sector.getActivo() &&
                tieneHorariosActivos != null &&
                tieneHorariosActivos;
    }
}