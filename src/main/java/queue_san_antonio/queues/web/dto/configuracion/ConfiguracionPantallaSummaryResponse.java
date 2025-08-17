package queue_san_antonio.queues.web.dto.configuracion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionPantallaSummaryResponse {

    private Long id;
    private String nombre;
    private Integer tiempoMensaje;
    private Integer tiempoTurno;
    private Boolean activo;
    private Integer totalMensajes;
    private Boolean tieneMensajes;
    private LocalDateTime fechaCreacion;
}