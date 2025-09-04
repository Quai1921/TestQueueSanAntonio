package queue_san_antonio.queues.web.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    //Código de error HTTP
    private int status;

    //Nombre del error HTTP (ej: "Bad Request", "Unauthorized")
    private String error;

    //Mensaje principal del error
    private String message;

    //Código de error específico del sistema
    private String code;

    //Path/endpoint donde ocurrió el error
    private String path;

    //Timestamp del error
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    //Detalles adicionales del error
    private String details;

    //Errores de validación específicos por campo
    private Map<String, String> fieldErrors;

    //Lista de errores múltiples
    private List<String> errors;

    //Información de debugging (solo desarrollo)
    private String trace;

    // ==========================================
    // MÉTODOS FACTORY PARA DIFERENTES TIPOS DE ERROR
    // ==========================================

    //Error básico con status y mensaje
    public static ErrorResponse basic(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

    //Error de validación con campos específicos
    public static ErrorResponse validation(String message, String path, Map<String, String> fieldErrors) {
        return ErrorResponse.builder()
                .status(400)
                .error("Bad Request")
                .message(message)
                .code("VALIDATION_ERROR")
                .path(path)
                .fieldErrors(fieldErrors)
                .build();
    }

    //Error de autenticación
    public static ErrorResponse unauthorized(String message, String path) {
        return ErrorResponse.builder()
                .status(401)
                .error("Unauthorized")
                .message(message)
                .code("AUTH_ERROR")
                .path(path)
                .build();
    }

    //Error de autorización/permisos
    public static ErrorResponse forbidden(String message, String path) {
        return ErrorResponse.builder()
                .status(403)
                .error("Forbidden")
                .message(message)
                .code("ACCESS_DENIED")
                .path(path)
                .build();
    }

    //Error de recurso no encontrado
    public static ErrorResponse notFound(String message, String path) {
        return ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message(message)
                .code("RESOURCE_NOT_FOUND")
                .path(path)
                .build();
    }

    //Error de conflicto (ej: DNI duplicado)
    public static ErrorResponse conflict(String message, String path, String details) {
        return ErrorResponse.builder()
                .status(409)
                .error("Conflict")
                .message(message)
                .code("BUSINESS_RULE_VIOLATION")
                .path(path)
                .details(details)
                .build();
    }

    //Error interno del servidor
    public static ErrorResponse internal(String message, String path) {
        return ErrorResponse.builder()
                .status(500)
                .error("Internal Server Error")
                .message(message)
                .code("INTERNAL_ERROR")
                .path(path)
                .build();
    }

    //Error interno con trace (desarrollo)
    public static ErrorResponse internal(String message, String path, String trace) {
        return ErrorResponse.builder()
                .status(500)
                .error("Internal Server Error")
                .message(message)
                .code("INTERNAL_ERROR")
                .path(path)
                .trace(trace)
                .build();
    }

    // ==========================================
    // MÉTODOS DE CONVENIENCIA
    // ==========================================

    //Agrega un error de campo específico
    public ErrorResponse addFieldError(String field, String error) {
        if (this.fieldErrors == null) {
            this.fieldErrors = new java.util.HashMap<>();
        }
        this.fieldErrors.put(field, error);
        return this;
    }

    //Agrega múltiples errores de campo
    public ErrorResponse addFieldErrors(Map<String, String> errors) {
        if (this.fieldErrors == null) {
            this.fieldErrors = new java.util.HashMap<>();
        }
        this.fieldErrors.putAll(errors);
        return this;
    }

    //Agrega un error a la lista de errores
    public ErrorResponse addError(String error) {
        if (this.errors == null) {
            this.errors = new java.util.ArrayList<>();
        }
        this.errors.add(error);
        return this;
    }

    //Verifica si hay errores de validación
    public boolean hasFieldErrors() {
        return fieldErrors != null && !fieldErrors.isEmpty();
    }

    //Verifica si hay múltiples errores
    public boolean hasMultipleErrors() {
        return errors != null && !errors.isEmpty();
    }
}