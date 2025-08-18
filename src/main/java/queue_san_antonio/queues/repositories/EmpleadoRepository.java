package queue_san_antonio.queues.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import queue_san_antonio.queues.models.Empleado;
import queue_san_antonio.queues.models.RolEmpleado;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    // Para login
    Optional<Empleado> findByUsername(String username);

    // Empleados activos de un sector
    List<Empleado> findBySectorIdAndActivoTrue(Long sectorId);

    // Empleados por rol
    List<Empleado> findByRolAndActivoTrue(RolEmpleado rol);

    // Verificar si username ya existe
    boolean existsByUsername(String username);

    // Verificar si DNI ya existe
    boolean existsByDni(String dni);

    // Verificar si email ya existe
    boolean existsByEmail(String email);
}
