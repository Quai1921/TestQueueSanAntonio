package queue_san_antonio.queues.services.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import queue_san_antonio.queues.models.Empleado;
import queue_san_antonio.queues.models.RolEmpleado;
import queue_san_antonio.queues.models.Sector;
import queue_san_antonio.queues.repositories.EmpleadoRepository;
import queue_san_antonio.queues.repositories.SectorRepository;
import queue_san_antonio.queues.services.EmpleadoService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final SectorRepository sectorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Empleado guardar(Empleado empleado) {
        log.debug("Guardando empleado: {}", empleado.getUsername());

        // Validación de username duplicado para empleados nuevos
        if (empleado.getId() == null && existePorUsername(empleado.getUsername())) {
            throw new IllegalArgumentException("Ya existe un empleado con username: " + empleado.getUsername());
        }

        // Validación de DNI duplicado si se proporciona
        if (empleado.getDni() != null && empleado.getId() == null && existePorDni(empleado.getDni())) {
            throw new IllegalArgumentException("Ya existe un empleado con DNI: " + empleado.getDni());
        }

        return empleadoRepository.save(empleado);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Empleado> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return empleadoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Empleado> buscarPorUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return empleadoRepository.findByUsername(username.trim().toLowerCase());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Empleado> autenticar(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.isEmpty()) {
            log.debug("Intento de autenticación con datos incompletos");
            return Optional.empty();
        }

        String usernameLimpio = username.trim().toLowerCase();
        log.debug("Intentando autenticar usuario: {}", usernameLimpio);

        Optional<Empleado> empleadoOpt = buscarPorUsername(usernameLimpio);

        if (empleadoOpt.isEmpty()) {
            log.debug("Usuario no encontrado: {}", usernameLimpio);
            return Optional.empty();
        }

        Empleado empleado = empleadoOpt.get();

        // Verificar que el empleado pueda acceder
        if (!empleado.puedeAcceder()) {
            log.warn("Intento de acceso de empleado inactivo: {}", usernameLimpio);
            return Optional.empty();
        }

        // Verificar contraseña
        if (!passwordEncoder.matches(password, empleado.getPassword())) {
            log.warn("Contraseña incorrecta para usuario: {}", usernameLimpio);
            return Optional.empty();
        }

        // Registrar acceso exitoso
        empleado.registrarAccesoExitoso();
        empleadoRepository.save(empleado);

        log.info("Autenticación exitosa para usuario: {}", usernameLimpio);
        return Optional.of(empleado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> listarPorSector(Long sectorId) {
        if (sectorId == null) {
            return List.of();
        }
        return empleadoRepository.findBySectorIdAndActivoTrue(sectorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> listarPorRol(RolEmpleado rol) {
        if (rol == null) {
            return List.of();
        }
        return empleadoRepository.findByRolAndActivoTrue(rol);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return empleadoRepository.existsByUsername(username.trim().toLowerCase());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return false;
        }
        return empleadoRepository.existsByDni(dni.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return empleadoRepository.existsByEmail(email.trim());
    }



    @Override
    public Empleado crear(String username, String password, String nombre, String apellido, String email, String dni, RolEmpleado rol, Long sectorId) {
        // Validar parámetros obligatorios
        validarDatosObligatorios(username, password, nombre, apellido, rol);

        // Limpiar y normalizar datos
        String usernameLimpio = username.trim().toLowerCase();
        String nombreLimpio = nombre.trim();
        String apellidoLimpio = apellido.trim();
        String emailLimpio = email != null ? email.trim() : null;
        String dniLimpio = dni != null ? dni.trim() : null;

        log.info("Creando nuevo empleado: {} - Rol: {}", usernameLimpio, rol);

        // Verificar que no exista el username
        if (existePorUsername(usernameLimpio)) {
            throw new IllegalArgumentException("Ya existe un empleado con username: " + usernameLimpio);
        }

        // Verificar DNI único si se proporciona
        if (dniLimpio != null && !dniLimpio.isEmpty() && existePorDni(dniLimpio)) {
            throw new IllegalArgumentException("Ya existe un empleado con DNI: " + dniLimpio);
        }

        // Verificar emailúnico si se proporciona
        if (emailLimpio != null && !emailLimpio.isEmpty() && existePorEmail(emailLimpio)) {
            throw new IllegalArgumentException("Ya existe un empleado con email: " + emailLimpio);
        }

        // Buscar sector si se proporciona
        Sector sector = null;
        if (sectorId != null) {
            sector = sectorRepository.findById(sectorId)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró sector con ID: " + sectorId));

            if (!sector.estaActivo()) {
                throw new IllegalStateException("No se puede asignar empleado a sector inactivo: " + sector.getCodigo());
            }
        }

        // Validar asignación de sector según rol
        validarAsignacionSector(rol, sector);

        // Crear nuevo empleado
        Empleado nuevoEmpleado = Empleado.builder()
                .username(usernameLimpio)
                .password(passwordEncoder.encode(password))
                .nombre(nombreLimpio)
                .apellido(apellidoLimpio)
                .email(emailLimpio)
                .dni(dniLimpio)
                .rol(rol)
                .sector(sector)
                .activo(true)
                .build();

        log.debug("Creando empleado: {} - Sector: {}", usernameLimpio,
                sector != null ? sector.getCodigo() : "Sin asignar");

        Empleado empleadoGuardado = guardar(nuevoEmpleado);

        // Si es responsable de sector y tiene sector asignado, asignarlo como responsable automáticamente
//        if (rol == RolEmpleado.RESPONSABLE_SECTOR && sector != null) {
//            log.info("Asignando automáticamente como responsable del sector {}", sector.getCodigo());
//            sector.establecerResponsable(empleadoGuardado);
//            sectorRepository.save(sector);
//        }
        if (rol == RolEmpleado.RESPONSABLE_SECTOR && sector != null) {
            log.info("Asignando automáticamente como responsable del sector {}", sector.getCodigo());

            // **NUEVA LÓGICA: Desasignar responsable anterior si existe**
            if (sector.getResponsable() != null) {
                Empleado responsableAnterior = sector.getResponsable();
                log.info("Desasignando responsable anterior {} del sector {}",
                        responsableAnterior.getUsername(), sector.getCodigo());

                // Desasignar el empleado anterior del sector
                responsableAnterior.asignarASector(null);
                empleadoRepository.save(responsableAnterior);

                log.debug("Responsable anterior {} desasignado exitosamente", responsableAnterior.getUsername());
            }

            // Asignar el nuevo responsable
            sector.establecerResponsable(empleadoGuardado);
            sectorRepository.save(sector);
        }

        return empleadoGuardado;
    }

    @Override
    @Transactional
    public Empleado actualizarEmpleado(Long empleadoId, String nombre, String apellido,
                                       String email, String dni, RolEmpleado nuevoRol, Long sectorId) {

        Empleado empleado = buscarPorId(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        RolEmpleado rolAnterior = empleado.getRol();

        // Actualizar datos básicos
        empleado.setNombre(nombre);
        empleado.setApellido(apellido);
        empleado.setEmail(email);
        empleado.setDni(dni);
        empleado.setRol(nuevoRol);

        // LÓGICA PARA CAMBIO DE ROL
        if (rolAnterior != nuevoRol) {
            manejarCambioDeRol(empleado, rolAnterior, nuevoRol);
        }

        // Asignar sector si se especifica
        if (sectorId != null) {
            asignarASector(empleado.getId(), sectorId);
        }

        return guardar(empleado);
    }


    @Override
    public void cambiarPassword(Long empleadoId, String nuevaPassword) {
        if (empleadoId == null) {
            throw new IllegalArgumentException("El ID del empleado no puede ser nulo");
        }
        if (nuevaPassword == null || nuevaPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía");
        }
        if (nuevaPassword.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }

        log.info("Cambiando contraseña para empleado ID: {}", empleadoId);

        Empleado empleado = buscarPorId(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró empleado con ID: " + empleadoId));

        empleado.cambiarPassword(passwordEncoder.encode(nuevaPassword));
        guardar(empleado);

        log.debug("Contraseña cambiada exitosamente para empleado: {}", empleado.getUsername());

    }

    @Override
    public void asignarASector(Long empleadoId, Long sectorId) {
        if (empleadoId == null) {
            throw new IllegalArgumentException("El ID del empleado no puede ser nulo");
        }

        log.info("Asignando empleado {} al sector {}", empleadoId, sectorId);

        Empleado empleado = buscarPorId(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró empleado con ID: " + empleadoId));

        Sector sector = null;
        if (sectorId != null) {
            sector = sectorRepository.findById(sectorId)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró sector con ID: " + sectorId));

            if (!sector.estaActivo()) {
                throw new IllegalStateException("No se puede asignar empleado a sector inactivo: " + sector.getCodigo());
            }
        }

        // Validar asignación según rol
        validarAsignacionSector(empleado.getRol(), sector);

        empleado.asignarASector(sector);
        guardar(empleado);

        log.debug("Empleado {} asignado al sector {}", empleado.getUsername(),
                sector != null ? sector.getCodigo() : "Sin asignar");

    }

    @Override
    public void activar(Long empleadoId) {
        if (empleadoId == null) {
            throw new IllegalArgumentException("El ID del empleado no puede ser nulo");
        }

        log.info("Activando empleado ID: {}", empleadoId);

        Empleado empleado = buscarPorId(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró empleado con ID: " + empleadoId));

        if (empleado.puedeAcceder()) {
            log.debug("El empleado {} ya estaba activo", empleado.getUsername());
            return;
        }

        empleado.activar();
        guardar(empleado);

        log.debug("Empleado {} activado exitosamente", empleado.getUsername());

    }

    @Override
    public void desactivar(Long empleadoId) {
        if (empleadoId == null) {
            throw new IllegalArgumentException("El ID del empleado no puede ser nulo");
        }

        log.info("Desactivando empleado ID: {}", empleadoId);

        Empleado empleado = buscarPorId(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró empleado con ID: " + empleadoId));

        if (!empleado.puedeAcceder()) {
            log.debug("El empleado {} ya estaba inactivo", empleado.getUsername());
            return;
        }

        empleado.desactivar();
        guardar(empleado);

        log.debug("Empleado {} desactivado exitosamente", empleado.getUsername());

    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> listarTodos() {
        return empleadoRepository.findAll();
    }









    @Override
    public Optional<Empleado> buscarResponsablePorSector(Long sectorId) {
        log.debug("Buscando responsable del sector ID: {}", sectorId);

        return empleadoRepository.findBySectorIdAndRol(sectorId, RolEmpleado.RESPONSABLE_SECTOR)
                .stream()
                .findFirst();
    }

    @Override
    public List<Empleado> buscarOperadoresPorSector(Long sectorId) {
        log.debug("Buscando operadores del sector ID: {}", sectorId);

        return empleadoRepository.findBySectorIdAndRolAndActivoTrue(sectorId, RolEmpleado.OPERADOR);
    }

    @Override
    public List<Empleado> buscarOperadoresSinSector() {
        log.debug("Buscando operadores sin sector asignado");

        return empleadoRepository.findByRolAndSectorIsNullAndActivoTrue(RolEmpleado.OPERADOR);
    }

    @Override
    public List<Empleado> buscarPorRol(RolEmpleado rol) {
        log.debug("Buscando empleados con rol: {}", rol);

        return empleadoRepository.findByRolAndActivoTrue(rol);
    }






















//    Valida que todos los datos obligatorios estén presentes
    private void validarDatosObligatorios(String username, String password, String nombre,
                                          String apellido, RolEmpleado rol) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Por favor ingrese el username");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Por favor ingrese la contraseña");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("POr favor ingrese el nombre");
        }
        if (apellido == null || apellido.trim().isEmpty()) {
            throw new IllegalArgumentException("Por favor ingrese el apellido");
        }
        if (rol == null) {
            throw new IllegalArgumentException("Por favor seleccione el rol");
        }

        // Validar formato username (3-50 caracteres, letras, números, puntos, guiones)
        String usernameLimpio = username.trim().toLowerCase();
        if (!usernameLimpio.matches("^[a-zA-Z0-9._-]{3,50}$")) {
            throw new IllegalArgumentException("El username debe tener 3-50 caracteres y solo puede contener letras, números, puntos, guiones y guiones bajos");
        }

        // Validar longitud mínima de contraseña
        if (password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
    }

    // Valida la asignación de sector según el rol del empleado
    private void validarAsignacionSector(RolEmpleado rol, Sector sector) {
        switch (rol) {
            case ADMIN -> {
                // Los administradores no necesitan sector asignado
                if (sector != null) {
                    log.debug("Asignando sector {} a administrador (opcional)", sector.getCodigo());
                }
            }
            case RESPONSABLE_SECTOR -> {
                // Los responsables pueden no tener sector inicialmente
                if (sector != null) {
                    log.debug("Asignando responsable al sector {}", sector.getCodigo());
                }
            }
            case OPERADOR -> {
                // Los operadores DEBEN tener un sector asignado
//                if (sector == null) {
//                    throw new IllegalArgumentException("Los operadores deben tener un sector asignado");
//                }
//                log.debug("Asignando operador al sector {}", sector.getCodigo());
                if (sector != null) {
                    log.debug("Asignando operador al sector {}", sector.getCodigo());
                } else {
                    log.debug("Desasignando operador - quedará disponible para reasignación");
                }
            }
        }
    }

    private void manejarCambioDeRol(Empleado empleado, RolEmpleado rolAnterior, RolEmpleado nuevoRol) {
        switch (nuevoRol) {
            case ADMIN -> {
                // Admin no necesita sector, desasignar cualquier sector
                if (empleado.getSector() != null) {
                    log.info("Desasignando sector al empleado {} que pasa a ADMIN", empleado.getUsername());
                    empleado.asignarASector(null);
                }
            }
            case RESPONSABLE_SECTOR -> {
                // Si era operador, desasignar sector actual para posterior reasignación como responsable
                if (rolAnterior == RolEmpleado.OPERADOR && empleado.getSector() != null) {
                    log.info("Desasignando sector al empleado {} que pasa de OPERADOR a RESPONSABLE_SECTOR",
                            empleado.getUsername());
                    empleado.asignarASector(null);
                }
            }
            case OPERADOR -> {
                // Si era responsable, remover de sectores donde es responsable
                if (rolAnterior == RolEmpleado.RESPONSABLE_SECTOR) {
                    log.info("Removiendo responsabilidad de sectores al empleado {} que pasa a OPERADOR",
                            empleado.getUsername());
                    // Buscar sectores donde es responsable y desasignarlo
                    List<Sector> sectoresResponsable = sectorRepository.findByResponsableId(empleado.getId());
                    for (Sector sector : sectoresResponsable) {
                        sector.establecerResponsable(null);
                        sectorRepository.save(sector);
                    }
                    // También desasignar de sector actual para posterior reasignación
                    empleado.asignarASector(null);
                }
            }
        }

    }

}
