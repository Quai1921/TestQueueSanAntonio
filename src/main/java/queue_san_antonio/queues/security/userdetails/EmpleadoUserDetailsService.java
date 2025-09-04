package queue_san_antonio.queues.security.userdetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import queue_san_antonio.queues.models.Empleado;
import queue_san_antonio.queues.models.RolEmpleado;
import queue_san_antonio.queues.repositories.EmpleadoRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmpleadoUserDetailsService implements UserDetailsService {

    private final EmpleadoRepository empleadoRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Cargando detalles del usuario: {}", username);

        if (username == null || username.trim().isEmpty()) {
            throw new UsernameNotFoundException("Username no puede estar vacío");
        }

        String usernameLimpio = username.trim().toLowerCase();

        Empleado empleado = empleadoRepository.findByUsername(usernameLimpio)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado: {}", usernameLimpio);
                    return new UsernameNotFoundException("Usuario no encontrado: " + usernameLimpio);
                });

        log.debug("Usuario encontrado: {} - Rol: {} - Activo: {}",
                empleado.getUsername(), empleado.getRol(), empleado.puedeAcceder());

        return createUserDetails(empleado);
    }

    //Crea UserDetails a partir de un Empleado
    private UserDetails createUserDetails(Empleado empleado) {
        return User.builder()
                .username(empleado.getUsername())
                .password(empleado.getPassword())
                .authorities(getAuthorities(empleado))
                .accountExpired(false)
                .accountLocked(!empleado.puedeAcceder())
                .credentialsExpired(false)
                .disabled(!empleado.puedeAcceder())
                .build();
    }

    //Determina las autoridades (roles y permisos) del empleado
    private Collection<? extends GrantedAuthority> getAuthorities(Empleado empleado) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Rol principal
        authorities.add(new SimpleGrantedAuthority("ROLE_" + empleado.getRol().name()));

        // Permisos específicos basados en el rol
        authorities.addAll(getRoleSpecificAuthorities(empleado.getRol()));

        // Permisos adicionales basados en responsabilidades
        if (empleado.esResponsable() && empleado.getSectoresResponsable() != null &&
                !empleado.getSectoresResponsable().isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_SECTOR"));
        }

        log.debug("Autoridades asignadas a {}: {}", empleado.getUsername(), authorities);

        return authorities;
    }

    //Obtiene permisos específicos según el rol del empleado
    private List<GrantedAuthority> getRoleSpecificAuthorities(RolEmpleado rol) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        switch (rol) {
            case ADMIN -> {
                // Administradores tienen todos los permisos
                authorities.add(new SimpleGrantedAuthority("PERMISSION_ADMIN_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_EMPLOYEES"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_SECTORS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_VIEW_STATISTICS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_CONFIGURATION"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_MESSAGES"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_SCHEDULES"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_ATTEND_TURNS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_REDIRECT_TURNS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_VIEW_AUDIT"));
            }

            case RESPONSABLE_SECTOR -> {
                // Responsables pueden gestionar su sector y ver estadísticas
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_SECTOR"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_VIEW_STATISTICS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_SCHEDULES"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_ATTEND_TURNS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_REDIRECT_TURNS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_VIEW_SECTOR_AUDIT"));
            }

            case OPERADOR -> {
                // Operadores solo pueden atender turnos y ver información básica
                authorities.add(new SimpleGrantedAuthority("PERMISSION_ATTEND_TURNS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_VIEW_QUEUE"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MARK_ABSENT"));
            }
        }

        return authorities;
    }

    //Método auxiliar para verificar si un empleado tiene un permiso específico
    //Útil para validaciones adicionales en servicios
    public boolean hasPermission(String username, String permission) {
        try {
            UserDetails userDetails = loadUserByUsername(username);
            return userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals(permission));
        } catch (Exception e) {
            log.error("Error verificando permiso {} para usuario {}: {}", permission, username, e.getMessage());
            return false;
        }
    }

    //Método auxiliar para obtener los permisos de un usuario
    //Útil para depuración y logging
    public List<String> getUserPermissions(String username) {
        try {
            UserDetails userDetails = loadUserByUsername(username);
            return userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
        } catch (Exception e) {
            log.error("Error obteniendo permisos para usuario {}: {}", username, e.getMessage());
            return List.of();
        }
    }

    //Refresca los datos del empleado en caso de cambios
    //Útil después de actualizaciones de rol o sector
    @Transactional(readOnly = true)
    public UserDetails refreshUserDetails(String username) {
        log.debug("Refrescando detalles del usuario: {}", username);
        return loadUserByUsername(username);
    }

    //Verifica si un empleado puede acceder a un sector específico
    public boolean canAccessSector(String username, Long sectorId) {
        try {
            Empleado empleado = empleadoRepository.findByUsername(username.trim().toLowerCase())
                    .orElse(null);

            if (empleado == null || !empleado.puedeAcceder()) {
                return false;
            }

            // Los administradores pueden acceder a cualquier sector
            if (empleado.esAdministrador()) {
                return true;
            }

            // Los responsables pueden acceder a sectores que administran
            if (empleado.esResponsable() && empleado.esResponsableDeSector(sectorId)) {
                return true;
            }

            // Los empleados pueden acceder a su sector asignado
            return empleado.perteneceASector(sectorId);

        } catch (Exception e) {
            log.error("Error verificando acceso al sector {} para usuario {}: {}",
                    sectorId, username, e.getMessage());
            return false;
        }
    }
}