package queue_san_antonio.queues.web.dto.configuracion;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigurarAparienciaRequest {

    @Size(max = 20, message = "El tema no puede exceder 20 caracteres")
    private String tema;

    private Boolean mostrarLogo;

    @Size(max = 200, message = "La ruta del logo no puede exceder 200 caracteres")
    private String rutaLogo;
}