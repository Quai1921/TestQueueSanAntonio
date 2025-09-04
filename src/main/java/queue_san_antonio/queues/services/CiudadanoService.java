package queue_san_antonio.queues.services;

import queue_san_antonio.queues.models.Ciudadano;

import java.util.List;
import java.util.Optional;

public interface CiudadanoService {

    // Operaciones CRUD básicas
    Ciudadano guardar(Ciudadano ciudadano);
    Optional<Ciudadano> buscarPorId(Long id);
    Optional<Ciudadano> buscarPorDni(String dni);
    List<Ciudadano> listarTodos();

    // Búsquedas para formulario de turno
    List<Ciudadano> buscarPorDniOApellido(String dni, String apellido);
    List<Ciudadano> buscarPorApellido(String apellido);

    // Validaciones
    boolean existePorDni(String dni);

    // Operaciones de negocio
    Ciudadano crearOActualizar(String dni, String nombre, String apellido, String telefono, String direccion, String observaciones);

    void establecerPrioridad(Long ciudadanoId, boolean prioritario, String motivo);

}
