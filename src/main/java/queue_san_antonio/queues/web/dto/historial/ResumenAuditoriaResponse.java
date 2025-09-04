package queue_san_antonio.queues.web.dto.historial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.models.AccionTurno;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenAuditoriaResponse {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer totalDias;

    // Información del empleado
    private String empleadoUsername;
    private String empleadoNombreCompleto;
    private String sectorCodigo;
    private String sectorNombre;

    // Contadores por tipo de acción
    private Integer totalAcciones;
    private Map<AccionTurno, Integer> accionesPorTipo;

    // Estadísticas de productividad
    private Integer turnosGenerados;
    private Integer turnosAtendidos;
    private Integer turnosFinalizados;
    private Integer turnosRedirigidos;
    private Integer turnosAusentes;

    // Análisis de patrones
    private String diaConMasActividad;
    private String accionMasFrecuente;
    private Double promedioAccionesPorDia;

    // Últimas acciones destacadas
    private List<HistorialSummaryResponse> ultimasAcciones;
}