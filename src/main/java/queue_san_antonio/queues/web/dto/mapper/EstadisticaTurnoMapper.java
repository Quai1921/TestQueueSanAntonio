package queue_san_antonio.queues.web.dto.mapper;

import queue_san_antonio.queues.models.EstadisticaTurno;
import queue_san_antonio.queues.web.dto.estadistica.EstadisticaSummaryResponse;
import queue_san_antonio.queues.web.dto.estadistica.EstadisticaTurnoResponse;
import queue_san_antonio.queues.web.dto.estadistica.ResumenEstadisticasResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//Mapper para conversiones entre EstadisticaTurno y DTOs
public class EstadisticaTurnoMapper {

    //Convierte EstadisticaTurno a EstadisticaTurnoResponse (información completa)
    public static EstadisticaTurnoResponse toResponse(EstadisticaTurno estadistica) {
        if (estadistica == null) return null;

        return EstadisticaTurnoResponse.builder()
                .id(estadistica.getId())
                .fecha(estadistica.getFecha())
                .sector(toSectorInfo(estadistica))
                .empleado(toEmpleadoInfo(estadistica))
                .turnosGenerados(estadistica.getTurnosGenerados())
                .turnosAtendidos(estadistica.getTurnosAtendidos())
                .turnosAusentes(estadistica.getTurnosAusentes())
                .turnosRedirigidos(estadistica.getTurnosRedirigidos())
                .turnosCancelados(estadistica.getTurnosCancelados())
                .totalTurnos(estadistica.getTotalTurnosProcesados())
                .porcentajeEficiencia(estadistica.getPorcentajeEficiencia().doubleValue())
                .porcentajeAusencias(estadistica.getPorcentajeAusencias().doubleValue())
                .tiempoPromedioEspera(estadistica.getTiempoPromedioEspera())
                .tiempoPromedioAtencion(estadistica.getTiempoPromedioAtencion())
                .tiempoTotalAtencion(estadistica.getTiempoTotalAtencion())
                .horaPico(estadistica.getHoraPico())
                .cantidadPico(estadistica.getCantidadPico())
                .tiempoMaximoEspera(estadistica.getTiempoMaximoEspera())
                .tiempoMinimoEspera(estadistica.getTiempoMinimoEspera())
                .fechaActualizacion(estadistica.getFechaActualizacion())
                .build();
    }

    //Convierte EstadisticaTurno a EstadisticaSummaryResponse (información resumida)
    public static EstadisticaSummaryResponse toSummaryResponse(EstadisticaTurno estadistica) {
        if (estadistica == null) return null;

        return EstadisticaSummaryResponse.builder()
                .fecha(estadistica.getFecha())
                .sectorCodigo(estadistica.getSector() != null ? estadistica.getSector().getCodigo() : null)
                .sectorNombre(estadistica.getSector() != null ? estadistica.getSector().getNombre() : null)
                .empleadoUsername(estadistica.getEmpleado() != null ? estadistica.getEmpleado().getUsername() : null)
                .empleadoNombre(estadistica.getEmpleado() != null ? estadistica.getEmpleado().getNombreCompleto() : null)
                .turnosGenerados(estadistica.getTurnosGenerados())
                .turnosAtendidos(estadistica.getTurnosAtendidos())
                .turnosAusentes(estadistica.getTurnosAusentes())
                .porcentajeEficiencia(estadistica.getPorcentajeEficiencia().doubleValue())
                .tiempoPromedioEspera(estadistica.getTiempoPromedioEspera())
                .horaPico(estadistica.getHoraPico())
                .cantidadPico(estadistica.getCantidadPico())
                .build();
    }

    //Convierte lista de EstadisticaTurno a lista de Response
    public static List<EstadisticaTurnoResponse> toResponseList(List<EstadisticaTurno> estadisticas) {
        if (estadisticas == null) return null;
        return estadisticas.stream()
                .map(EstadisticaTurnoMapper::toResponse)
                .toList();
    }

    //Convierte lista de EstadisticaTurno a lista de SummaryResponse
    public static List<EstadisticaSummaryResponse> toSummaryResponseList(List<EstadisticaTurno> estadisticas) {
        if (estadisticas == null) return null;
        return estadisticas.stream()
                .map(EstadisticaTurnoMapper::toSummaryResponse)
                .toList();
    }

