package queue_san_antonio.queues.web.dto.turno;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para respuesta de listado de turnos con metadatos de paginación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnosListadoResponse {

    /**
     * Lista de turnos en la página actual
     */
    private List<TurnoSummaryResponse> turnos;

    /**
     * Total de turnos que cumplen los filtros
     */
    private Long total;

    /**
     * Límite de turnos por página
     */
    private Integer limite;

    /**
     * Offset actual (desde qué posición)
     */
    private Integer offset;

    /**
     * Indica si hay más páginas hacia adelante
     */
    private Boolean hasNext;

    /**
     * Indica si hay páginas anteriores
     */
    private Boolean hasPrevious;

    /**
     * Total de páginas
     */
    private Integer totalPaginas;

    /**
     * Página actual (base 1)
     */
    private Integer paginaActual;

    /**
     * Filtros aplicados en la consulta
     */
    private FiltrosAplicados filtros;

    /**
     * DTO interno para los filtros aplicados
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FiltrosAplicados {

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate fecha;

        private Long sectorId;
    }
}