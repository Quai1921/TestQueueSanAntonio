package queue_san_antonio.queues.services;

import queue_san_antonio.queues.models.EstadisticaTurno;

import java.time.LocalDate;
import java.util.List;

public interface EstadisticaTurnoService {

    // Operaciones básicas
    EstadisticaTurno guardar(EstadisticaTurno estadistica);

    // Obtener/crear estadísticas
    EstadisticaTurno obtenerEstadisticaDelDia(Long sectorId, LocalDate fecha);
    EstadisticaTurno obtenerEstadisticaEmpleado(Long empleadoId, Long sectorId, LocalDate fecha);

    // Actualización automática
    void actualizarTurnoGenerado(Long sectorId, Long empleadoId);
    void actualizarTurnoAtendido(Long sectorId, Long empleadoId, int tiempoEspera, int tiempoAtencion);
    void actualizarTurnoAusente(Long sectorId, Long empleadoId);
    void actualizarTurnoRedirigido(Long sectorId, Long empleadoId);

    // Consultas para reportes
    List<EstadisticaTurno> obtenerEstadisticasSector(Long sectorId, LocalDate fechaInicio, LocalDate fechaFin);
    List<EstadisticaTurno> obtenerEstadisticasGenerales(LocalDate fechaInicio, LocalDate fechaFin);
    List<EstadisticaTurno> obtenerEstadisticasEmpleado(Long empleadoId, LocalDate fechaInicio, LocalDate fechaFin);

    // Cálculos y análisis
    void calcularHoraPico(Long sectorId, LocalDate fecha);
    void generarReporteDelDia(LocalDate fecha);
}
