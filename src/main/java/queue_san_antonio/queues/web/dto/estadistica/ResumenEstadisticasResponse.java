package queue_san_antonio.queues.web.dto.estadistica;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenEstadisticasResponse {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer totalDias;

    // Totales agregados
    private Integer totalTurnosGenerados;
    private Integer totalTurnosAtendidos;
    private Integer totalTurnosAusentes;
    private Integer totalTurnosRedirigidos;

    // Promedios
    private Double promedioEficiencia;
    private Double promedioTiempoEspera;
    private Double promedioTiempoAtencion;

    // Mejores y peores
    private String sectorMasEficiente;
    private String sectorMenosEficiente;
    private String empleadoMasProductivo;

    // Top sectores por volumen
    private List<RankingSector> topSectores;

    // Top empleados por eficiencia
    private List<RankingEmpleado> topEmpleados;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankingSector {
        private String codigo;
        private String nombre;
        private Integer totalTurnos;
        private Double eficiencia;
        private Integer posicion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankingEmpleado {
        private String username;
        private String nombreCompleto;
        private String sectorCodigo;
        private Integer totalTurnos;
        private Double eficiencia;
        private Integer posicion;
    }
}