package queue_san_antonio.queues.web.dto.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import queue_san_antonio.queues.models.RolEmpleado;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    //Token JWT de acceso
    @JsonProperty("accessToken")
    private String accessToken;

    //Token de refresh para renovar sesión
    @JsonProperty("refreshToken")
    private String refreshToken;

    //Tipo de token (siempre "Bearer")
    @JsonProperty("tokenType")
    @Builder.Default
    private String tokenType = "Bearer";

    //Tiempo de vida del access token en segundos
    @JsonProperty("expiresIn")
    private Long expiresIn;

    //Timestamp de expiración del token
    @JsonProperty("expiresAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    //Información del usuario autenticado
    @JsonProperty("user")
    private UserInfo user;

    //Información del sector asignado
    @JsonProperty("sector")
    private SectorInfo sector;

    //Permisos del usuario
    @JsonProperty("permissions")
    private java.util.List<String> permissions;

    //Información anidada del usuario
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("username")
        private String username;

        @JsonProperty("nombreCompleto")
        private String nombreCompleto;

        @JsonProperty("nombre")
        private String nombre;

        @JsonProperty("apellido")
        private String apellido;

        @JsonProperty("email")
        private String email;

        @JsonProperty("dni")
        private String dni;

        @JsonProperty("rol")
        private RolEmpleado rol;

        @JsonProperty("ultimoAcceso")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime ultimoAcceso;
    }

    //Información anidada del sector
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SectorInfo {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("codigo")
        private String codigo;

        @JsonProperty("nombre")
        private String nombre;

        @JsonProperty("tipo")
        private String tipo;

        @JsonProperty("esResponsable")
        private Boolean esResponsable;
    }

    // ==========================================
    // MÉTODOS FACTORY
    // ==========================================

    //Crea una respuesta de login básica
    public static LoginResponse basic(String accessToken, String refreshToken, Long expiresIn) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .expiresAt(LocalDateTime.now().plusSeconds(expiresIn))
                .build();
    }

    //Crea una respuesta completa de login
    public static LoginResponse complete(String accessToken, String refreshToken, Long expiresIn,
                                         UserInfo user, SectorInfo sector, java.util.List<String> permissions) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .expiresAt(LocalDateTime.now().plusSeconds(expiresIn))
                .user(user)
                .sector(sector)
                .permissions(permissions)
                .build();
    }

    // ==========================================
    // MÉTODOS DE CONVENIENCIA
    // ==========================================

    //Obtiene el header Authorization completo
    public String getAuthorizationHeader() {
        return tokenType + " " + accessToken;
    }

    //Verifica si el token está próximo a expirar (menos de 5 minutos)
    public boolean isTokenExpiringSoon() {
        if (expiresAt == null) return false;
        return expiresAt.isBefore(LocalDateTime.now().plusMinutes(5));
    }

    //Calcula los minutos restantes hasta la expiración
    public long getMinutesUntilExpiration() {
        if (expiresAt == null) return 0;
        LocalDateTime now = LocalDateTime.now();
        if (expiresAt.isBefore(now)) return 0;
        return java.time.Duration.between(now, expiresAt).toMinutes();
    }

    //Verifica si el usuario es administrador
    public boolean isAdmin() {
        return user != null && user.getRol() == RolEmpleado.ADMIN;
    }

    //Verifica si el usuario es responsable de sector
    public boolean isResponsable() {
        return user != null && user.getRol() == RolEmpleado.RESPONSABLE_SECTOR;
    }

    //Verifica si el usuario es operador
    public boolean isOperador() {
        return user != null && user.getRol() == RolEmpleado.OPERADOR;
    }

    //Verifica si el usuario tiene un permiso específico
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    //Obtiene el nombre del rol en formato legible
    public String getRoleName() {
        if (user == null || user.getRol() == null) return "Sin rol";
        return switch (user.getRol()) {
            case ADMIN -> "Administrador";
            case RESPONSABLE_SECTOR -> "Responsable de Sector";
            case OPERADOR -> "Operador";
        };
    }

    //Verifica si el usuario tiene sector asignado
    public boolean hasSector() {
        return sector != null && sector.getId() != null;
    }
}