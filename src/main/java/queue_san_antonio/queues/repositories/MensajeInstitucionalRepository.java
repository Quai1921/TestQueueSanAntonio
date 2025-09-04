package queue_san_antonio.queues.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import queue_san_antonio.queues.models.MensajeInstitucional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MensajeInstitucionalRepository extends JpaRepository<MensajeInstitucional, Long> {

    // Mensajes activos de una configuración ordenados por orden
    List<MensajeInstitucional> findByConfiguracionIdAndActivoTrueOrderByOrdenAsc(Long configuracionId);

    // Mensajes vigentes (que deben mostrarse ahora)
    @Query("SELECT m FROM MensajeInstitucional m WHERE m.activo = true AND " +
            "(m.fechaInicio IS NULL OR m.fechaInicio <= :ahora) AND " +
            "(m.fechaFin IS NULL OR m.fechaFin >= :ahora) " +
            "ORDER BY m.orden ASC")
    List<MensajeInstitucional> findMensajesVigentes(@Param("ahora") LocalDateTime ahora);

    List<MensajeInstitucional> findByConfiguracionIdOrderByOrdenAsc(Long configuracionId);
}
