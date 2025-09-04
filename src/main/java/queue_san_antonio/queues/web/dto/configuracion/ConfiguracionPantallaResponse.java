package queue_san_antonio.queues.web.dto.configuracion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.web.dto.mensaje.MensajeInstitucionalSummaryResponse;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionPantallaResponse {

    private Long id;
    private String nombre;
    private Integer tiempoMensaje;
    private Integer tiempoTurno;
    private String textoEncabezado;
    private Boolean sonidoActivo;
    private String archivoSonido;
    private Integer volumenSonido;
    private Boolean animacionesActivas;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    // Mensajes asociados (solo para configuraciones completas)
    private List<MensajeInstitucionalSummaryResponse> mensajes;
    private Integer totalMensajes;
    private Boolean tieneMensajes;
}