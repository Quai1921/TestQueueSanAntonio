package queue_san_antonio.queues.web.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    //Nombre de usuario del empleado
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
            message = "El username solo puede contener letras, números, puntos, guiones y guiones bajos")
    @JsonProperty("username")
    private String username;

    //Contraseña del empleado
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    @JsonProperty("password")
    private String password;

    //Indica si debe recordar la sesión (refresh token de larga duración)
    @JsonProperty("rememberMe")
    @Builder.Default
    private Boolean rememberMe = false;

    //Información del dispositivo/navegador (opcional, para auditoría)
    @JsonProperty("deviceInfo")
    @Size(max = 200, message = "La información del dispositivo no puede exceder 200 caracteres")
    private String deviceInfo;

    //IP del cliente (se puede setear desde el controller)
    @JsonProperty("clientIp")
    private String clientIp;

    // ==========================================
    // MÉTODOS DE CONVENIENCIA
    // ==========================================

    //Limpia y normaliza el username
    public String getUsername() {
        return username != null ? username.trim().toLowerCase() : null;
    }

    //Establece el username normalizándolo
    public void setUsername(String username) {
        this.username = username != null ? username.trim().toLowerCase() : null;
    }

    //Verifica si el usuario quiere ser recordado
    public boolean isRememberMe() {
        return rememberMe != null && rememberMe;
    }

    //Verifica si se proporcionó información del dispositivo
    public boolean hasDeviceInfo() {
        return deviceInfo != null && !deviceInfo.trim().isEmpty();
    }

    //Obtiene la información del dispositivo limpia
    public String getCleanDeviceInfo() {
        return deviceInfo != null ? deviceInfo.trim() : "Dispositivo desconocido";
    }

    //Valida que los campos obligatorios estén presentes
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
                password != null && !password.trim().isEmpty();
    }

    //Crea una representación segura para logs (sin password)
    public String toSecureString() {
        return String.format("LoginRequest{username='%s', rememberMe=%s, deviceInfo='%s', clientIp='%s'}",
                username, rememberMe, getCleanDeviceInfo(), clientIp);
    }

    //Override toString para evitar logging accidental de contraseñas
    @Override
    public String toString() {
        return toSecureString();
    }
}