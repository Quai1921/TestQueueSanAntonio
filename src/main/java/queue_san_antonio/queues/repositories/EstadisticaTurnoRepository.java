package queue_san_antonio.queues.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import queue_san_antonio.queues.models.EstadisticaTurno;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstadisticaTurnoRepository extends JpaRepository<EstadisticaTurno, Long> {

    // Estadística específica de un sector en una fecha
    Optional<EstadisticaTurno> findByFechaAndSectorIdAndEmpleadoIsNull(LocalDate fecha, Long sectorId);

    // Estadística específica de un empleado en una fecha
    Optional<EstadisticaTurno> findByFechaAndSectorIdAndEmpleadoId(LocalDate fecha, Long sectorId, Long empleadoId);

    // Estadísticas de un sector entre fechas
    @Query("SELECT e FROM EstadisticaTurno e WHERE e.sector.id = :sectorId AND e.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY e.fecha DESC")
    List<EstadisticaTurno> findEstadisticasSectorEntreFechas(
            @Param("sectorId") Long sectorId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    // Estadísticas generales entre fechas (para reportes globales)
    @Query("SELECT e FROM EstadisticaTurno e WHERE e.empleado IS NULL AND e.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY e.fecha DESC, e.sector.nombre ASC")
    List<EstadisticaTurno> findEstadisticasGeneralesEntreFechas(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    // Estadísticas de un empleado entre fechas
    @Query("SELECT e FROM EstadisticaTurno e WHERE e.empleado.id = :empleadoId AND e.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY e.fecha DESC")
    List<EstadisticaTurno> findEstadisticasEmpleadoEntreFechas(
            @Param("empleadoId") Long empleadoId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);
}
