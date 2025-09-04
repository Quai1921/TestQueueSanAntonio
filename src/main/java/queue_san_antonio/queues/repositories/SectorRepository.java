package queue_san_antonio.queues.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import queue_san_antonio.queues.models.Sector;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectorRepository extends JpaRepository<Sector, Long> {

    // Buscar por código
    Optional<Sector> findByCodigo(String codigo);

    // Sectores activos ordenados por orden de visualización
    @Query("SELECT s FROM Sector s WHERE s.activo = true ORDER BY s.ordenVisualizacion ASC, s.nombre ASC")
    List<Sector> findActivosOrdenados();

    // Sectores que requieren cita previa
    @Query("SELECT s FROM Sector s WHERE s.activo = true AND (s.requiereCitaPrevia = true OR s.tipoSector = 'ESPECIAL')")
    List<Sector> findSectoresEspeciales();

    // Verificar si código ya existe
    boolean existsByCodigo(String codigo);

    @Query("SELECT DISTINCT s FROM Sector s LEFT JOIN FETCH s.empleados e WHERE e.activo = true OR e IS NULL ORDER BY s.ordenVisualizacion ASC, s.nombre ASC")
    List<Sector> findAllWithEmpleados();

    @Query("SELECT s FROM Sector s WHERE s.responsable.id = :empleadoId")
    List<Sector> findByResponsableId(@Param("empleadoId") Long empleadoId);
}
