package queue_san_antonio.queues.services;

import queue_san_antonio.queues.models.TipoTurno;
import queue_san_antonio.queues.models.Turno;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TurnoService {

    // Operaciones CRUD básicas
    Turno guardar(Turno turno);
    Optional<Turno> buscarPorId(Long id);
    Optional<Turno> buscarPorCodigo(String codigo);
    Optional<Turno> buscarPorCodigoYFecha(String codigo, LocalDate fecha);

    // Generación de turnos
    Turno generarTurno(Long ciudadanoId, Long sectorId, TipoTurno tipo, Long empleadoId);
    Turno generarTurnoEspecial(Long ciudadanoId, Long sectorId, LocalDate fechaCita, LocalTime horaCita, Long empleadoId);

    // Gestión de cola
    List<Turno> obtenerColaEspera(Long sectorId);
    Optional<Turno> obtenerProximoTurno(Long sectorId);
    int contarTurnosPendientes(Long sectorId);

    // Operaciones de atención
    Turno llamarTurno(Long turnoId, Long empleadoId, String observaciones);
    Turno iniciarAtencion(Long turnoId, Long empleadoId);
    Turno finalizarAtencion(Long turnoId, String observaciones);
    Turno marcarAusente(Long turnoId, Long empleadoId, String observaciones);

    // Redirección
    Turno redirigirTurno(Long turnoId, Long nuevoSectorId, String motivo, String observaciones, Long empleadoId);

    // Consultas
    List<Turno> listarTurnosDelDia(Long sectorId, LocalDate fecha);
    List<Turno> listarTurnosCiudadano(Long ciudadanoId);
    List<Turno> listarTurnosPendientesCiudadano(Long ciudadanoId);

    // Validaciones
    boolean ciudadanoTieneTurnoPendiente(Long ciudadanoId);

    // Generación de códigos
    String generarCodigoTurno(String codigoSector, LocalDate fecha);

    // Métodos para historial
    List<Turno> listarTurnosConFiltros(int limite, int offset, LocalDate fecha, Long sectorId);
    long contarTurnosConFiltros(LocalDate fecha, Long sectorId);
    List<Turno> listarTodos(int limite);

}
