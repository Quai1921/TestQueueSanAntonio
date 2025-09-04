package queue_san_antonio.queues.services.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import queue_san_antonio.queues.models.HorarioAtencion;
import queue_san_antonio.queues.models.Sector;
import queue_san_antonio.queues.repositories.HorarioAtencionRepository;
import queue_san_antonio.queues.repositories.SectorRepository;
import queue_san_antonio.queues.services.HorarioAtencionService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HorarioAtencionServiceImpl implements HorarioAtencionService {

    private final HorarioAtencionRepository horarioAtencionRepository;
    private final SectorRepository sectorRepository;

    @Override
    public HorarioAtencion guardar(HorarioAtencion horario) {
        log.debug("Guardando horario de atención: {} - {} de {} a {}",
                horario.getSector() != null ? horario.getSector().getCodigo() : "null",
                horario.getDiaSemana(),
                horario.getHoraInicio(),
                horario.getHoraFin());

        // Validar que el horario sea consistente
        if (!horario.esValido()) {
            throw new IllegalArgumentException("El horario de atención no es válido");
        }

        return horarioAtencionRepository.save(horario);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HorarioAtencion> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return horarioAtencionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorarioAtencion> listarPorSector(Long sectorId) {
        if (sectorId == null) {
            return List.of();
        }
        log.debug("Listando horarios del sector ID: {}", sectorId);
        return horarioAtencionRepository.findBySectorIdOrderByDiaSemanaAscHoraInicioAsc(sectorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorarioAtencion> listarPorDia(Long sectorId, DayOfWeek diaSemana) {
        if (sectorId == null || diaSemana == null) {
            return List.of();
        }
        return horarioAtencionRepository.findHorariosPorDia(sectorId, diaSemana);
    }

    @Override
    public HorarioAtencion crear(Long sectorId, DayOfWeek diaSemana, LocalTime horaInicio,
                                 LocalTime horaFin, Integer intervaloCitas) {
        // Validar parámetros obligatorios
        validarParametrosCreacion(sectorId, diaSemana, horaInicio, horaFin);

        log.info("Creando horario de atención para sector {} - {} de {} a {}",
                sectorId, diaSemana, horaInicio, horaFin);

        // Buscar sector
        Sector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró sector con ID: " + sectorId));

        if (!sector.estaActivo()) {
            throw new IllegalStateException("No se puede crear horario para sector inactivo: " + sector.getCodigo());
        }

        // Validar conflictos de horarios
        if (hayConflictoHorarios(sectorId, diaSemana, horaInicio, horaFin)) {
            throw new IllegalStateException("El horario se superpone con otro horario existente del mismo sector");
        }

        // Establecer valores por defecto
        if (intervaloCitas == null || intervaloCitas <= 0) {
            intervaloCitas = 30; // 30 minutos por defecto
        }

        // Crear nuevo horario
        HorarioAtencion nuevoHorario = HorarioAtencion.builder()
                .sector(sector)
                .diaSemana(diaSemana)
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .intervaloCitas(intervaloCitas)
                .capacidadMaxima(1) // 1 cita simultánea por defecto
                .activo(true)
                .build();

        log.debug("Creando horario: Sector {} - {} de {} a {} (intervalo: {}min)",
                sector.getCodigo(), diaSemana, horaInicio, horaFin, intervaloCitas);

        return guardar(nuevoHorario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> obtenerHorariosDisponibles(Long sectorId, DayOfWeek diaSemana) {
        if (sectorId == null || diaSemana == null) {
            return List.of();
        }

        log.debug("Obteniendo horarios disponibles para sector {} en {}", sectorId, diaSemana);

        List<HorarioAtencion> horariosDelDia = listarPorDia(sectorId, diaSemana);

        if (horariosDelDia.isEmpty()) {
            log.debug("No hay horarios configurados para sector {} en {}", sectorId, diaSemana);
            return List.of();
        }

        // Combinar todos los horarios disponibles de todos los rangos del día
        return horariosDelDia.stream()
                .filter(HorarioAtencion::estaActivo)
                .flatMap(horario -> horario.getHorariosDisponibles().stream())
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaEnHorarioAtencion(Long sectorId, DayOfWeek diaSemana, LocalTime hora) {
        if (sectorId == null || diaSemana == null || hora == null) {
            return false;
        }

        log.debug("Verificando si {} está en horario de atención para sector {} en {}",
                hora, sectorId, diaSemana);

        List<HorarioAtencion> horariosDelDia = listarPorDia(sectorId, diaSemana);

        return horariosDelDia.stream()
                .filter(HorarioAtencion::estaActivo)
                .anyMatch(horario -> horario.estaEnHorario(hora));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hayConflictoHorarios(Long sectorId, DayOfWeek diaSemana,
                                        LocalTime horaInicio, LocalTime horaFin) {
        if (sectorId == null || diaSemana == null || horaInicio == null || horaFin == null) {
            return false;
        }

        log.debug("Verificando conflictos de horarios para sector {} - {} de {} a {}",
                sectorId, diaSemana, horaInicio, horaFin);

        // Validar que hora inicio sea antes que hora fin
        if (!horaInicio.isBefore(horaFin)) {
            return true; // Conflicto: horario inválido
        }

        List<HorarioAtencion> horariosExistentes = listarPorDia(sectorId, diaSemana);

        // Crear horario temporal para verificar superposiciones
        HorarioAtencion horarioTemporal = HorarioAtencion.builder()
                .diaSemana(diaSemana)
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .build();

        boolean hayConflicto = horariosExistentes.stream()
                .filter(HorarioAtencion::estaActivo)
                .anyMatch(horarioExistente -> horarioTemporal.seSuperponeCon(horarioExistente));

        if (hayConflicto) {
            log.warn("Conflicto detectado: el horario {} de {} a {} se superpone con horarios existentes",
                    diaSemana, horaInicio, horaFin);
        }

        return hayConflicto;
    }

    //Método adicional para activar horarios
    @Override
    @Transactional
    public void activar(Long horarioId) {
        if (horarioId == null) {
            throw new IllegalArgumentException("El ID del horario no puede ser nulo");
        }

        log.info("Activando horario ID: {}", horarioId);

        HorarioAtencion horario = buscarPorId(horarioId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró horario con ID: " + horarioId));

        if (horario.estaActivo()) {
            log.debug("El horario {} ya estaba activo", horarioId);
            return;
        }

        horario.activar();
        guardar(horario);

        log.debug("Horario {} activado exitosamente", horarioId);
    }

    //Método adicional para desactivar horarios
    @Override
    @Transactional
    public void desactivar(Long horarioId) {
        if (horarioId == null) {
            throw new IllegalArgumentException("El ID del horario no puede ser nulo");
        }

        log.info("Desactivando horario ID: {}", horarioId);

        HorarioAtencion horario = buscarPorId(horarioId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró horario con ID: " + horarioId));

        if (!horario.estaActivo()) {
            log.debug("El horario {} ya estaba inactivo", horarioId);
            return;
        }

        horario.desactivar();
        guardar(horario);

        log.debug("Horario {} desactivado exitosamente", horarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validarFechaHoraTurnoEspecial(Long sectorId, LocalDate fecha, LocalTime hora) {
        if (sectorId == null || fecha == null || hora == null) {
            return false;
        }

        // Validar que la fecha no sea anterior a hoy
        if (fecha.isBefore(LocalDate.now(ZoneId.of("America/Argentina/Cordoba")))) {
            return false;
        }

        // Obtener el día de la semana de la fecha
        DayOfWeek diaSemana = fecha.getDayOfWeek();

        // Obtener horarios del día
        List<HorarioAtencion> horariosDelDia = listarPorDia(sectorId, diaSemana);

        if (horariosDelDia.isEmpty()) {
            return false;
        }

        // Verificar si la hora está en alguno de los horarios disponibles configurados
        for (HorarioAtencion horario : horariosDelDia) {
            if (horario.estaActivo()) {
                // Obtener todos los horarios disponibles según el intervalo
                List<LocalTime> horariosDisponibles = horario.getHorariosDisponibles();

                // Verificar si la hora solicitada coincide exactamente con un horario disponible
                if (horariosDisponibles.contains(hora)) {
                    return true;
                }
            }
        }

        // Verificar si está en horario de atención
//        return estaEnHorarioAtencion(sectorId, diaSemana, hora);
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> obtenerHorariosDisponiblesParaFecha(Long sectorId, LocalDate fecha) {
        if (sectorId == null || fecha == null) {
            return List.of();
        }

        // Verificar que la fecha no sea anterior a hoy
        if (fecha.isBefore(LocalDate.now(ZoneId.of("America/Argentina/Cordoba")))) {
            return List.of();
        }

        DayOfWeek diaSemana = fecha.getDayOfWeek();

        // Obtener horarios del día
        List<HorarioAtencion> horariosDelDia = listarPorDia(sectorId, diaSemana);

        if (horariosDelDia.isEmpty()) {
            return List.of();
        }

        // Generar todos los horarios posibles
        Set<LocalTime> horariosSet = new HashSet<>();

        for (HorarioAtencion horario : horariosDelDia) {
            if (horario.estaActivo()) {
                List<LocalTime> horariosDisponibles = horario.getHorariosDisponibles();
                horariosSet.addAll(horariosDisponibles);
            }
        }

        return horariosSet.stream()
                .sorted()
                .collect(Collectors.toList());
    }












    //Método adicional para actualizar horarios existentes
    public HorarioAtencion actualizar(Long horarioId, LocalTime horaInicio, LocalTime horaFin,
                                      Integer intervaloCitas, Integer capacidadMaxima) {
        if (horarioId == null) {
            throw new IllegalArgumentException("El ID del horario no puede ser nulo");
        }

        log.info("Actualizando horario ID: {}", horarioId);

        HorarioAtencion horario = buscarPorId(horarioId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró horario con ID: " + horarioId));

        // Validar nuevos horarios si se proporcionan
        if (horaInicio != null && horaFin != null) {
            if (!horaInicio.isBefore(horaFin)) {
                throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
            }

            // Verificar conflictos excluyendo el horario actual
            List<HorarioAtencion> otrosHorarios = listarPorDia(horario.getSector().getId(), horario.getDiaSemana())
                    .stream()
                    .filter(h -> !h.getId().equals(horarioId))
                    .toList();

            HorarioAtencion horarioTemporal = HorarioAtencion.builder()
                    .diaSemana(horario.getDiaSemana())
                    .horaInicio(horaInicio)
                    .horaFin(horaFin)
                    .build();

            boolean hayConflicto = otrosHorarios.stream()
                    .filter(HorarioAtencion::estaActivo)
                    .anyMatch(h -> horarioTemporal.seSuperponeCon(h));

            if (hayConflicto) {
                throw new IllegalStateException("El nuevo horario se superpone con otros horarios existentes");
            }
        }

        // Actualizar horario
        horario.actualizarHorario(
                horaInicio != null ? horaInicio : horario.getHoraInicio(),
                horaFin != null ? horaFin : horario.getHoraFin(),
                intervaloCitas != null ? intervaloCitas : horario.getIntervaloCitas(),
                capacidadMaxima != null ? capacidadMaxima : horario.getCapacidadMaxima()
        );

        HorarioAtencion horarioActualizado = guardar(horario);

        log.debug("Horario {} actualizado exitosamente", horarioId);

        return horarioActualizado;
    }

    //Método adicional para obtener el próximo horario disponible
    @Transactional(readOnly = true)
    public Optional<LocalTime> obtenerProximoHorarioDisponible(Long sectorId, DayOfWeek diaSemana, LocalTime horaReferencia) {
        if (sectorId == null || diaSemana == null) {
            return Optional.empty();
        }

        final LocalTime horaReferenciaFinal = horaReferencia != null ? horaReferencia : LocalTime.now();

        log.debug("Buscando próximo horario disponible para sector {} en {} después de {}",
                sectorId, diaSemana, horaReferencia);

        List<LocalTime> horariosDisponibles = obtenerHorariosDisponibles(sectorId, diaSemana);

        return horariosDisponibles.stream()
                .filter(hora -> hora.isAfter(horaReferencia))
                .findFirst();
    }

    //Valida los parámetros obligatorios para la creación de horarios
    private void validarParametrosCreacion(Long sectorId, DayOfWeek diaSemana,
                                           LocalTime horaInicio, LocalTime horaFin) {
        if (sectorId == null) {
            throw new IllegalArgumentException("El ID del sector es obligatorio");
        }
        if (diaSemana == null) {
            throw new IllegalArgumentException("El día de la semana es obligatorio");
        }
        if (horaInicio == null) {
            throw new IllegalArgumentException("La hora de inicio es obligatoria");
        }
        if (horaFin == null) {
            throw new IllegalArgumentException("La hora de fin es obligatoria");
        }
        if (!horaInicio.isBefore(horaFin)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }
    }
}