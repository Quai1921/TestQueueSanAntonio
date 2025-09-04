package queue_san_antonio.queues.web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import queue_san_antonio.queues.models.Empleado;
import queue_san_antonio.queues.security.jwt.JwtService;
import queue_san_antonio.queues.security.userdetails.EmpleadoUserDetailsService;
import queue_san_antonio.queues.services.EmpleadoService;
import queue_san_antonio.queues.web.dto.auth.*;
import queue_san_antonio.queues.web.dto.common.ApiResponseWrapper;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmpleadoService empleadoService;
    private final EmpleadoUserDetailsService userDetailsService;

    // Inyectar la configuración de expiración para mantener consistencia
    @Value("${jwt.expiration:86400000}") // 24 horas en milisegundos
    private long jwtExpiration;

    //Endpoint de login
    //POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponseWrapper<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        // Establecer información adicional de la request
        loginRequest.setClientIp(getClientIpAddress(request));
        if (!loginRequest.hasDeviceInfo()) {
            loginRequest.setDeviceInfo(getUserAgent(request));
        }

        log.info("Intento de login: {}", loginRequest.toSecureString());

        try {
            // Autenticar credenciales
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Obtener empleado desde la base de datos
            Optional<Empleado> empleadoOpt = empleadoService.buscarPorUsername(loginRequest.getUsername());
            if (empleadoOpt.isEmpty()) {
                log.error("Usuario autenticado pero no encontrado en BD: {}", loginRequest.getUsername());
                return ResponseEntity.badRequest()
                        .body(ApiResponseWrapper.error("Error interno de autenticación", "AUTH_ERROR"));
            }

            Empleado empleado = empleadoOpt.get();

            empleado.registrarAccesoExitoso();
            empleadoService.guardar(empleado);

            // Generar tokens JWT
            String accessToken = jwtService.generateToken(empleado);
            String refreshToken = jwtService.generateRefreshToken(empleado);

            // Calcular tiempo de expiración
            long expiresIn = jwtExpiration / 1000;

            // Crear información del usuario para la respuesta
            LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                    .id(empleado.getId())
                    .username(empleado.getUsername())
                    .nombreCompleto(empleado.getNombreCompleto())
                    .nombre(empleado.getNombre())
                    .apellido(empleado.getApellido())
                    .email(empleado.getEmail())
                    .rol(empleado.getRol())
                    .ultimoAcceso(empleado.getUltimoAcceso())
                    .build();

            // Crear información del sector (si tiene)
            LoginResponse.SectorInfo sectorInfo = null;
            if (empleado.getSector() != null) {
                sectorInfo = LoginResponse.SectorInfo.builder()
                        .id(empleado.getSector().getId())
                        .codigo(empleado.getSector().getCodigo())
                        .nombre(empleado.getSector().getNombre())
                        .tipo(empleado.getSector().getTipoSector().name())
                        .esResponsable(empleado.esResponsableDeSector(empleado.getSector().getId()))
                        .build();
            }

            // Obtener permisos del usuario
            List<String> permissions = userDetailsService.getUserPermissions(empleado.getUsername());

            // Crear respuesta completa
            LoginResponse loginResponse = LoginResponse.complete(
                    accessToken, refreshToken, expiresIn, userInfo, sectorInfo, permissions
            );

            log.info("Login exitoso para usuario: {} - Rol: {} - IP: {}",
                    empleado.getUsername(), empleado.getRol(), loginRequest.getClientIp());

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(loginResponse, "Autenticación exitosa")
            );

        } catch (BadCredentialsException e) {
            log.warn("Credenciales inválidas para usuario: {} - IP: {}",
                    loginRequest.getUsername(), loginRequest.getClientIp());
            return ResponseEntity.badRequest()
                    .body(ApiResponseWrapper.error("Usuario o contraseña incorrectos", "INVALID_CREDENTIALS"));

        } catch (DisabledException e) {
            log.warn("Intento de acceso con cuenta deshabilitada: {} - IP: {}",
                    loginRequest.getUsername(), loginRequest.getClientIp());
            return ResponseEntity.badRequest()
                    .body(ApiResponseWrapper.error("Cuenta de usuario deshabilitada", "ACCOUNT_DISABLED"));

        } catch (LockedException e) {
            log.warn("Intento de acceso con cuenta bloqueada: {} - IP: {}",
                    loginRequest.getUsername(), loginRequest.getClientIp());
            return ResponseEntity.badRequest()
                    .body(ApiResponseWrapper.error("Cuenta de usuario bloqueada", "ACCOUNT_LOCKED"));

        } catch (AuthenticationException e) {
            log.error("Error de autenticación para usuario: {} - Error: {}",
                    loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseWrapper.error("Error de autenticación", "AUTH_FAILED"));

        } catch (Exception e) {
            log.error("Error inesperado durante login de usuario: {} - Error: {}",
                    loginRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseWrapper.error("Error interno del servidor", "INTERNAL_ERROR"));
        }
    }

    //Endpoint de refresh token
    //POST /api/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseWrapper<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshRequest,
            HttpServletRequest request) {

        refreshRequest.setClientIp(getClientIpAddress(request));
        if (!refreshRequest.hasDeviceInfo()) {
            refreshRequest.setDeviceInfo(getUserAgent(request));
        }

        log.debug("Solicitud de refresh token: {}", refreshRequest.toSecureString());

        try {
            // Validar estructura del refresh token
            if (!jwtService.isTokenStructureValid(refreshRequest.getRefreshToken())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseWrapper.error("Refresh token inválido", "INVALID_REFRESH_TOKEN"));
            }

            // Extraer username del refresh token
            String username = jwtService.extractUsername(refreshRequest.getRefreshToken());

            // Validar que el username coincida si se proporcionó
            if (refreshRequest.hasUsername() && !username.equals(refreshRequest.getUsername())) {
                log.warn("Username del refresh token no coincide con el proporcionado: {} vs {}",
                        username, refreshRequest.getUsername());
                return ResponseEntity.badRequest()
                        .body(ApiResponseWrapper.error("Token inválido para el usuario", "TOKEN_USER_MISMATCH"));
            }

            // Cargar detalles del usuario
            var userDetails = userDetailsService.loadUserByUsername(username);

            // Validar el refresh token
            if (!jwtService.isTokenValid(refreshRequest.getRefreshToken(), userDetails)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseWrapper.error("Refresh token expirado o inválido", "INVALID_REFRESH_TOKEN"));
            }

            // Obtener empleado actualizado
            Optional<Empleado> empleadoOpt = empleadoService.buscarPorUsername(username);
            if (empleadoOpt.isEmpty() || !empleadoOpt.get().puedeAcceder()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseWrapper.error("Usuario no encontrado o inactivo", "USER_NOT_FOUND"));
            }

            Empleado empleado = empleadoOpt.get();

            // Generar nuevo access token
            String newAccessToken = jwtService.generateToken(empleado);
            long expiresIn = jwtExpiration / 1000; // 24 horas

            // Determinar si renovar también el refresh token
            long remainingMinutes = jwtService.getTokenRemainingMinutes(refreshRequest.getRefreshToken());
            boolean shouldRenewRefreshToken = remainingMinutes < (7 * 24 * 60) / 2; // Si queda menos de 3.5 días

            RefreshTokenResponse response;
            if (shouldRenewRefreshToken) {
                String newRefreshToken = jwtService.generateRefreshToken(empleado);
                response = RefreshTokenResponse.complete(newAccessToken, newRefreshToken, expiresIn, username);
                log.debug("Access token y refresh token renovados para usuario: {}", username);
            } else {
                response = RefreshTokenResponse.accessOnly(newAccessToken, expiresIn, username);
                log.debug("Solo access token renovado para usuario: {}", username);
            }

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(response, "Token renovado exitosamente")
            );

        } catch (Exception e) {
            log.error("Error renovando token: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseWrapper.error("Error renovando token", "REFRESH_FAILED"));
        }
    }

    //Endpoint de logout
    //POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseWrapper<String>> logout(HttpServletRequest request) {
        try {
            // Obtener usuario actual
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                log.info("Logout de usuario: {} - IP: {}",
                        authentication.getName(), getClientIpAddress(request));
            }

            // Limpiar contexto de seguridad
            SecurityContextHolder.clearContext();

            // En una implementación más avanzada, aquí se podría:
            // - Agregar el token a una blacklist
            // - Registrar el logout en auditoría
            // - Invalidar sesiones activas

            return ResponseEntity.ok(
                    ApiResponseWrapper.success("Sesión cerrada exitosamente")
            );

        } catch (Exception e) {
            log.error("Error durante logout: {}", e.getMessage());
            return ResponseEntity.ok(
                    ApiResponseWrapper.success("Sesión cerrada") // Siempre exitoso para logout
            );
        }
    }

    //Endpoint para obtener información del usuario actual
    //GET /api/auth/me
    @GetMapping("/me")
    public ResponseEntity<ApiResponseWrapper<LoginResponse.UserInfo>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseWrapper.error("No hay usuario autenticado", "NOT_AUTHENTICATED"));
            }

            String username = authentication.getName();
            Optional<Empleado> empleadoOpt = empleadoService.buscarPorUsername(username);

            if (empleadoOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseWrapper.error("Usuario no encontrado", "USER_NOT_FOUND"));
            }

            Empleado empleado = empleadoOpt.get();

            LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                    .id(empleado.getId())
                    .username(empleado.getUsername())
                    .nombreCompleto(empleado.getNombreCompleto())
                    .nombre(empleado.getNombre())
                    .apellido(empleado.getApellido())
                    .email(empleado.getEmail())
                    .dni(empleado.getDni())
                    .rol(empleado.getRol())
                    .ultimoAcceso(empleado.getUltimoAcceso())
                    .build();

            return ResponseEntity.ok(
                    ApiResponseWrapper.success(userInfo, "Información del usuario obtenida")
            );

        } catch (Exception e) {
            log.error("Error obteniendo información del usuario: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponseWrapper.error("Error interno", "INTERNAL_ERROR"));
        }
    }

    //Endpoint para validar token actual
    //GET /api/auth/validate
    @GetMapping("/validate")
    public ResponseEntity<ApiResponseWrapper<Object>> validateToken(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseWrapper.error("Token inválido", "INVALID_TOKEN"));
            }

            // Obtener información del token desde el header
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                long remainingMinutes = jwtService.getTokenRemainingMinutes(token);

                var tokenInfo = new java.util.HashMap<String, Object>();
                tokenInfo.put("valid", true);
                tokenInfo.put("username", authentication.getName());
                tokenInfo.put("remainingMinutes", remainingMinutes);
                tokenInfo.put("authorities", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).toList());

                return ResponseEntity.ok(
                        ApiResponseWrapper.success(tokenInfo, "Token válido")
                );
            }

            return ResponseEntity.ok(
                    ApiResponseWrapper.success("Token válido")
            );

        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseWrapper.error("Token inválido", "INVALID_TOKEN"));
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    //Obtiene la dirección IP real del cliente
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    //Obtiene el User-Agent del request
    private String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 200)) : "Unknown";
    }
}