package queue_san_antonio.queues.web.dto.estadistica;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticaSummaryResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    private String sectorCodigo;
    private String sectorNombre;
    private String empleadoUsername;
    private String empleadoNombre;

    private Integer turnosGenerados;
    private Integer turnosAtendidos;
    private Integer turnosAusentes;

    private Double porcentajeEficiencia;
    private Integer tiempoPromedioEspera;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaPico;
    private Integer cantidadPico;
}