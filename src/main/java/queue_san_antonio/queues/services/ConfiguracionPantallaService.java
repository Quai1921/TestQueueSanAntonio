package queue_san_antonio.queues.services;

import queue_san_antonio.queues.models.ConfiguracionPantalla;

import java.util.List;
import java.util.Optional;

public interface ConfiguracionPantallaService {

    // Operaciones básicas
    ConfiguracionPantalla guardar(ConfiguracionPantalla configuracion);
    Optional<ConfiguracionPantalla> obtenerConfiguracionActiva();
    List<ConfiguracionPantalla> listarTodas();
    Optional<ConfiguracionPantalla> buscarPorId(Long id);

    // Operaciones de negocio
    ConfiguracionPantalla crear(String nombre, Integer tiempoMensaje, Integer tiempoTurno);
    void activar(Long configuracionId);
    void configurarSonido(Long configuracionId, Boolean activo, String archivo, Integer volumen);
    void configurarApariencia(Long configuracionId);
    ConfiguracionPantalla actualizar(Long configuracionId, String nombre, Integer tiempoMensaje, Integer tiempoTurno, String textoEncabezado);
}
