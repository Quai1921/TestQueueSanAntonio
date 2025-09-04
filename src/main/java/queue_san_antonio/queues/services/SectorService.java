package queue_san_antonio.queues.services;

import queue_san_antonio.queues.models.Sector;
import queue_san_antonio.queues.models.TipoSector;

import java.util.List;
import java.util.Optional;

public interface SectorService {

    // Operaciones CRUD básicas
    Sector guardar(Sector sector);
    Optional<Sector> buscarPorId(Long id);
    Optional<Sector> buscarPorCodigo(String codigo);
    List<Sector> listarTodos();

    // Operaciones específicas
    List<Sector> listarActivosOrdenados();
    List<Sector> listarSectoresEspeciales();

    // Validaciones
    boolean existePorCodigo(String codigo);

    // Operaciones de negocio
    Sector crear(String codigo, String nombre, TipoSector tipo, boolean requiereCitaPrevia);
    void activar(Long sectorId);
    void desactivar(Long sectorId);
    void asignarResponsable(Long sectorId, Long empleadoId);

}
