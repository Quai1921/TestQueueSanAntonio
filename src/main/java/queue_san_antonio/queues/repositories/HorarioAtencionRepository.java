package queue_san_antonio.queues.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import queue_san_antonio.queues.models.HorarioAtencion;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface HorarioAtencionRepository extends JpaRepository<HorarioAtencion, Long> {

    // Horarios activos de un sector
    List<HorarioAtencion> findBySectorIdAndActivoTrueOrderByDiaSemanaAscHoraInicioAsc(Long sectorId);

    // Horarios de un día específico
    @Query("SELECT h FROM HorarioAtencion h WHERE h.sector.id = :sectorId AND h.diaSemana = :diaSemana AND h.activo = true ORDER BY h.horaInicio")
    List<HorarioAtencion> findHorariosPorDia(@Param("sectorId") Long sectorId, @Param("diaSemana") java.time.DayOfWeek diaSemana);

    // Horarios ACTIVOS de un sector para un día puntual
    @Query("""
      SELECT h
      FROM HorarioAtencion h
      WHERE h.sector.id = :sectorId
        AND h.activo = true
        AND h.diaSemana = :diaSemana
      ORDER BY h.horaInicio
    """)
    List<HorarioAtencion> findActivosBySectorAndDia(
            @Param("sectorId") Long sectorId,
            @Param("diaSemana") DayOfWeek diaSemana
    );

    // Agregar este método para obtener TODOS los horarios (activos e inactivos)
    List<HorarioAtencion> findBySectorIdOrderByDiaSemanaAscHoraInicioAsc(Long sectorId);

}
