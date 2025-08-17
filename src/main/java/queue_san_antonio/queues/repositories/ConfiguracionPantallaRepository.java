package queue_san_antonio.queues.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import queue_san_antonio.queues.models.ConfiguracionPantalla;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfiguracionPantallaRepository extends JpaRepository<ConfiguracionPantalla, Long> {

    // Configuración activa (normalmente solo hay una)
    Optional<ConfiguracionPantalla> findByActivoTrue();

    // Todas las configuraciones ordenadas por nombre
    List<ConfiguracionPantalla> findAllByOrderByNombreAsc();
}
