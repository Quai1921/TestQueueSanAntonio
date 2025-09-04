package queue_san_antonio.queues.web.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    //Token de refresh válido
    @NotBlank(message = "El refresh token es obligatorio")
    @JsonProperty("refreshToken")
    private String refreshToken;

    //Username del usuario (opcional, para validación adicional)
    @JsonProperty("username")
    private String username;

    //Información del dispositivo (opcional, para auditoría)
    @JsonProperty("deviceInfo")
    private String deviceInfo;

    //IP del cliente (se puede setear desde el controller)
    @JsonProperty("clientIp")
    private String clientIp;

    // ==========================================
    // MÉTODOS DE CONVENIENCIA
    // ==========================================

    //Limpia y normaliza el username si está presente
    public String getUsername() {
        return username != null ? username.trim().toLowerCase() : null;
    }

    //Establece el username normalizándolo
    public void setUsername(String username) {
        this.username = username != null ? username.trim().toLowerCase() : null;
    }

    //Verifica si se proporcionó username para validación
    public boolean hasUsername() {
        return username != null && !username.trim().isEmpty();
    }

    //Verifica si se proporcionó información del dispositivo
    public boolean hasDeviceInfo() {
        return deviceInfo != null && !deviceInfo.trim().isEmpty();
    }

    //Obtiene la información del dispositivo limpia
    public String getCleanDeviceInfo() {
        return deviceInfo != null ? deviceInfo.trim() : "Dispositivo desconocido";
    }

    //Valida que el refresh token esté presente
    public boolean isValid() {
        return refreshToken != null && !refreshToken.trim().isEmpty();
    }

    //Crea una representación segura para logs
    public String toSecureString() {
        return String.format("RefreshTokenRequest{username='%s', deviceInfo='%s', clientIp='%s', tokenPresent=%s}",
                username, getCleanDeviceInfo(), clientIp, refreshToken != null);
    }

    //Override toString para evitar logging del token
    @Override
    public String toString() {
        return toSecureString();
    }
}