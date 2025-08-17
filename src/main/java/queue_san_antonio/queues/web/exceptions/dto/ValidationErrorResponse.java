package queue_san_antonio.queues.web.exceptions.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Respuesta específica para errores de validación
//Proporciona detalles estructurados sobre errores de validación de campos
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorResponse {

    //Código de estado HTTP
    private int status;

    //Mensaje principal del error
    private String message;

    //Código de error específico
    private String errorCode;

    //Path del endpoint donde ocurrió el error
    private String path;

    //Timestamp del error
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    //Errores específicos por campo
    @Builder.Default
    private Map<String, String> fieldErrors = new HashMap<>();

    //Lista de errores generales
    private List<String> globalErrors;

    //Número total de errores
    private int errorCount;

    //Objeto que causó la validación fallida (sin datos sensibles)
    private Object rejectedValue;

    // ==========================================
    // MÉTODOS FACTORY
    // ==========================================

    //Crea respuesta para errores de Bean Validation
    public static ValidationErrorResponse fromBindingResult(
            org.springframework.validation.BindingResult bindingResult,
            String path) {

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .status(400)
                .message("Error de validación en los datos enviados")
                .errorCode("VALIDATION_ERROR")
                .path(path)
                .build();

        // Agregar errores de campo
        bindingResult.getFieldErrors().forEach(error ->
                response.addFieldError(error.getField(), error.getDefaultMessage())
        );

        // Agregar errores globales
        bindingResult.getGlobalErrors().forEach(error ->
                response.addGlobalError(error.getDefaultMessage())
        );

        response.setErrorCount(response.fieldErrors.size() +
                (response.globalErrors != null ? response.globalErrors.size() : 0));

        return response;
    }

    //Crea respuesta para ValidationException personalizada
    public static ValidationErrorResponse fromValidationException(
            queue_san_antonio.queues.web.exceptions.custom.ValidationException ex,
            String path) {

        return ValidationErrorResponse.builder()
                .status(400)
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .path(path)
                .fieldErrors(new HashMap<>(ex.getFieldErrors()))
                .errorCount(ex.getFieldErrors().size())
                .rejectedValue(sanitizeRejectedValue(ex.getRejectedValue()))
                .build();
    }

    //Crea respuesta para error de campo único
    public static ValidationErrorResponse singleFieldError(
            String field, String message, String path) {

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .status(400)
                .message("Error de validación")
                .errorCode("VALIDATION_ERROR")
                .path(path)
                .errorCount(1)
                .build();

        response.addFieldError(field, message);
        return response;
    }

    //Crea respuesta para múltiples errores de campo
    public static ValidationErrorResponse multipleFieldErrors(
            Map<String, String> fieldErrors, String path) {

        return ValidationErrorResponse.builder()
                .status(400)
                .message("Múltiples errores de validación")
                .errorCode("VALIDATION_ERROR")
                .path(path)
                .fieldErrors(new HashMap<>(fieldErrors))
                .errorCount(fieldErrors.size())
                .build();
    }

    // ==========================================
    // MÉTODOS DE CONVENIENCIA
    // ==========================================

    //Agrega un error de campo
    public ValidationErrorResponse addFieldError(String field, String message) {
        if (this.fieldErrors == null) {
            this.fieldErrors = new HashMap<>();
        }
        this.fieldErrors.put(field, message);
        updateErrorCount();
        return this;
    }

    //Agrega un error global
    public ValidationErrorResponse addGlobalError(String message) {
        if (this.globalErrors == null) {
            this.globalErrors = new ArrayList<>();
        }
        this.globalErrors.add(message);
        updateErrorCount();
        return this;
    }

    //Agrega múltiples errores de campo
    public ValidationErrorResponse addFieldErrors(Map<String, String> errors) {
        if (this.fieldErrors == null) {
            this.fieldErrors = new HashMap<>();
        }
        this.fieldErrors.putAll(errors);
        updateErrorCount();
        return this;
    }

    //Verifica si hay errores de campo
    public boolean hasFieldErrors() {
        return fieldErrors != null && !fieldErrors.isEmpty();
    }

    //Verifica si hay errores globales
    public boolean hasGlobalErrors() {
        return globalErrors != null && !globalErrors.isEmpty();
    }

    //Obtiene el primer error de campo
    public String getFirstFieldError() {
        if (hasFieldErrors()) {
            return fieldErrors.values().iterator().next();
        }
        return null;
    }

    //Obtiene el primer error global
    public String getFirstGlobalError() {
        if (hasGlobalErrors()) {
            return globalErrors.get(0);
        }
        return null;
    }

    // ==========================================
    // MÉTODOS PRIVADOS
    // ==========================================

    //Actualiza el contador de errores
    private void updateErrorCount() {
        this.errorCount = (fieldErrors != null ? fieldErrors.size() : 0) +
                (globalErrors != null ? globalErrors.size() : 0);
    }

    //Sanitiza el valor rechazado para evitar exposición de datos sensibles
    private static Object sanitizeRejectedValue(Object value) {
        if (value == null) return null;

        String stringValue = value.toString();

        // Ocultar contraseñas
        if (stringValue.toLowerCase().contains("password") ||
                stringValue.toLowerCase().contains("pass")) {
            return "[OCULTO]";
        }

        // Limitar longitud
        if (stringValue.length() > 100) {
            return stringValue.substring(0, 100) + "...";
        }

        return value;
    }
}