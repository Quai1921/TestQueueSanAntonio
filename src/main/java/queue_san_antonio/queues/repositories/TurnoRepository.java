package queue_san_antonio.queues.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import queue_san_antonio.queues.models.EstadoTurno;
import queue_san_antonio.queues.models.Turno;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    // Buscar por código - el más reciente si hay duplicados
    @Query(value = "SELECT * FROM turnos WHERE codigo = :codigo ORDER BY fecha_hora_generacion DESC LIMIT 1", nativeQuery = true)
    Optional<Turno> findByCodigo(@Param("codigo") String codigo);

    // Método adicional para buscar por código y fecha específica
    @Query("SELECT t FROM Turno t WHERE t.codigo = :codigo AND DATE(t.fechaHoraGeneracion) = :fecha")
    Optional<Turno> findByCodigoAndFecha(@Param("codigo") String codigo, @Param("fecha") LocalDate fecha);

    // Turnos activos de un sector (cola de espera) - INCLUIR REDIRIGIDO
    @Query("SELECT t FROM Turno t WHERE t.sector.id = :sectorId AND t.estado IN ('GENERADO', 'LLAMADO', 'EN_ATENCION', 'REDIRIGIDO') ORDER BY t.prioridad DESC, t.fechaHoraGeneracion ASC")
    List<Turno> findTurnosActivosBySector(@Param("sectorId") Long sectorId);

    // Próximo turno a llamar de un sector - INCLUIR REDIRIGIDO
    @Query("SELECT t FROM Turno t WHERE t.sector.id = :sectorId AND t.estado IN ('GENERADO', 'REDIRIGIDO') ORDER BY t.prioridad DESC, t.fechaHoraGeneracion ASC")
    List<Turno> findProximoTurnoSector(@Param("sectorId") Long sectorId);

    // Turnos del día por sector
    @Query("SELECT t FROM Turno t WHERE t.sector.id = :sectorId AND DATE(t.fechaHoraGeneracion) = :fecha ORDER BY t.fechaHoraGeneracion DESC")
    List<Turno> findTurnosDelDiaBySector(@Param("sectorId") Long sectorId, @Param("fecha") LocalDate fecha);

    // Último turno generado del día para generar código secuencial
//    @Query("SELECT t FROM Turno t WHERE t.sector.codigo = :codigoSector AND DATE(t.fechaHoraGeneracion) = :fecha ORDER BY t.fechaHoraGeneracion DESC")
//    List<Turno> findUltimoTurnoDelDia(@Param("codigoSector") String codigoSector, @Param("fecha") LocalDate fecha);
    @Query("SELECT t FROM Turno t WHERE t.sector.codigo = :codigoSector AND t.fechaHoraGeneracion BETWEEN :desde AND :hasta ORDER BY t.fechaHoraGeneracion DESC")
    List<Turno> findUltimoTurnoDelDia(@Param("codigoSector") String codigoSector,
                                      @Param("desde") LocalDateTime desde,
                                      @Param("hasta") LocalDateTime hasta);

    // Turnos en atención de un empleado
    List<Turno> findByEmpleadoAtencionIdAndEstado(Long empleadoId, EstadoTurno estado);

    // Turnos de un ciudadano
    List<Turno> findByCiudadanoIdOrderByFechaHoraGeneracionDesc(Long ciudadanoId);

    // Turnos pendientes de un ciudadano - INCLUIR REDIRIGIDO
    @Query("SELECT t FROM Turno t WHERE t.ciudadano.id = :ciudadanoId AND t.estado IN ('GENERADO', 'LLAMADO', 'EN_ATENCION', 'REDIRIGIDO')")
    List<Turno> findTurnosPendientesByCiudadano(@Param("ciudadanoId") Long ciudadanoId);

    // Para estadísticas - turnos del día
//    @Query("SELECT t FROM Turno t WHERE DATE(t.fechaHoraGeneracion) = :fecha")
//    List<Turno> findTurnosDelDia(@Param("fecha") LocalDate fecha);

    @Query("SELECT t FROM Turno t WHERE t.fechaHoraGeneracion BETWEEN :desde AND :hasta")
    List<Turno> findTurnosDelDia(@Param("desde") LocalDateTime desde,
                                 @Param("hasta") LocalDateTime hasta);

    // Para estadísticas - turnos entre fechas
    @Query("SELECT t FROM Turno t WHERE t.fechaHoraGeneracion BETWEEN :inicio AND :fin")
    List<Turno> findTurnosEntreFechas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Verificar si existe turno especial en fecha y hora específica
    @Query("SELECT COUNT(t) > 0 FROM Turno t WHERE t.sector.id = :sectorId AND t.fechaCita = :fechaCita AND t.horaCita = :horaCita AND t.estado NOT IN ('CANCELADO', 'AUSENTE')")
    boolean existeTurnoEspecial(@Param("sectorId") Long sectorId,
                                @Param("fechaCita") LocalDate fechaCita,
                                @Param("horaCita") LocalTime horaCita);

    // Contar turnos especiales en una fecha específica para un sector
    @Query("SELECT COUNT(t) FROM Turno t WHERE t.sector.id = :sectorId AND t.fechaCita = :fechaCita AND t.estado NOT IN ('CANCELADO', 'AUSENTE')")
    long contarTurnosEspecialesPorFecha(@Param("sectorId") Long sectorId,
                                        @Param("fechaCita") LocalDate fechaCita);


    // Cuenta cuántos turnos “ocupan cupo” por hora para un sector y fecha
    @Query("""
      select t.horaCita as hora, count(t) as cantidad
      from Turno t
      where t.sector.id = :sectorId
        and t.fechaCita = :fecha
        and t.estado not in (:excluir)
      group by t.horaCita
    """)
    List<Object[]> countByHoraForSectorAndFecha(
            @Param("sectorId") Long sectorId,
            @Param("fecha") LocalDate fecha,
            @Param("excluir") Collection<EstadoTurno> excluir
    );


}
