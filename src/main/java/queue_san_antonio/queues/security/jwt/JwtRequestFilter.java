package queue_san_antonio.queues.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    // Rutas que no requieren autenticación
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/pantalla", // Pantallas públicas de turnos
            "/api/turnos/consulta", // Consulta pública de turno por código
            "/h2-console", // Base de datos H2 en desarrollo
            "/actuator", // Endpoints de monitoreo
            "/swagger-ui", // Documentación API
            "/v3/api-docs" // OpenAPI
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Saltar validación para endpoints públicos
        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // Extraer token del header Authorization
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);

            try {
                username = jwtService.extractUsername(jwtToken);
                log.debug("Token JWT procesado para usuario: {}", username);

            } catch (ExpiredJwtException e) {
                log.warn("Token JWT expirado para usuario: {}", e.getClaims().getSubject());
                setErrorResponse(response, "Token expirado", HttpServletResponse.SC_UNAUTHORIZED);
                return;

            } catch (MalformedJwtException e) {
                log.error("Token JWT malformado: {}", e.getMessage());
                setErrorResponse(response, "Token inválido", HttpServletResponse.SC_UNAUTHORIZED);
                return;

            } catch (UnsupportedJwtException e) {
                log.error("Token JWT no soportado: {}", e.getMessage());
                setErrorResponse(response, "Token no soportado", HttpServletResponse.SC_UNAUTHORIZED);
                return;

            } catch (SecurityException e) {
                log.error("Error de seguridad en token JWT: {}", e.getMessage());
                setErrorResponse(response, "Token inválido", HttpServletResponse.SC_UNAUTHORIZED);
                return;

            } catch (IllegalArgumentException e) {
                log.error("Token JWT vacío o nulo: {}", e.getMessage());
                setErrorResponse(response, "Token requerido", HttpServletResponse.SC_UNAUTHORIZED);
                return;

            } catch (Exception e) {
                log.error("Error inesperado procesando token JWT: {}", e.getMessage());
                setErrorResponse(response, "Error de autenticación", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

        } else {
            log.debug("No se encontró token JWT en request a: {}", request.getRequestURI());
        }

        // Validar y establecer autenticación si el token es válido
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwtToken, userDetails)) {
                    // Crear authentication token
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());

                    // Establecer detalles de la request
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Establecer autenticación en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Autenticación establecida para usuario: {}", username);

                    // Agregar información del token a los headers de respuesta (opcional)
                    addTokenInfoHeaders(response, jwtToken);

                } else {
                    log.warn("Token JWT inválido para usuario: {}", username);
                }

            } catch (Exception e) {
                log.error("Error cargando detalles del usuario {}: {}", username, e.getMessage());
            }
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    //Verifica si el endpoint es público y no requiere autenticación
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        // Endpoints completamente públicos
        for (String publicPath : PUBLIC_ENDPOINTS) {
            if (requestPath.startsWith(publicPath)) {
                return true;
            }
        }

        // Endpoints públicos específicos por método HTTP
        if ("GET".equals(method)) {
            if (requestPath.matches("/api/turnos/codigo/[A-Z0-9]+")) {
                return true; // Consulta pública de turno por código
            }
            if (requestPath.startsWith("/api/sectores/publicos")) {
                return true; // Lista de sectores para mostrar en pantalla
            }
        }

        return false;
    }

    //Establece una respuesta de error en formato JSON
    private void setErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        String jsonResponse = String.format(
                "{\"error\": \"%s\", \"message\": \"%s\", \"timestamp\": \"%s\"}",
                status == 401 ? "Unauthorized" : "Bad Request",
                message,
                java.time.LocalDateTime.now().toString()
        );

        response.getWriter().write(jsonResponse);
    }

    //Agrega headers informativos sobre el token a la respuesta
    private void addTokenInfoHeaders(HttpServletResponse response, String token) {
        try {
            long remainingMinutes = jwtService.getTokenRemainingMinutes(token);
            response.setHeader("X-Token-Remaining-Minutes", String.valueOf(remainingMinutes));

            if (remainingMinutes < 30) { // Advertir si el token expira en menos de 30 minutos
                response.setHeader("X-Token-Warning", "Token expires soon");
            }

        } catch (Exception e) {
            // Ignorar errores al agregar headers informativos
            log.debug("Error agregando headers de token: {}", e.getMessage());
        }
    }

    //Override para mejorar logging en caso de errores del filtro
    @Override
    protected void doFilterNestedErrorDispatch(@NonNull HttpServletRequest request,
                                               @NonNull HttpServletResponse response,
                                               @NonNull FilterChain filterChain) throws ServletException, IOException {
        // En caso de errores, simplemente continuar sin procesamiento JWT
        filterChain.doFilter(request, response);
    }
}