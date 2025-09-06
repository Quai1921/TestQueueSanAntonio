package queue_san_antonio.queues.services.impl;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import queue_san_antonio.queues.models.*;
import queue_san_antonio.queues.repositories.CiudadanoRepository;
import queue_san_antonio.queues.repositories.EmpleadoRepository;
import queue_san_antonio.queues.repositories.SectorRepository;
import queue_san_antonio.queues.repositories.TurnoRepository;
import queue_san_antonio.queues.services.EstadisticaTurnoService;
import queue_san_antonio.queues.services.HistorialTurnoService;
import queue_san_antonio.queues.services.HorarioAtencionService;
import queue_san_antonio.queues.services.TurnoService;
import queue_san_antonio.queues.services.realtime.SseTurnosService;
import queue_san_antonio.queues.utils.DiaSemanaUtil;
import jakarta.persistence.criteria.*;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TurnoServiceImpl implements TurnoService {

    private final TurnoRepository turnoRepository;
    private final CiudadanoRepository ciudadanoRepository;
    private final SectorRepository sectorRepository;
    private final EmpleadoRepository empleadoRepository;
    private final HistorialTurnoService historialTurnoService;
    private final EstadisticaTurnoService estadisticaTurnoService;
    private final HorarioAtencionService horarioAtencionService;
    private final SseTurnosService sseTurnosService;

    @PersistenceContext
    private EntityManager entityManager;



    @Override
    public Turno guardar(Turno turno) {
        log.debug("Guardando turno: {}", turno.getCodigo());
        return turnoRepository.save(turno);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Turno> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return turnoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Turno> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return Optional.empty();
        }
        // Busca el más reciente si hay duplicados
        return turnoRepository.findByCodigo(codigo.trim().toUpperCase());
    }
    @Override
    @Transactional(readOnly = true)
    public Optional<Turno> buscarPorCodigoYFecha(String codigo, LocalDate fecha) {
        if (codigo == null || codigo.trim().isEmpty() || fecha == null) {
            return Optional.empty();
        }
        return turnoRepository.findByCodigoAndFecha(codigo.trim().toUpperCase(), fecha);
    }

    @Override
    @Transactional
    public Turno generarTurno(Long ciudadanoId, Long sectorId, TipoTurno tipo, Long empleadoId) {
        // Validar parámetros
        if (ciudadanoId == null) {
            throw new IllegalArgumentException("El ID del ciudadano no puede ser nulo");
        }
        if (sectorId == null) {
            throw new IllegalArgumentException("El ID del sector no puede ser nulo");
        }
        if (tipo == null) {
            tipo = TipoTurno.NORMAL;
        }

        Empleado empleado = null;
        if (empleadoId != null) {
            empleado = empleadoRepository.findById(empleadoId)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró empleado con ID: " + empleadoId));

            if (!empleado.puedeAcceder()) {
                throw new IllegalStateException("El empleado no está activo");
            }
        }

        log.info("Generando turno para ciudadano {} en sector {} - Tipo: {}",
                ciudadanoId, sectorId, tipo);

        // Buscar entidades
        Ciudadano ciudadano = ciudadanoRepository.findById(ciudadanoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ciudadano con ID: " + ciudadanoId));

        Sector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró sector con ID: " + sectorId));

        // Validaciones de negocio
        validarGeneracionTurno(ciudadano, sector, tipo);

        // Generar código único
        String codigo = generarCodigoTurno(sector.getCodigo(), LocalDate.now(ZoneId.of("America/Argentina/Cordoba")));

        // Determinar prioridad inicial
        int prioridad = determinarPrioridadInicial(ciudadano, tipo);

        // Crear turno
        Turno nuevoTurno = Turno.builder()
                .codigo(codigo)
                .ciudadano(ciudadano)
                .sector(sector)
                .estado(EstadoTurno.GENERADO)
                .tipo(tipo)
                .prioridad(prioridad)
                .build();

        // Guardar turno
        Turno turnoGuardado = guardar(nuevoTurno);

        // Registrar en historial
        historialTurnoService.registrarGeneracion(turnoGuardado, empleado);

        // Actualizar estadísticas
        estadisticaTurnoService.actualizarTurnoGenerado(sectorId, empleadoId);

        Long finalSectorId = turnoGuardado.getSector().getId();
        notifyAfterCommit(
                finalSectorId,
                "cola_actualizada",
                Map.of(
                        "tipo", "GENERAR_TURNO",
                        "turnoId", turnoGuardado.getId(),
                        "codigo", turnoGuardado.getCodigo()
                )
        );

        log.info("Turno generado exitosamente: {} para ciudadano {} en sector {}",
                codigo, ciudadano.getDni(), sector.getCodigo());

        return turnoGuardado;
    }

    @Override
    @Transactional
    public Turno generarTurnoEspecial(Long ciudadanoId, Long sectorId, LocalDate fechaCita, LocalTime horaCita, Long empleadoId) {
        if (fechaCita == null || horaCita == null) {
            throw new IllegalArgumentException("Fecha y hora de cita son obligatorias para turnos especiales");
        }

        log.info("Generando turno especial para ciudadano {} en sector {} - Cita: {} {}",
                ciudadanoId, sectorId, fechaCita, horaCita);

        validarTurnoEspecial(sectorId, fechaCita, horaCita);

        // Generar turno normal primero
        Turno turno = generarTurno(ciudadanoId, sectorId, TipoTurno.ESPECIAL, empleadoId);

        // Configurar como cita especial
        turno.configurarCitaEspecial(fechaCita, horaCita);

        Turno turnoActualizado = guardar(turno);

        Empleado empleado = null;
        if (empleadoId != null) {
            empleado = empleadoRepository.findById(empleadoId).orElse(null);
        }

        // Crear registro específico para la cita especial
        HistorialTurno registroCita = HistorialTurno.builder()
                .turno(turnoActualizado)
                .accion(AccionTurno.CAMBIO_ESTADO)
                .empleado(empleado)
                .observaciones(String.format("Configurado como cita especial para %s a las %s", fechaCita, horaCita))
                .build();
        historialTurnoService.guardar(registroCita);

        log.info("Turno especial generado exitosamente: {} para cita {} {}",
                turnoActualizado.getCodigo(), fechaCita, horaCita);

        Long finalSectorId = turnoActualizado.getSector().getId();
        notifyAfterCommit(
                finalSectorId,
                "cola_actualizada",
                Map.of(
                        "tipo", "GENERAR_TURNO_ESPECIAL",
                        "turnoId", turnoActualizado.getId(),
                        "codigo", turnoActualizado.getCodigo(),
                        "sectorId", finalSectorId,
                        "fechaCita", fechaCita.toString(),
                        "horaCita", horaCita.toString().substring(0,5)
                )
        );


        return turnoActualizado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Turno> obtenerColaEspera(Long sectorId) {
        if (sectorId == null) {
            return List.of();
        }
        return turnoRepository.findTurnosActivosBySector(sectorId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Turno> obtenerProximoTurno(Long sectorId) {
        if (sectorId == null) {
            return Optional.empty();
        }

        List<Turno> proximosTurnos = turnoRepository.findProximoTurnoSector(sectorId);
        return proximosTurnos.isEmpty() ? Optional.empty() : Optional.of(proximosTurnos.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public int contarTurnosPendientes(Long sectorId) {
        if (sectorId == null) {
            return 0;
        }

        List<Turno> turnosActivos = obtenerColaEspera(sectorId);
        return (int) turnosActivos.stream()
                .filter(turno -> turno.getEstado() == EstadoTurno.GENERADO)
                .count();
    }

    @Override
    @Transactional
    public Turno llamarTurno(Long turnoId, Long empleadoId, String observaciones) {
        if (turnoId == null) {
            throw new IllegalArgumentException("El ID del turno no puede ser nulo");
        }
        if (empleadoId == null) {
            throw new IllegalArgumentException("El ID del empleado no puede ser nulo");
        }

        log.info("Empleado {} llamando turno {}", empleadoId, turnoId);

        // Buscar entidades
        Turno turno = buscarPorId(turnoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró turno con ID: " + turnoId));

        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró empleado con ID: " + empleadoId));

        if (observaciones != null && !observaciones.isBlank()) {
            turno.setObservaciones(observaciones.trim());
        }

        // Validaciones
        validarLlamadoTurno(turno, empleado);

        // Llamar turno
        turno.llamar();
        Turno turnoActualizado = guardar(turno);

        // Registrar en historial
        historialTurnoService.registrarLlamado(turnoActualizado, empleado);


        Long sectorId = turnoActualizado.getSector().getId();
        notifyAfterCommit(
                sectorId,
                "cola_actualizada",
                Map.of(
                        "tipo", "LLAMAR_TURNO",
                        "turnoId", turnoActualizado.getId(),
                        "codigo", turnoActualizado.getCodigo()
                )
        );

        log.info("Turno {} llamado exitosamente por empleado {}",
                turno.getCodigo(), empleado.getUsername());

        return turnoActualizado;
    }

    @Override
    @Transactional
    public Turno iniciarAtencion(Long turnoId, Long empleadoId) {
        if (turnoId == null) {
            throw new IllegalArgumentException("El ID del turno no puede ser nulo");
        }
        if (empleadoId == null) {
            throw new IllegalArgumentException("El ID del empleado no puede ser nulo");
        }

        log.info("Empleado {} iniciando atención del turno {}", empleadoId, turnoId);

        // Buscar entidades
        Turno turno = buscarPorId(turnoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró turno con ID: " + turnoId));

        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró empleado con ID: " + empleadoId));

        // Validaciones
        validarInicioAtencion(turno, empleado);

        // Iniciar atención
        turno.iniciarAtencion(empleado);
        Turno turnoActualizado = guardar(turno);

        // Registrar en historial
        historialTurnoService.registrarInicioAtencion(turnoActualizado, empleado);

        Long sectorId = turnoActualizado.getSector().getId();
        notifyAfterCommit(
                sectorId,
                "cola_actualizada",
                Map.of(
                        "tipo", "INICIAR_ATENCION",
                        "turnoId", turnoActualizado.getId(),
                        "codigo", turnoActualizado.getCodigo()
                )
        );

        log.info("Atención iniciada para turno {} por empleado {}",
                turno.getCodigo(), empleado.getUsername());

        return turnoActualizado;
    }

    @Override
    @Transactional
    public Turno finalizarAtencion(Long turnoId, String observaciones) {
        if (turnoId == null) {
            throw new IllegalArgumentException("El ID del turno no puede ser nulo");
        }

        log.info("Finalizando atención del turno {}", turnoId);

        Turno turno = buscarPorId(turnoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró turno con ID: " + turnoId));

        // Validaciones
        if (turno.getEstado() != EstadoTurno.EN_ATENCION) {
            throw new IllegalStateException("Solo se pueden finalizar turnos en atención. Estado actual: " + turno.getEstado());
        }

        if (turno.getEmpleadoAtencion() == null) {
            throw new IllegalStateException("El turno no tiene empleado asignado");
        }

        // 1) Finalizar y persistir primero (esto setea finAtencion, etc.)
        turno.finalizarAtencion(observaciones);
        Turno turnoActualizado = guardar(turno);

        // 2) Recalcular tiempos DESPUÉS de finalizar
        int tiempoEspera   = Optional.ofNullable(turnoActualizado.getTiempoEsperaminutos())
                .map(Long::intValue).orElse(0);
        int tiempoAtencion = Optional.ofNullable(turnoActualizado.getTiempoAtencionMinutos())
                .map(Long::intValue).orElse(0);

        // 3) Historial
        historialTurnoService.registrarFinalizacion(
                turnoActualizado, turnoActualizado.getEmpleadoAtencion(), observaciones);

        // 4) Estadísticas: incrementar SIEMPRE 'atendidos' (los tiempos son opcionales)
        estadisticaTurnoService.actualizarTurnoAtendido(
                turnoActualizado.getSector().getId(),
                turnoActualizado.getEmpleadoAtencion().getId(),
                tiempoEspera,
                tiempoAtencion
        );

        // 5) Notificar SSE
        Long sectorId = turnoActualizado.getSector().getId();
        notifyAfterCommit(
                sectorId,
                "cola_actualizada",
                Map.of(
                        "tipo", "FINALIZAR_ATENCION",
                        "turnoId", turnoActualizado.getId(),
                        "codigo", turnoActualizado.getCodigo()
                )
        );

        log.info("Atención finalizada para turno {} - Tiempo espera: {}min, Tiempo atención: {}min",
                turnoActualizado.getCodigo(), tiempoEspera, tiempoAtencion);

        return turnoActualizado;
    }

    @Override
    @Transactional
    public Turno marcarAusente(Long turnoId, Long empleadoId, String observaciones) {
        if (turnoId == null) throw new IllegalArgumentException("El ID del turno no puede ser nulo");
        if (empleadoId == null) throw new IllegalArgumentException("El ID del empleado no puede ser nulo");

        log.info("Empleado {} marcando como ausente el turno {}", empleadoId, turnoId);

        Turno turno = buscarPorId(turnoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró turno con ID: " + turnoId));
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró empleado con ID: " + empleadoId));

        if (!turno.estaActivo()) {
            throw new IllegalStateException("Solo se pueden marcar como ausentes turnos activos");
        }

        // CAMBIO: guardar la observación en el turno
        if (observaciones != null && !observaciones.isBlank()) {
            turno.setObservaciones(observaciones.trim());
        }

        turno.marcarAusente();
        Turno turnoActualizado = guardar(turno);

        historialTurnoService.registrarAusente(turnoActualizado, empleado);
        estadisticaTurnoService.actualizarTurnoAusente(turno.getSector().getId(), empleado.getId());

        Long sectorId = turnoActualizado.getSector().getId();
        notifyAfterCommit(
                sectorId,
                "cola_actualizada",
                Map.of(
                        "tipo", "MARCAR_AUSENTE",
                        "turnoId", turnoActualizado.getId(),
                        "codigo", turnoActualizado.getCodigo()
                )
        );

        return turnoActualizado;
    }

    @Override
    @Transactional
    public Turno redirigirTurno(Long turnoId, Long nuevoSectorId, String motivo, String observaciones, Long empleadoId) {
        if (turnoId == null) {
            throw new IllegalArgumentException("El ID del turno no puede ser nulo");
        }
        if (nuevoSectorId == null) {
            throw new IllegalArgumentException("El ID del nuevo sector no puede ser nulo");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de redirección es obligatorio");
        }
        if (empleadoId == null) {
            throw new IllegalArgumentException("El ID del empleado no puede ser nulo");
        }

        log.info("Empleado {} redirigiendo turno {} al sector {} - Motivo: {}",
                empleadoId, turnoId, nuevoSectorId, motivo);

        // Buscar entidades
        Turno turno = buscarPorId(turnoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró turno con ID: " + turnoId));

        Sector nuevoSector = sectorRepository.findById(nuevoSectorId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró sector con ID: " + nuevoSectorId));

        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró empleado con ID: " + empleadoId));

        // Validaciones
        validarRedireccion(turno, nuevoSector, empleado);

        // Guardar sector original
        Sector sectorOriginal = turno.getSector();

        if (observaciones != null && !observaciones.isBlank()) {
            turno.setObservaciones(observaciones.trim());
        }

        // Redirigir turno
        turno.redirigirASector(nuevoSector, motivo.trim());
        Turno turnoActualizado = guardar(turno);

        // Registrar en historial
        historialTurnoService.registrarRedireccion(turnoActualizado, empleado, sectorOriginal, nuevoSector, motivo.trim());

        // Actualizar estadísticas
        estadisticaTurnoService.actualizarTurnoRedirigido(sectorOriginal.getId(), empleado.getId());

        log.info("Turno {} redirigido exitosamente de {} a {} por empleado {}",
                turno.getCodigo(), sectorOriginal.getCodigo(), nuevoSector.getCodigo(), empleado.getUsername());

        Long sectorId = turnoActualizado.getSector().getId();
        notifyAfterCommit(
                sectorId,
                "cola_actualizada",
                Map.of(
                        "tipo", "REDIRIGIR_TURNO",
                        "turnoId", turnoActualizado.getId(),
                        "codigo", turnoActualizado.getCodigo()
                )
        );

        return turnoActualizado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Turno> listarTurnosDelDia(Long sectorId, LocalDate fecha) {
        if (sectorId == null || fecha == null) {
            return List.of();
        }
        return turnoRepository.findTurnosDelDiaBySector(sectorId, fecha);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Turno> listarTurnosCiudadano(Long ciudadanoId) {
        if (ciudadanoId == null) {
            return List.of();
        }
        return turnoRepository.findByCiudadanoIdOrderByFechaHoraGeneracionDesc(ciudadanoId);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Turno> listarTurnosPendientesCiudadano(Long ciudadanoId) {
        if (ciudadanoId == null) {
            return List.of();
        }
        return turnoRepository.findTurnosPendientesByCiudadano(ciudadanoId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean ciudadanoTieneTurnoPendiente(Long ciudadanoId) {
        if (ciudadanoId == null) {
            return false;
        }
        return !listarTurnosPendientesCiudadano(ciudadanoId).isEmpty();
    }

    @Override
    public String generarCodigoTurno(String codigoSector, LocalDate fecha) {
        if (codigoSector == null || codigoSector.trim().isEmpty()) {
            throw new IllegalArgumentException("Por favor ingrese el sector");
        }
        if (fecha == null) {
            fecha = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));
        }

        String codigoSectorLimpio = codigoSector.trim().toUpperCase();

        LocalDateTime desde = fecha.atStartOfDay();
        LocalDateTime hasta = fecha.atTime(LocalTime.MAX);

        // Buscar último turno del día para este sector
        List<Turno> ultimosTurnos = turnoRepository.findUltimoTurnoDelDia(codigoSectorLimpio, desde, hasta);

        int siguienteNumero = 1;
        if (!ultimosTurnos.isEmpty()) {
            String ultimoCodigo = ultimosTurnos.get(0).getCodigo();
            // Extraer número del código (ej: "A015" -> 15)
            String numeroStr = ultimoCodigo.substring(codigoSectorLimpio.length());
            try {
                siguienteNumero = Integer.parseInt(numeroStr) + 1;
            } catch (NumberFormatException e) {
                log.warn("Error al parsear número del código: {}", ultimoCodigo);
                siguienteNumero = 1;
            }
        }

        // Formatear código (ej: "A001", "A015")
        String nuevoCodigo = String.format("%s%03d", codigoSectorLimpio, siguienteNumero);

        log.debug("Código generado para sector {} en fecha {}: {}", codigoSectorLimpio, fecha, nuevoCodigo);

        return nuevoCodigo;
    }


    @Override
    @Transactional(readOnly = true)
    public List<Turno> listarTurnosConFiltros(int limite, int offset, LocalDate fecha, Long sectorId) {
        log.debug("Listando turnos con filtros - límite: {}, offset: {}, fecha: {}, sectorId: {}",
                limite, offset, fecha, sectorId);

        // Validar parámetros
        if (limite <= 0 || limite > 500) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y 500");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("El offset no puede ser negativo");
        }

        try {
            // Convertir fecha a rango si se proporciona
            LocalDateTime fechaInicio = null;
            LocalDateTime fechaFin = null;

            if (fecha != null) {
                fechaInicio = fecha.atStartOfDay();
                fechaFin = fecha.atTime(LocalTime.MAX);
            }

            // Crear Pageable
            Pageable pageable = PageRequest.of(offset / limite, limite);

            // Usar repository
            Page<Turno> page = turnoRepository.findTurnosConFiltros(fechaInicio, fechaFin, sectorId, pageable);
            List<Turno> turnos = page.getContent();

            log.debug("Se encontraron {} turnos con los filtros aplicados", turnos.size());
            return turnos;

        } catch (Exception e) {
            log.error("Error listando turnos con filtros: {}", e.getMessage(), e);
            throw new RuntimeException("Error al listar turnos con filtros", e);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public long contarTurnosConFiltros(LocalDate fecha, Long sectorId) {
        log.debug("Contando turnos con filtros - fecha: {}, sectorId: {}", fecha, sectorId);

        try {
            // Convertir fecha a rango si se proporciona
            LocalDateTime fechaInicio = null;
            LocalDateTime fechaFin = null;

            if (fecha != null) {
                fechaInicio = fecha.atStartOfDay();
                fechaFin = fecha.atTime(LocalTime.MAX);
            }

            // Usar repository
            long count = turnoRepository.countTurnosConFiltros(fechaInicio, fechaFin, sectorId);

            log.debug("Total de turnos que cumplen los filtros: {}", count);
            return count;

        } catch (Exception e) {
            log.error("Error contando turnos con filtros: {}", e.getMessage(), e);
            throw new RuntimeException("Error al contar turnos con filtros", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Turno> listarTodos(int limite) {
        log.debug("Listando todos los turnos - límite: {}", limite);

        // Validar límite
        if (limite <= 0 || limite > 1000) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y 1000");
        }

        try {
            // Crear Pageable solo con el límite
            Pageable pageable = PageRequest.of(0, limite);

            // Usar repository
            List<Turno> turnos = turnoRepository.findTurnosRecientes(pageable);

            log.debug("Se listaron {} turnos", turnos.size());
            return turnos;

        } catch (Exception e) {
            log.error("Error listando todos los turnos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al listar todos los turnos", e);
        }
    }







    // Métodos de validación privados

    private void validarGeneracionTurno(Ciudadano ciudadano, Sector sector, TipoTurno tipo) {
        // Validar que el sector esté activo
        if (!sector.estaActivo()) {
            throw new IllegalStateException("El sector " + sector.getCodigo() + " está inactivo");
        }

        // Validar que no tenga turnos pendientes
//        if (ciudadanoTieneTurnoPendiente(ciudadano.getId())) {
//            throw new IllegalStateException("El ciudadano ya tiene un turno pendiente");
//        }

        // Validar sector especial
        if (sector.esEspecial() && tipo != TipoTurno.ESPECIAL) {
            throw new IllegalStateException("El sector " + sector.getCodigo() + " requiere turno especial con cita previa");
        }
    }

    private int determinarPrioridadInicial(Ciudadano ciudadano, TipoTurno tipo) {
        if (tipo == TipoTurno.URGENTE) {
            return 10; // Máxima prioridad
        }
        if (tipo == TipoTurno.REDIRIGIDO) {
            return 3; // Alta prioridad para redirigidos
        }
        if (ciudadano.tienePrioridad() || tipo == TipoTurno.PRIORITARIO) {
            return 2; // Prioridad por condición especial
        }
        return 0; // Prioridad normal
    }

    private void validarLlamadoTurno(Turno turno, Empleado empleado) {
        if (turno.getEstado() != EstadoTurno.GENERADO && turno.getEstado() != EstadoTurno.REDIRIGIDO) {
            throw new IllegalStateException("Solo se pueden llamar turnos en estado GENERADO o REDIRIGIDO. Estado actual: " + turno.getEstado());
        }

        if (!empleado.puedeAcceder()) {
            throw new IllegalStateException("El empleado no está activo");
        }

        // Validar que el empleado pueda atender este sector
        if (!empleado.esAdministrador() &&
                !empleado.perteneceASector(turno.getSector().getId()) &&
                !empleado.esResponsableDeSector(turno.getSector().getId())) {
            throw new IllegalStateException("El empleado no tiene permisos para atender turnos del sector " + turno.getSector().getCodigo());
        }
    }

    private void validarInicioAtencion(Turno turno, Empleado empleado) {
        if (turno.getEstado() != EstadoTurno.LLAMADO) {
            throw new IllegalStateException("Solo se puede iniciar atención de turnos llamados. Estado actual: " + turno.getEstado());
        }

        if (!empleado.puedeAcceder()) {
            throw new IllegalStateException("El empleado no está activo");
        }

        if (!empleado.esAdministrador() &&
                !empleado.perteneceASector(turno.getSector().getId()) &&
                !empleado.esResponsableDeSector(turno.getSector().getId())) {
            throw new IllegalStateException("El empleado no tiene permisos para atender turnos del sector " + turno.getSector().getCodigo());
        }
    }

    private void validarRedireccion(Turno turno, Sector nuevoSector, Empleado empleado) {
        if (!turno.estaActivo()) {
            throw new IllegalStateException("Solo se pueden redirigir turnos activos");
        }

        if (!nuevoSector.estaActivo()) {
            throw new IllegalStateException("No se puede redirigir a un sector inactivo: " + nuevoSector.getCodigo());
        }

        if (turno.getSector().getId().equals(nuevoSector.getId())) {
            throw new IllegalStateException("No se puede redirigir un turno al mismo sector");
        }

        if (!empleado.puedeAcceder()) {
            throw new IllegalStateException("El empleado no está activo");
        }
    }

    private void validarTurnoEspecial(Long sectorId, LocalDate fechaCita, LocalTime horaCita) {
        // 1. Validar que la fecha no sea anterior a hoy
        if (fechaCita.isBefore(LocalDate.now(ZoneId.of("America/Argentina/Cordoba")))) {
            throw new IllegalArgumentException("No se puede crear un turno especial para una fecha anterior a hoy");
        }

        // 2. Validar que la fecha y hora estén en los horarios configurados
        if (!horarioAtencionService.validarFechaHoraTurnoEspecial(sectorId, fechaCita, horaCita)) {
            DayOfWeek diaSemana = fechaCita.getDayOfWeek();
            String diaSemanaEspanol = DiaSemanaUtil.toEspanol(diaSemana);
            throw new IllegalArgumentException(
                    String.format("No hay atención disponible para %s a las %s en el día %s",
                            fechaCita, horaCita, diaSemanaEspanol)
            );
        }

        // 3. Validar que no exista otro turno en esa fecha y hora exacta
        if (turnoRepository.existeTurnoEspecial(sectorId, fechaCita, horaCita)) {
            throw new IllegalArgumentException(
                    String.format("Ya existe un turno especial para %s a las %s", fechaCita, horaCita)
            );
        }

        // 4. Validar capacidad máxima del día (opcional)
        // Obtener horarios del día para verificar capacidad
        DayOfWeek diaSemana = fechaCita.getDayOfWeek();
        List<HorarioAtencion> horariosDelDia = horarioAtencionService.listarPorDia(sectorId, diaSemana);

        if (!horariosDelDia.isEmpty()) {
            // Encontrar el horario que contiene la hora solicitada
            HorarioAtencion horarioAplicable = horariosDelDia.stream()
                    .filter(h -> h.estaActivo() && h.estaEnHorario(horaCita))
                    .findFirst()
                    .orElse(null);

            if (horarioAplicable != null) {
                // Verificar capacidad máxima para ese horario específico
                long turnosExistentesEnHora = turnoRepository.existeTurnoEspecial(sectorId, fechaCita, horaCita) ? 1 : 0;

                if (turnosExistentesEnHora >= horarioAplicable.getCapacidadMaxima()) {
                    throw new IllegalArgumentException(
                            String.format("No hay capacidad disponible para %s a las %s (máximo: %d turnos)",
                                    fechaCita, horaCita, horarioAplicable.getCapacidadMaxima())
                    );
                }
            }
        }
    }

    private void notifyAfterCommit(Long sectorId, String eventName, Map<String, Object> payload) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sseTurnosService.notifySector(sectorId, eventName, payload);
                }
            });
        } else {
            // fuera de TX (tests o llamados no transaccionales)
            sseTurnosService.notifySector(sectorId, eventName, payload);
        }
    }








}
