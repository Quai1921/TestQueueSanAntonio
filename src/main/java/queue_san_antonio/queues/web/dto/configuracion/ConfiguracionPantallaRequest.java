package queue_san_antonio.queues.web.dto.configuracion;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionPantallaRequest {

    @NotBlank(message = "El nombre de la configuración es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotNull(message = "El tiempo de mensaje es obligatorio")
    @Min(value = 3, message = "El tiempo mínimo de mensaje es 3 segundos")
    @Max(value = 60, message = "El tiempo máximo de mensaje es 60 segundos")
    private Integer tiempoMensaje;

    @NotNull(message = "El tiempo de turno es obligatorio")
    @Min(value = 3, message = "El tiempo mínimo de turno es 3 segundos")
    @Max(value = 30, message = "El tiempo máximo de turno es 30 segundos")
    private Integer tiempoTurno;

    @Size(max = 200, message = "El texto del encabezado no puede exceder 200 caracteres")
    private String textoEncabezado;

    @Builder.Default
    private Boolean sonidoActivo = true;

    @Size(max = 200, message = "La ruta del archivo de sonido no puede exceder 200 caracteres")
    private String archivoSonido;

    @Min(value = 0, message = "El volumen mínimo es 0")
    @Max(value = 100, message = "El volumen máximo es 100")
    @Builder.Default
    private Integer volumenSonido = 70;

    @Builder.Default
    private Boolean animacionesActivas = true;

}