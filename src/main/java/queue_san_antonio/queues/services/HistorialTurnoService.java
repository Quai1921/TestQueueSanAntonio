package queue_san_antonio.queues.services;

import queue_san_antonio.queues.models.Empleado;
import queue_san_antonio.queues.models.HistorialTurno;
import queue_san_antonio.queues.models.Sector;
import queue_san_antonio.queues.models.Turno;

import java.time.LocalDate;
import java.util.List;

public interface HistorialTurnoService {

    // Operaciones básicas
    HistorialTurno guardar(HistorialTurno historial);
    List<HistorialTurno> listarPorTurno(Long turnoId);

    // Registro automático de acciones
    void registrarGeneracion(Turno turno, Empleado empleado);
    void registrarLlamado(Turno turno, Empleado empleado);
    void registrarInicioAtencion(Turno turno, Empleado empleado);
    void registrarFinalizacion(Turno turno, Empleado empleado, String observaciones);
    void registrarRedireccion(Turno turno, Empleado empleado, Sector sectorOrigen,
                              Sector sectorDestino, String motivo);
    void registrarAusente(Turno turno, Empleado empleado);

    // Consultas de auditoría
    List<HistorialTurno> listarAccionesEmpleado(Long empleadoId, LocalDate fechaInicio, LocalDate fechaFin);
    List<HistorialTurno> listarUltimasAcciones(int limite);
}
