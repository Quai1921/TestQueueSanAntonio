package queue_san_antonio.queues.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import queue_san_antonio.queues.models.HistorialTurno;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistorialTurnoRepository extends JpaRepository<HistorialTurno, Long> {
    // Historial de un turno específico
    List<HistorialTurno> findByTurnoIdOrderByFechaHoraAsc(Long turnoId);

    // Historial de acciones de un empleado
    List<HistorialTurno> findByEmpleadoIdAndFechaHoraBetweenOrderByFechaHoraDesc(
            Long empleadoId, LocalDateTime inicio, LocalDateTime fin);

    // Últimas acciones del sistema (para auditoría)
    @Query("SELECT h FROM HistorialTurno h ORDER BY h.fechaHora DESC")
    List<HistorialTurno> findUltimasAcciones(org.springframework.data.domain.Pageable pageable);



}
