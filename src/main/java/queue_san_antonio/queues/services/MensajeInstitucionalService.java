package queue_san_antonio.queues.services;

import queue_san_antonio.queues.models.MensajeInstitucional;
import queue_san_antonio.queues.models.TipoMensaje;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MensajeInstitucionalService {

    // Operaciones básicas
    MensajeInstitucional guardar(MensajeInstitucional mensaje);
    Optional<MensajeInstitucional> buscarPorId(Long id);
    void eliminar(Long id);

    // Consultas específicas
    List<MensajeInstitucional> listarPorConfiguracion(Long configuracionId);
    List<MensajeInstitucional> listarMensajesVigentes();

    // Operaciones de negocio
    MensajeInstitucional crear(Long configuracionId, TipoMensaje tipo, String titulo,
                               String contenido, String rutaArchivo, Integer duracion, Integer orden, LocalDate fechaInicio, LocalDate fechaFin);
    void activar(Long mensajeId);
    void desactivar(Long mensajeId);
    void establecerVigencia(Long mensajeId, LocalDate fechaInicio, LocalDate fechaFin);

    List<MensajeInstitucional> listarTodosPorConfiguracion(Long configuracionId);
}
