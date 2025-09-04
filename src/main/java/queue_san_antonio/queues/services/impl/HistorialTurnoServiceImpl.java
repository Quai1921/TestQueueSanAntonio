package queue_san_antonio.queues.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import queue_san_antonio.queues.models.*;
import queue_san_antonio.queues.repositories.HistorialTurnoRepository;
import queue_san_antonio.queues.services.HistorialTurnoService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HistorialTurnoServiceImpl implements HistorialTurnoService {

    private final HistorialTurnoRepository historialTurnoRepository;

    @Override
    public HistorialTurno guardar(HistorialTurno historial) {
        log.debug("Guardando registro de historial para turno: {}",
                historial.getTurno() != null ? historial.getTurno().getCodigo() : "null");
        return historialTurnoRepository.save(historial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialTurno> listarPorTurno(Long turnoId) {
        if (turnoId == null) {
            return List.of();
        }
        return historialTurnoRepository.findByTurnoIdOrderByFechaHoraAsc(turnoId);
    }

    @Override
    public void registrarGeneracion(Turno turno, Empleado empleado) {
        if (turno == null) {
            throw new IllegalArgumentException("El turno no puede ser nulo");
        }

        log.debug("Registrando generación del turno: {}", turno.getCodigo());

        HistorialTurno registro = HistorialTurno.crearRegistroGeneracion(turno, empleado);
        guardar(registro);

        log.trace("Generación registrada para turno {} - Empleado: {}",
                turno.getCodigo(), empleado != null ? empleado.getUsername() : "Sistema");
    }

    @Override
    public void registrarLlamado(Turno turno, Empleado empleado) {
        if (turno == null) {
            throw new IllegalArgumentException("El turno no puede ser nulo");
        }
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo para el llamado");
        }

        log.debug("Registrando llamado del turno: {} por empleado: {}",
                turno.getCodigo(), empleado.getUsername());

        HistorialTurno registro = HistorialTurno.crearRegistroLlamado(turno, empleado);
        guardar(registro);

        log.trace("Llamado registrado para turno {} por empleado {}",
                turno.getCodigo(), empleado.getUsername());
    }

    @Override
    public void registrarInicioAtencion(Turno turno, Empleado empleado) {
        if (turno == null) {
            throw new IllegalArgumentException("El turno no puede ser nulo");
        }
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo para el inicio de atención");
        }

        log.debug("Registrando inicio de atención del turno: {} por empleado: {}",
                turno.getCodigo(), empleado.getUsername());

        HistorialTurno registro = HistorialTurno.crearRegistroInicioAtencion(turno, empleado);
        guardar(registro);

        log.trace("Inicio de atención registrado para turno {} por empleado {}",
                turno.getCodigo(), empleado.getUsername());
    }

    @Override
    public void registrarFinalizacion(Turno turno, Empleado empleado, String observaciones) {
        if (turno == null) {
            throw new IllegalArgumentException("El turno no puede ser nulo");
        }
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo para la finalización");
        }

        log.debug("Registrando finalización del turno: {} por empleado: {}",
                turno.getCodigo(), empleado.getUsername());

        // Limpiar observaciones
        String observacionesLimpias = observaciones != null ? observaciones.trim() : "Atención finalizada";
        if (observacionesLimpias.isEmpty()) {
            observacionesLimpias = "Atención finalizada";
        }

        HistorialTurno registro = HistorialTurno.crearRegistroFinalizacion(turno, empleado, observacionesLimpias);
        guardar(registro);

        log.trace("Finalización registrada para turno {} por empleado {} - Observaciones: {}",
                turno.getCodigo(), empleado.getUsername(),
                observacionesLimpias.length() > 50 ? observacionesLimpias.substring(0, 50) + "..." : observacionesLimpias);
    }

    @Override
    public void registrarRedireccion(Turno turno, Empleado empleado, Sector sectorOrigen,
                                     Sector sectorDestino, String motivo) {
        if (turno == null) {
            throw new IllegalArgumentException("El turno no puede ser nulo");
        }
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo para la redirección");
        }
        if (sectorOrigen == null) {
            throw new IllegalArgumentException("El sector origen no puede ser nulo");
        }
        if (sectorDestino == null) {
            throw new IllegalArgumentException("El sector destino no puede ser nulo");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de redirección es obligatorio");
        }

        log.debug("Registrando redirección del turno: {} de {} a {} por empleado: {} - Motivo: {}",
                turno.getCodigo(), sectorOrigen.getCodigo(), sectorDestino.getCodigo(),
                empleado.getUsername(), motivo);

        String motivoLimpio = motivo.trim();

        HistorialTurno registro = HistorialTurno.crearRegistroRedireccion(
                turno, empleado, sectorOrigen, sectorDestino, motivoLimpio);
        guardar(registro);

        log.info("Redirección registrada: turno {} redirigido de {} a {} por {} - Motivo: {}",
                turno.getCodigo(), sectorOrigen.getCodigo(), sectorDestino.getCodigo(),
                empleado.getUsername(), motivoLimpio);
    }

    @Override
    public void registrarAusente(Turno turno, Empleado empleado) {
        if (turno == null) {
            throw new IllegalArgumentException("El turno no puede ser nulo");
        }
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo para marcar ausente");
        }

        log.debug("Registrando ausencia del turno: {} por empleado: {}",
                turno.getCodigo(), empleado.getUsername());

        HistorialTurno registro = HistorialTurno.crearRegistroAusente(turno, empleado);
        guardar(registro);

        log.info("Ausencia registrada para turno {} por empleado {}",
                turno.getCodigo(), empleado.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialTurno> listarAccionesEmpleado(Long empleadoId, LocalDate fechaInicio, LocalDate fechaFin) {
        if (empleadoId == null) {
            return List.of();
        }

        // Convertir fechas a LocalDateTime para la consulta
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : LocalDateTime.now();

        log.debug("Consultando acciones del empleado {} entre {} y {}", empleadoId, inicio, fin);

        return historialTurnoRepository.findByEmpleadoIdAndFechaHoraBetweenOrderByFechaHoraDesc(
                empleadoId, inicio, fin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialTurno> listarUltimasAcciones(int limite) {
        if (limite <= 0) {
            limite = 50; // Límite por defecto
        }
        if (limite > 1000) {
            limite = 1000; // Límite máximo para evitar problemas de rendimiento
        }

        log.debug("Consultando últimas {} acciones del sistema", limite);

        PageRequest pageRequest = PageRequest.of(0, limite);
        return historialTurnoRepository.findUltimasAcciones(pageRequest);
    }

//    Método adicional para registrar cambios manuales de estado o prioridad
    public void registrarCambioManual(Turno turno, Empleado empleado, AccionTurno accion,
                                      String motivo, EstadoTurno estadoAnterior, EstadoTurno estadoNuevo) {
        if (turno == null) {
            throw new IllegalArgumentException("El turno no puede ser nulo");
        }
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo");
        }
        if (accion == null) {
            throw new IllegalArgumentException("La acción no puede ser nula");
        }

        log.debug("Registrando cambio manual en turno: {} por empleado: {} - Acción: {}",
                turno.getCodigo(), empleado.getUsername(), accion);

        HistorialTurno registro = HistorialTurno.builder()
                .turno(turno)
                .accion(accion)
                .empleado(empleado)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .motivo(motivo != null ? motivo.trim() : null)
                .observaciones("Cambio manual realizado por " + empleado.getNombreCompleto())
                .build();

        guardar(registro);

        log.info("Cambio manual registrado: turno {} - Acción: {} por empleado {}",
                turno.getCodigo(), accion, empleado.getUsername());
    }

    //Método adicional para obtener estadísticas de acciones por período
    @Transactional(readOnly = true)
    public long contarAccionesPorTipo(AccionTurno accion, LocalDate fechaInicio, LocalDate fechaFin) {
        if (accion == null) {
            return 0;
        }

        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : LocalDateTime.now();

        log.debug("Contando acciones de tipo {} entre {} y {}", accion, inicio, fin);

        // Esta consulta requeriría un método adicional en el repository
        // Por simplicidad, se podría implementar con una consulta nativa o JPQL
        return 0; // Placeholder - implementar según necesidades específicas
    }

    //Método para obtener el historial completo de un turno con descripción legible
    @Transactional(readOnly = true)
    public List<String> obtenerHistorialLegible(Long turnoId) {
        if (turnoId == null) {
            return List.of();
        }

        List<HistorialTurno> historial = listarPorTurno(turnoId);

        return historial.stream()
                .map(registro -> String.format("[%s] %s - %s",
                        registro.getFechaHora().toString(),
                        registro.getDescripcionAccion(),
                        registro.getNombreEmpleado()))
                .toList();
    }
}