package queue_san_antonio.queues.services.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import queue_san_antonio.queues.models.Empleado;
import queue_san_antonio.queues.models.EstadisticaTurno;
import queue_san_antonio.queues.models.Sector;
import queue_san_antonio.queues.repositories.EmpleadoRepository;
import queue_san_antonio.queues.repositories.EstadisticaTurnoRepository;
import queue_san_antonio.queues.repositories.SectorRepository;
import queue_san_antonio.queues.services.EstadisticaTurnoService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EstadisticaTurnoServiceImpl implements EstadisticaTurnoService {

    private final EstadisticaTurnoRepository estadisticaTurnoRepository;
    private final SectorRepository sectorRepository;
    private final EmpleadoRepository empleadoRepository;

    @Override
    public EstadisticaTurno guardar(EstadisticaTurno estadistica) {
        log.debug("Guardando estadística para sector: {} - fecha: {} - empleado: {}",
                estadistica.getSector() != null ? estadistica.getSector().getCodigo() : "null",
                estadistica.getFecha(),
                estadistica.getEmpleado() != null ? estadistica.getEmpleado().getUsername() : "General");

        return estadisticaTurnoRepository.save(estadistica);
    }

    @Override
    public EstadisticaTurno obtenerEstadisticaDelDia(Long sectorId, LocalDate fecha) {
        if (sectorId == null) {
            throw new IllegalArgumentException("El ID del sector no puede ser nulo");
        }
        if (fecha == null) {
            fecha = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));
        }

        log.debug("Obteniendo estadística del día {} para sector {}", fecha, sectorId);

        // Buscar estadística existente
        Optional<EstadisticaTurno> estadisticaOpt = estadisticaTurnoRepository
                .findByFechaAndSectorIdAndEmpleadoIsNull(fecha, sectorId);

        if (estadisticaOpt.isPresent()) {
            return estadisticaOpt.get();
        }

        // Crear nueva estadística si no existe
        Sector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró sector con ID: " + sectorId));

        EstadisticaTurno nuevaEstadistica = EstadisticaTurno.crearNueva(fecha, sector, null);

        log.debug("Creando nueva estadística diaria para sector {} - fecha {}",
                sector.getCodigo(), fecha);

        return guardar(nuevaEstadistica);
    }

    @Override
    public EstadisticaTurno obtenerEstadisticaEmpleado(Long empleadoId, Long sectorId, LocalDate fecha) {
        if (empleadoId == null) {
            throw new IllegalArgumentException("El ID del empleado no puede ser nulo");
        }
        if (sectorId == null) {
            throw new IllegalArgumentException("El ID del sector no puede ser nulo");
        }
        if (fecha == null) {
            fecha = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));
        }

        log.debug("Obteniendo estadística del empleado {} en sector {} para fecha {}",
                empleadoId, sectorId, fecha);

        // Buscar estadística existente
        Optional<EstadisticaTurno> estadisticaOpt = estadisticaTurnoRepository
                .findByFechaAndSectorIdAndEmpleadoId(fecha, sectorId, empleadoId);

        if (estadisticaOpt.isPresent()) {
            return estadisticaOpt.get();
        }

        // Crear nueva estadística si no existe
        Sector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró sector con ID: " + sectorId));

        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró empleado con ID: " + empleadoId));

        EstadisticaTurno nuevaEstadistica = EstadisticaTurno.crearNueva(fecha, sector, empleado);

        log.debug("Creando nueva estadística para empleado {} en sector {} - fecha {}",
                empleado.getUsername(), sector.getCodigo(), fecha);

        return guardar(nuevaEstadistica);
    }

    @Override
    public void actualizarTurnoGenerado(Long sectorId, Long empleadoId) {
        if (sectorId == null) {
            log.warn("Intento de actualizar estadística con sectorId nulo");
            return;
        }

        LocalDate hoy = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));

        try {
            // Actualizar estadística general del sector
            EstadisticaTurno estadisticaSector = obtenerEstadisticaDelDia(sectorId, hoy);
            estadisticaSector.incrementarGenerados();
            guardar(estadisticaSector);

            // Actualizar estadística del empleado si se proporciona
            if (empleadoId != null) {
                EstadisticaTurno estadisticaEmpleado = obtenerEstadisticaEmpleado(empleadoId, sectorId, hoy);
                estadisticaEmpleado.incrementarGenerados();
                guardar(estadisticaEmpleado);
            }

            log.trace("EST-OK: GENERADO sector={} empleado={}", sectorId, empleadoId);

        } catch (Exception e) {
            log.error("Error al actualizar estadística de turno generado - Sector: {} - Empleado: {}",
                    sectorId, empleadoId, e);
        }
    }

    @Override
    public void actualizarTurnoAtendido(Long sectorId, Long empleadoId, int tiempoEspera, int tiempoAtencion) {
        if (sectorId == null) {
            log.warn("Intento de actualizar atendido con sectorId nulo");
            return;
        }

        LocalDate hoy = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));

        try {
            // Actualizar estadística general del sector
            EstadisticaTurno estadisticaSector = obtenerEstadisticaDelDia(sectorId, hoy);
            estadisticaSector.incrementarAtendidos();
            if (tiempoEspera > 0)   estadisticaSector.actualizarTiempoEspera(tiempoEspera);
            if (tiempoAtencion > 0) estadisticaSector.actualizarTiempoAtencion(tiempoAtencion);

            guardar(estadisticaSector);

            if (empleadoId != null) {
                EstadisticaTurno estEmp = obtenerEstadisticaEmpleado(empleadoId, sectorId, hoy);
                estEmp.incrementarAtendidos();
                if (tiempoEspera > 0)   estEmp.actualizarTiempoEspera(tiempoEspera);
                if (tiempoAtencion > 0) estEmp.actualizarTiempoAtencion(tiempoAtencion);
                guardar(estEmp);
            }

            // Actualizar estadística del empleado
//            EstadisticaTurno estadisticaEmpleado = obtenerEstadisticaEmpleado(empleadoId, sectorId, hoy);
//            estadisticaEmpleado.incrementarAtendidos();
//            estadisticaEmpleado.actualizarTiempoEspera(tiempoEspera);
//            estadisticaEmpleado.actualizarTiempoAtencion(tiempoAtencion);
//            guardar(estadisticaEmpleado);

            log.trace("Estadística actualizada: turno atendido en sector {} por empleado {} - Espera: {}min, Atención: {}min",
                    sectorId, empleadoId, tiempoEspera, tiempoAtencion);

        } catch (Exception e) {
            log.error("Error al actualizar estadística de turno atendido - Sector: {} - Empleado: {} - Tiempos: {}/{}",
                    sectorId, empleadoId, tiempoEspera, tiempoAtencion, e);
        }
    }

    @Override
    public void actualizarTurnoAusente(Long sectorId, Long empleadoId) {
        if (sectorId == null) {
            log.warn("Intento de actualizar estadística de turno ausente con sectorId nulo");
            return;
        }

        LocalDate hoy = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));

        try {
            // Actualizar estadística general del sector
            EstadisticaTurno estadisticaSector = obtenerEstadisticaDelDia(sectorId, hoy);
            estadisticaSector.incrementarAusentes();
            guardar(estadisticaSector);

            // Actualizar estadística del empleado si se proporciona
            if (empleadoId != null) {
                EstadisticaTurno estadisticaEmpleado = obtenerEstadisticaEmpleado(empleadoId, sectorId, hoy);
                estadisticaEmpleado.incrementarAusentes();
                guardar(estadisticaEmpleado);
            }

            log.trace("Estadística actualizada: turno ausente en sector {} por empleado {}",
                    sectorId, empleadoId);

        } catch (Exception e) {
            log.error("Error al actualizar estadística de turno ausente - Sector: {} - Empleado: {}",
                    sectorId, empleadoId, e);
        }
    }

    @Override
    public void actualizarTurnoRedirigido(Long sectorId, Long empleadoId) {
        if (sectorId == null) {
            log.warn("Intento de actualizar estadística de turno redirigido con sectorId nulo");
            return;
        }

        LocalDate hoy = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));

        try {
            // Actualizar estadística general del sector ORIGEN
            EstadisticaTurno estadisticaSector = obtenerEstadisticaDelDia(sectorId, hoy);
            estadisticaSector.incrementarRedirigidos();
            guardar(estadisticaSector);

            // Actualizar estadística del empleado si se proporciona
            if (empleadoId != null) {
                EstadisticaTurno estadisticaEmpleado = obtenerEstadisticaEmpleado(empleadoId, sectorId, hoy);
                estadisticaEmpleado.incrementarRedirigidos();
                guardar(estadisticaEmpleado);
            }

            log.trace("Estadística actualizada: turno redirigido desde sector {} por empleado {}",
                    sectorId, empleadoId);

        } catch (Exception e) {
            log.error("Error al actualizar estadística de turno redirigido - Sector: {} - Empleado: {}",
                    sectorId, empleadoId, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadisticaTurno> obtenerEstadisticasSector(Long sectorId, LocalDate fechaInicio, LocalDate fechaFin) {
        if (sectorId == null) {
            return List.of();
        }

        // Valores por defecto para fechas
        if (fechaInicio == null) {
            fechaInicio = LocalDate.now(ZoneId.of("America/Argentina/Cordoba")).minusDays(30);
        }
        if (fechaFin == null) {
            fechaFin = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));
        }

        log.debug("Consultando estadísticas del sector {} entre {} y {}", sectorId, fechaInicio, fechaFin);

        return estadisticaTurnoRepository.findEstadisticasSectorEntreFechas(sectorId, fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadisticaTurno> obtenerEstadisticasGenerales(LocalDate fechaInicio, LocalDate fechaFin) {
        // Valores por defecto para fechas
        if (fechaInicio == null) {
            fechaInicio = LocalDate.now(ZoneId.of("America/Argentina/Cordoba")).minusDays(30);
        }
        if (fechaFin == null) {
            fechaFin = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));
        }

        log.debug("Consultando estadísticas generales entre {} y {}", fechaInicio, fechaFin);

        return estadisticaTurnoRepository.findEstadisticasGeneralesEntreFechas(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadisticaTurno> obtenerEstadisticasEmpleado(Long empleadoId, LocalDate fechaInicio, LocalDate fechaFin) {
        if (empleadoId == null) {
            return List.of();
        }

        // Valores por defecto para fechas
        if (fechaInicio == null) {
            fechaInicio = LocalDate.now(ZoneId.of("America/Argentina/Cordoba")).minusDays(30);
        }
        if (fechaFin == null) {
            fechaFin = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));
        }

        log.debug("Consultando estadísticas del empleado {} entre {} y {}", empleadoId, fechaInicio, fechaFin);

        return estadisticaTurnoRepository.findEstadisticasEmpleadoEntreFechas(empleadoId, fechaInicio, fechaFin);
    }

    @Override
    public void calcularHoraPico(Long sectorId, LocalDate fecha) {
        if (sectorId == null) {
            return;
        }
        if (fecha == null) {
            fecha = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));
        }

        log.debug("Calculando hora pico para sector {} en fecha {}", sectorId, fecha);

        try {
            // Esta funcionalidad requeriría análisis más complejo de los turnos generados por hora
            // Por simplicidad, se puede implementar con una hora fija de mayor demanda típica
            LocalTime horaPicoCalculada = LocalTime.of(10, 0); // Ejemplo: 10:00 AM
            int cantidadPico = 15; // Ejemplo: 15 turnos en esa hora

            EstadisticaTurno estadistica = obtenerEstadisticaDelDia(sectorId, fecha);
            estadistica.actualizarHoraPico(horaPicoCalculada, cantidadPico);
            guardar(estadistica);

            log.debug("Hora pico calculada para sector {}: {} con {} turnos", sectorId, horaPicoCalculada, cantidadPico);

        } catch (Exception e) {
            log.error("Error al calcular hora pico para sector {} en fecha {}", sectorId, fecha, e);
        }
    }

    @Override
    public void generarReporteDelDia(LocalDate fecha) {
        if (fecha == null) {
            fecha = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));
        }

        log.info("Generando reporte del día {}", fecha);

        try {
            // Obtener estadísticas generales del día
            List<EstadisticaTurno> estadisticasDelDia = obtenerEstadisticasGenerales(fecha, fecha);

            if (estadisticasDelDia.isEmpty()) {
                log.info("No hay estadísticas para generar reporte del día {}", fecha);
                return;
            }

            // Calcular totales
            int totalGenerados = estadisticasDelDia.stream().mapToInt(EstadisticaTurno::getTurnosGenerados).sum();
            int totalAtendidos = estadisticasDelDia.stream().mapToInt(EstadisticaTurno::getTurnosAtendidos).sum();
            int totalAusentes = estadisticasDelDia.stream().mapToInt(EstadisticaTurno::getTurnosAusentes).sum();
            int totalRedirigidos = estadisticasDelDia.stream().mapToInt(EstadisticaTurno::getTurnosRedirigidos).sum();

            // Log del reporte
            log.info("=== REPORTE DEL DÍA {} ===", fecha);
            log.info("Sectores activos: {}", estadisticasDelDia.size());
            log.info("Turnos generados: {}", totalGenerados);
            log.info("Turnos atendidos: {}", totalAtendidos);
            log.info("Turnos ausentes: {}", totalAusentes);
            log.info("Turnos redirigidos: {}", totalRedirigidos);

            // Detalles por sector
            estadisticasDelDia.forEach(est -> {
                log.info("Sector {}: Generados={}, Atendidos={}, Ausentes={}, Eficiencia={}%",
                        est.getSector().getCodigo(),
                        est.getTurnosGenerados(),
                        est.getTurnosAtendidos(),
                        est.getTurnosAusentes(),
                        est.getPorcentajeEficiencia());
            });

            log.info("=== FIN REPORTE ===");

        } catch (Exception e) {
            log.error("Error al generar reporte del día {}", fecha, e);
        }
    }

    //Método adicional para resetear estadísticas (útil para testing o correcciones)
    public void resetearEstadisticasDia(Long sectorId, LocalDate fecha) {
        if (sectorId == null || fecha == null) {
            return;
        }

        log.warn("Reseteando estadísticas del sector {} para fecha {}", sectorId, fecha);

        try {
            EstadisticaTurno estadistica = obtenerEstadisticaDelDia(sectorId, fecha);
            estadistica.resetear();
            guardar(estadistica);

            log.info("Estadísticas reseteadas para sector {} en fecha {}", sectorId, fecha);

        } catch (Exception e) {
            log.error("Error al resetear estadísticas - Sector: {} - Fecha: {}", sectorId, fecha, e);
        }
    }
}