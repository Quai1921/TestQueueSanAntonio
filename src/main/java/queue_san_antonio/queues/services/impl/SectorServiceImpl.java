package queue_san_antonio.queues.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import queue_san_antonio.queues.models.Empleado;
import queue_san_antonio.queues.models.Sector;
import queue_san_antonio.queues.models.TipoSector;
import queue_san_antonio.queues.repositories.EmpleadoRepository;
import queue_san_antonio.queues.repositories.SectorRepository;
import queue_san_antonio.queues.services.SectorService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SectorServiceImpl implements SectorService {

    private final SectorRepository sectorRepository;
    private final EmpleadoRepository empleadoRepository;

    @Override
    public Sector guardar(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException("El sector no puede ser nulo");
        }


        log.debug("Guardando sector: {}", sector.getCodigo());

        // Validación de código duplicado para sectores nuevos
        if (sector.getId() == null && existePorCodigo(sector.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un sector con código: " + sector.getCodigo());
        }

        // Auto-asignar orden si es un sector nuevo
        if (sector.getId() == null && sector.getOrdenVisualizacion() == null) {
            sector.setOrdenVisualizacion(obtenerSiguienteOrdenVisualizacion());
        }

        return sectorRepository.save(sector);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sector> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return sectorRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sector> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return Optional.empty();
        }
        return sectorRepository.findByCodigo(codigo.trim().toUpperCase());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sector> listarTodos() {
        return sectorRepository.findAllWithEmpleados();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sector> listarActivosOrdenados() {
        return sectorRepository.findActivosOrdenados();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sector> listarSectoresEspeciales() {
        return sectorRepository.findSectoresEspeciales();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return false;
        }
        return sectorRepository.existsByCodigo(codigo.trim().toUpperCase());
    }

    @Override
    public Sector crear(String codigo, String nombre, TipoSector tipo, boolean requiereCitaPrevia) {
        // Validar parámetros obligatorios
        validarDatosObligatorios(codigo, nombre, tipo);

        // Limpiar y normalizar datos
        String codigoLimpio = codigo.trim().toUpperCase();
        String nombreLimpio = nombre.trim();

        log.info("Creando nuevo sector: {} - {}", codigoLimpio, nombreLimpio);

        // Verificar que no exista el código
        if (existePorCodigo(codigoLimpio)) {
            throw new IllegalArgumentException("Ya existe un sector con código: " + codigoLimpio);
        }

        Integer siguienteOrden = obtenerSiguienteOrdenVisualizacion();

        // Crear nuevo sector
        Sector nuevoSector = Sector.builder()
                .codigo(codigoLimpio)
                .nombre(nombreLimpio)
                .tipoSector(tipo)
                .requiereCitaPrevia(requiereCitaPrevia)
                .activo(true)
                .capacidadMaxima(1)
                .tiempoEstimadoAtencion(15)
                .build();

        log.debug("Creando sector: {} - Tipo: {} - Requiere cita: {}",
                codigoLimpio, tipo, requiereCitaPrevia);

        return guardar(nuevoSector);
    }

    @Override
    public void activar(Long sectorId) {
        if (sectorId == null) {
            throw new IllegalArgumentException("El ID del sector no puede ser nulo");
        }

        log.info("Activando sector ID: {}", sectorId);

        Sector sector = buscarPorId(sectorId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró sector con ID: " + sectorId));

        if (sector.estaActivo()) {
            log.debug("El sector {} ya estaba activo", sector.getCodigo());
            return;
        }

        sector.activar();
        guardar(sector);

        log.debug("Sector {} activado exitosamente", sector.getCodigo());

    }

    @Override
    public void desactivar(Long sectorId) {
        if (sectorId == null) {
            throw new IllegalArgumentException("El ID del sector no puede ser nulo");
        }

        log.info("Desactivando sector ID: {}", sectorId);

        Sector sector = buscarPorId(sectorId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró sector con ID: " + sectorId));

        // Validar que no tenga turnos pendientes
        if (sector.getTurnosPendientes() > 0) {
            throw new IllegalStateException("No se puede desactivar el sector " + sector.getCodigo() +
                    " porque tiene " + sector.getTurnosPendientes() + " turnos pendientes");
        }

        if (!sector.estaActivo()) {
            log.debug("El sector {} ya estaba inactivo", sector.getCodigo());
            return;
        }

        sector.desactivar();
        guardar(sector);

        log.debug("Sector {} desactivado exitosamente", sector.getCodigo());

    }

    @Override
    public void asignarResponsable(Long sectorId, Long empleadoId) {
        if (sectorId == null) {
            throw new IllegalArgumentException("El ID del sector no puede ser nulo");
        }
        if (empleadoId == null) {
            throw new IllegalArgumentException("El ID del empleado no puede ser nulo");
        }

        // Buscar sector y empleado
        Sector sector = buscarPorId(sectorId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró sector con ID: " + sectorId));

        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró empleado con ID: " + empleadoId));

        // Validar que el empleado esté activo
        if (!empleado.puedeAcceder()) {
            throw new IllegalStateException("El empleado " + empleado.getUsername() + " no está activo");
        }

        // Validar que el empleado tenga rol RESPONSABLE_SECTOR
        if (!empleado.esResponsable()) {
            throw new IllegalStateException("Solo se pueden asignar empleados con rol RESPONSABLE_SECTOR. " +
                    "El empleado " + empleado.getUsername() + " tiene rol: " + empleado.getRol());
        }

        if (sector.getResponsable() != null) {
            Empleado responsableAnterior = sector.getResponsable();
            log.info("Desasignando responsable anterior {} del sector {}",
                    responsableAnterior.getUsername(), sector.getCodigo());

            // Desasignar el empleado anterior del sector
            responsableAnterior.asignarASector(null);
            empleadoRepository.save(responsableAnterior);

            log.debug("Responsable anterior {} desasignado exitosamente", responsableAnterior.getUsername());
        }

        // Asignar responsable
        sector.establecerResponsable(empleado);
        guardar(sector);

        empleado.asignarASector(sector);
        empleadoRepository.save(empleado);

        log.info("Se asignó exitosamente a {} como responsable del sector {}",
                empleado.getUsername(), sector.getCodigo());

    }


//    Valida que todos los datos obligatorios estén presentes
    private void validarDatosObligatorios(String codigo, String nombre, TipoSector tipo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("Por favor ingrese el código del sector");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Por favor ingrese el nombre del sector");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Por favor seleccione el tipo de sector");
        }

        // Validar formato del código (2-10 letras mayúsculas)
        String codigoLimpio = codigo.trim().toUpperCase();
        if (!codigoLimpio.matches("^[A-Z]{2,10}$")) {
            throw new IllegalArgumentException("El código debe contener solo letras mayúsculas (2-10 caracteres)");
        }
    }

    // Auto-asignar orden de visualización
    private Integer obtenerSiguienteOrdenVisualizacion() {
        List<Sector> sectores = sectorRepository.findAll();
        if (sectores.isEmpty()) {
            return 1;
        }

        Integer maxOrden = sectores.stream()
                .mapToInt(s -> s.getOrdenVisualizacion() != null ? s.getOrdenVisualizacion() : 0)
                .max()
                .orElse(0);

        return maxOrden + 1;
    }
}
