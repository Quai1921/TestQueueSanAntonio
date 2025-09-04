package queue_san_antonio.queues.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        log.warn("Acceso no autorizado detectado. IP: {}, URI: {}, User-Agent: {}",
                getClientIpAddress(request),
                request.getRequestURI(),
                request.getHeader("User-Agent"));

        // Configurar respuesta HTTP
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");

        // Crear respuesta de error estructurada
        Map<String, Object> errorResponse = createErrorResponse(request, authException);

        // Escribir respuesta JSON
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    //Crea la respuesta de error estructurada
    private Map<String, Object> createErrorResponse(HttpServletRequest request,
                                                    AuthenticationException authException) {
        Map<String, Object> errorResponse = new HashMap<>();

        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", determineErrorMessage(request, authException));
        errorResponse.put("path", request.getRequestURI());

        // Información adicional para debugging (solo en desarrollo)
        if (isDevelopmentMode()) {
            errorResponse.put("details", authException.getMessage());
            errorResponse.put("type", authException.getClass().getSimpleName());
        }

        return errorResponse;
    }

    //Determina el mensaje de error apropiado basado en el contexto
    private String determineErrorMessage(HttpServletRequest request, AuthenticationException authException) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "Token de acceso requerido. Por favor, inicie sesión.";
        }

        if (authHeader.equals("Bearer ")) {
            return "Token de acceso vacío. Por favor, proporcione un token válido.";
        }

        // El token está presente pero es inválido
        String token = authHeader.substring(7);
        if (token.split("\\.").length != 3) {
            return "Formato de token inválido.";
        }

        // Otros errores de autenticación
        return "Token de acceso inválido o expirado. Por favor, inicie sesión nuevamente.";
    }

    //Obtiene la dirección IP real del cliente considerando proxies
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

    //Verifica si la aplicación está en modo desarrollo
    private boolean isDevelopmentMode() {
        String profile = System.getProperty("spring.profiles.active", "");
        return profile.contains("dev") || profile.contains("local") || profile.isEmpty();
    }
}