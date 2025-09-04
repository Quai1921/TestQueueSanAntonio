package queue_san_antonio.queues.web.dto.mensaje;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.models.TipoMensaje;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeInstitucionalRequest {

    @NotNull(message = "El tipo de mensaje es obligatorio")
    private TipoMensaje tipo;

    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String titulo;

    @Size(max = 1000, message = "El contenido no puede exceder 1000 caracteres")
    private String contenido;

    @NotNull(message = "La duración del mensaje es obligatoria")
    @Min(value = 3, message = "La duración mínima es 3 segundos")
    @Max(value = 60, message = "La duración máxima es 60 segundos")
    private Integer duracion;

    @Min(value = 0, message = "El orden no puede ser negativo")
    @Max(value = 100, message = "El orden máximo es 100")
    @Builder.Default
    private Integer orden = 0;

    @Size(max = 300, message = "La ruta del archivo no puede exceder 300 caracteres")
    private String rutaArchivo;

    // Campos opcionales para vigencia
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public boolean esValidoParaTipo() {
        if (tipo == TipoMensaje.IMAGEN || tipo == TipoMensaje.VIDEO) {
            return rutaArchivo != null && !rutaArchivo.trim().isEmpty();
        }
        return true;
    }

    // NUEVO: Método para validar que el contenido sea obligatorio solo para TEXTO
    public boolean esContenidoValido() {
        if (tipo == TipoMensaje.TEXTO) {
            return contenido != null && !contenido.trim().isEmpty();
        }
        // Para IMAGEN y VIDEO el contenido es opcional
        return true;
    }
}