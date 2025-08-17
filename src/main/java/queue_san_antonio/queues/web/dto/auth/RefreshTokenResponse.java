package queue_san_antonio.queues.web.dto.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefreshTokenResponse {

    //Nuevo token JWT de acceso
    @JsonProperty("accessToken")
    private String accessToken;

    //Nuevo refresh token (opcional, solo si se renovó)
    @JsonProperty("refreshToken")
    private String refreshToken;

    //Tipo de token (siempre "Bearer")
    @JsonProperty("tokenType")
    @Builder.Default
    private String tokenType = "Bearer";

    //Tiempo de vida del nuevo access token en segundos
    @JsonProperty("expiresIn")
    private Long expiresIn;

    //Timestamp de expiración del nuevo token
    @JsonProperty("expiresAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    //Indica si también se renovó el refresh token
    @JsonProperty("refreshTokenRenewed")
    @Builder.Default
    private Boolean refreshTokenRenewed = false;

    //Username del usuario (para confirmación)
    @JsonProperty("username")
    private String username;

    //Timestamp de la renovación
    @JsonProperty("renewedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime renewedAt = LocalDateTime.now();

    // ==========================================
    // MÉTODOS FACTORY
    // ==========================================

    //Crea una respuesta de refresh básica (solo access token)
    public static RefreshTokenResponse accessOnly(String accessToken, Long expiresIn, String username) {
        return RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .expiresIn(expiresIn)
                .expiresAt(LocalDateTime.now().plusSeconds(expiresIn))
                .username(username)
                .refreshTokenRenewed(false)
                .build();
    }

    //Crea una respuesta completa (access + refresh token)
    public static RefreshTokenResponse complete(String accessToken, String refreshToken,
                                                Long expiresIn, String username) {
        return RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .expiresAt(LocalDateTime.now().plusSeconds(expiresIn))
                .username(username)
                .refreshTokenRenewed(true)
                .build();
    }

    // ==========================================
    // MÉTODOS DE CONVENIENCIA
    // ==========================================

    //Obtiene el header Authorization completo
    public String getAuthorizationHeader() {
        return tokenType + " " + accessToken;
    }

    //Verifica si se renovó también el refresh token
    public boolean isRefreshTokenRenewed() {
        return refreshTokenRenewed != null && refreshTokenRenewed;
    }

    //Calcula los minutos restantes hasta la expiración
    public long getMinutesUntilExpiration() {
        if (expiresAt == null) return 0;
        LocalDateTime now = LocalDateTime.now();
        if (expiresAt.isBefore(now)) return 0;
        return java.time.Duration.between(now, expiresAt).toMinutes();
    }

    //Verifica si el token está próximo a expirar (menos de 5 minutos)
    public boolean isTokenExpiringSoon() {
        if (expiresAt == null) return false;
        return expiresAt.isBefore(LocalDateTime.now().plusMinutes(5));
    }
}