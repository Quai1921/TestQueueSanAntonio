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
    boolean existePorEmail(String email);

    // Operaciones de negocio
    Empleado crear(String username, String password, String nombre, String apellido, String email, String dni, RolEmpleado rol, Long sectorId);
    Empleado actualizarEmpleado(Long empleadoId, String nombre, String apellido, String email, String dni, RolEmpleado nuevoRol, Long sectorId);
    void cambiarPassword(Long empleadoId, String nuevaPassword);
    void asignarASector(Long empleadoId, Long sectorId);
    void activar(Long empleadoId);
    void desactivar(Long empleadoId);

    //Lista todos los empleados (para administradores)
    List<Empleado> listarTodos();





    //Busca el responsable asignado a un sector específico
    Optional<Empleado> buscarResponsablePorSector(Long sectorId);

    //Busca todos los operadores asignados a un sector específico
    List<Empleado> buscarOperadoresPorSector(Long sectorId);

    //Busca operadores que no están asignados a ningún sector
    List<Empleado> buscarOperadoresSinSector();

    //Busca empleados por rol específico
    List<Empleado> buscarPorRol(RolEmpleado rol);




}