    //Crea un resumen agregado de estadísticas para un período
    public static ResumenEstadisticasResponse toResumenResponse(List<EstadisticaTurno> estadisticas,
                                                                LocalDate fechaInicio,
                                                                LocalDate fechaFin) {
        if (estadisticas == null || estadisticas.isEmpty()) {
            return ResumenEstadisticasResponse.builder()
                    .fechaInicio(fechaInicio)
                    .fechaFin(fechaFin)
                    .totalDias(0)
                    .totalTurnosGenerados(0)
                    .totalTurnosAtendidos(0)
                    .totalTurnosAusentes(0)
                    .totalTurnosRedirigidos(0)
                    .promedioEficiencia(0.0)
                    .promedioTiempoEspera(0.0)
                    .promedioTiempoAtencion(0.0)
                    .topSectores(List.of())
                    .topEmpleados(List.of())
                    .build();
        }

        // Filtrar solo estadísticas generales de sector (sin empleado específico)
        List<EstadisticaTurno> estadisticasGenerales = estadisticas.stream()
                .filter(EstadisticaTurno::esEstadisticaGeneral)
                .toList();

        // Calcular totales
        int totalGenerados = estadisticasGenerales.stream().mapToInt(EstadisticaTurno::getTurnosGenerados).sum();
        int totalAtendidos = estadisticasGenerales.stream().mapToInt(EstadisticaTurno::getTurnosAtendidos).sum();
        int totalAusentes = estadisticasGenerales.stream().mapToInt(EstadisticaTurno::getTurnosAusentes).sum();
        int totalRedirigidos = estadisticasGenerales.stream().mapToInt(EstadisticaTurno::getTurnosRedirigidos).sum();

        // Calcular promedios
        double promedioEficiencia = estadisticasGenerales.stream()
                .mapToDouble(e -> e.getPorcentajeEficiencia().doubleValue())
                .average()
                .orElse(0.0);

        double promedioTiempoEspera = estadisticasGenerales.stream()
                .mapToInt(EstadisticaTurno::getTiempoPromedioEspera)
                .average()
                .orElse(0.0);

        double promedioTiempoAtencion = estadisticasGenerales.stream()
                .mapToInt(EstadisticaTurno::getTiempoPromedioAtencion)
                .average()
                .orElse(0.0);

        // Encontrar mejores sectores y empleados
        String sectorMasEficiente = estadisticasGenerales.stream()
                .max((e1, e2) -> e1.getPorcentajeEficiencia().compareTo(e2.getPorcentajeEficiencia()))
                .map(e -> e.getSector().getNombre())
                .orElse("N/A");

        String sectorMenosEficiente = estadisticasGenerales.stream()
                .min((e1, e2) -> e1.getPorcentajeEficiencia().compareTo(e2.getPorcentajeEficiencia()))
                .map(e -> e.getSector().getNombre())
                .orElse("N/A");

        // Top sectores por volumen
        List<ResumenEstadisticasResponse.RankingSector> topSectores = estadisticasGenerales.stream()
                .collect(Collectors.groupingBy(
                        EstadisticaTurno::getSector,
                        Collectors.summingInt(EstadisticaTurno::getTotalTurnosProcesados)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<queue_san_antonio.queues.models.Sector, Integer>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    var sector = entry.getKey();
                    var totalTurnos = entry.getValue();
                    var eficiencia = estadisticasGenerales.stream()
                            .filter(e -> e.getSector().equals(sector))
                            .mapToDouble(e -> e.getPorcentajeEficiencia().doubleValue())
                            .average()
                            .orElse(0.0);

                    return ResumenEstadisticasResponse.RankingSector.builder()
                            .codigo(sector.getCodigo())
                            .nombre(sector.getNombre())
                            .totalTurnos(totalTurnos)
                            .eficiencia(eficiencia)
                            .build();
                })
                .toList();

        // Agregar posiciones al ranking de sectores
        for (int i = 0; i < topSectores.size(); i++) {
            topSectores.get(i).setPosicion(i + 1);
        }

        // Top empleados (de estadísticas específicas de empleados)
        List<EstadisticaTurno> estadisticasEmpleados = estadisticas.stream()
                .filter(EstadisticaTurno::esEstadisticaEmpleado)
                .toList();

        List<ResumenEstadisticasResponse.RankingEmpleado> topEmpleados = estadisticasEmpleados.stream()
                .collect(Collectors.groupingBy(
                        EstadisticaTurno::getEmpleado,
                        Collectors.averagingDouble(e -> e.getPorcentajeEficiencia().doubleValue())
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<queue_san_antonio.queues.models.Empleado, Double>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    var empleado = entry.getKey();
                    var eficienciaPromedio = entry.getValue();
                    var totalTurnos = estadisticasEmpleados.stream()
                            .filter(e -> e.getEmpleado().equals(empleado))
                            .mapToInt(EstadisticaTurno::getTotalTurnosProcesados)
                            .sum();

                    return ResumenEstadisticasResponse.RankingEmpleado.builder()
                            .username(empleado.getUsername())
                            .nombreCompleto(empleado.getNombreCompleto())
                            .sectorCodigo(empleado.getSector() != null ? empleado.getSector().getCodigo() : "N/A")
                            .totalTurnos(totalTurnos)
                            .eficiencia(eficienciaPromedio)
                            .build();
                })
                .toList();

        // Agregar posiciones al ranking de empleados
        for (int i = 0; i < topEmpleados.size(); i++) {
            topEmpleados.get(i).setPosicion(i + 1);
        }

        // Encontrar empleado más productivo (por total de turnos)
        String empleadoMasProductivo = estadisticasEmpleados.stream()
                .collect(Collectors.groupingBy(
                        EstadisticaTurno::getEmpleado,
                        Collectors.summingInt(EstadisticaTurno::getTotalTurnosProcesados)
                ))
                .entrySet().stream()
                .max(Map.Entry.<queue_san_antonio.queues.models.Empleado, Integer>comparingByValue())
                .map(entry -> entry.getKey().getNombreCompleto())
                .orElse("N/A");

        // Calcular días únicos en el rango
        long diasUnicos = estadisticasGenerales.stream()
                .map(EstadisticaTurno::getFecha)
                .distinct()
                .count();

        return ResumenEstadisticasResponse.builder()
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .totalDias((int) diasUnicos)
                .totalTurnosGenerados(totalGenerados)
                .totalTurnosAtendidos(totalAtendidos)
                .totalTurnosAusentes(totalAusentes)
                .totalTurnosRedirigidos(totalRedirigidos)
                .promedioEficiencia(Math.round(promedioEficiencia * 100.0) / 100.0)
                .promedioTiempoEspera(Math.round(promedioTiempoEspera * 100.0) / 100.0)
                .promedioTiempoAtencion(Math.round(promedioTiempoAtencion * 100.0) / 100.0)
                .sectorMasEficiente(sectorMasEficiente)
                .sectorMenosEficiente(sectorMenosEficiente)
                .empleadoMasProductivo(empleadoMasProductivo)
                .topSectores(topSectores)
                .topEmpleados(topEmpleados)
                .build();
    }

    // ==========================================
    // MÉTODOS HELPER PRIVADOS
    // ==========================================

    //Convierte información del sector
    private static EstadisticaTurnoResponse.SectorInfo toSectorInfo(EstadisticaTurno estadistica) {
        if (estadistica.getSector() == null) return null;

        return EstadisticaTurnoResponse.SectorInfo.builder()
                .id(estadistica.getSector().getId())
                .codigo(estadistica.getSector().getCodigo())
                .nombre(estadistica.getSector().getNombre())
                .tipo(estadistica.getSector().getTipoSector().name())
                .build();
    }

    //Convierte información del empleado
    private static EstadisticaTurnoResponse.EmpleadoInfo toEmpleadoInfo(EstadisticaTurno estadistica) {
        if (estadistica.getEmpleado() == null) return null;

        return EstadisticaTurnoResponse.EmpleadoInfo.builder()
                .id(estadistica.getEmpleado().getId())
                .username(estadistica.getEmpleado().getUsername())
                .nombreCompleto(estadistica.getEmpleado().getNombreCompleto())
                .rol(estadistica.getEmpleado().getRol().name())
                .build();
    }
}