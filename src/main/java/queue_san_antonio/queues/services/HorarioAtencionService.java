package queue_san_antonio.queues.services;

import queue_san_antonio.queues.models.HorarioAtencion;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface HorarioAtencionService {

    // Operaciones CRUD básicas
    HorarioAtencion guardar(HorarioAtencion horario);
    Optional<HorarioAtencion> buscarPorId(Long id);

    // Consultas específicas
    List<HorarioAtencion> listarPorSector(Long sectorId);
    List<HorarioAtencion> listarPorDia(Long sectorId, DayOfWeek diaSemana);

    // Operaciones de negocio
    HorarioAtencion crear(Long sectorId, DayOfWeek diaSemana, LocalTime horaInicio,
                          LocalTime horaFin, Integer intervaloCitas);
    List<LocalTime> obtenerHorariosDisponibles(Long sectorId, DayOfWeek diaSemana);
    boolean estaEnHorarioAtencion(Long sectorId, DayOfWeek diaSemana, LocalTime hora);

    // Validaciones
    boolean hayConflictoHorarios(Long sectorId, DayOfWeek diaSemana,
                                 LocalTime horaInicio, LocalTime horaFin);

    void activar(Long horarioId);
    void desactivar(Long horarioId);

    // Validar si una fecha y hora están en los horarios configurados del sector
    boolean validarFechaHoraTurnoEspecial(Long sectorId, LocalDate fecha, LocalTime hora);

    // Obtener horarios disponibles para una fecha específica considerando capacidad
    List<LocalTime> obtenerHorariosDisponiblesParaFecha(Long sectorId, LocalDate fecha);
}
