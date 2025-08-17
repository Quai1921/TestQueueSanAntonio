package queue_san_antonio.queues.services;

import queue_san_antonio.queues.models.Empleado;
import queue_san_antonio.queues.models.RolEmpleado;

import java.util.List;
import java.util.Optional;

public interface EmpleadoService {

    // Operaciones CRUD básicas
    Empleado guardar(Empleado empleado);
    Optional<Empleado> buscarPorId(Long id);
    Optional<Empleado> buscarPorUsername(String username);

    // Autenticación
    Optional<Empleado> autenticar(String username, String password);

    // Consultas específicas
    List<Empleado> listarPorSector(Long sectorId);
    List<Empleado> listarPorRol(RolEmpleado rol);

    // Validaciones
    boolean existePorUsername(String username);
    boolean existePorDni(String dni);

    // Operaciones de negocio
    Empleado crear(String username, String password, String nombre, String apellido,
                   RolEmpleado rol, Long sectorId);
    void cambiarPassword(Long empleadoId, String nuevaPassword);
    void asignarASector(Long empleadoId, Long sectorId);
    void activar(Long empleadoId);
    void desactivar(Long empleadoId);

    //Lista todos los empleados (para administradores)
    List<Empleado> listarTodos();
}
