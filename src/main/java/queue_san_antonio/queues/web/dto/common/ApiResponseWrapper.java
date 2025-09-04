package queue_san_antonio.queues.web.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class ApiResponseWrapper<T> {

    //Indica si la operación fue exitosa
    private boolean success;

    //Mensaje descriptivo de la operación
    private String message;

    //Datos de respuesta (cuando success = true)
    private T data;

    //Información de error (cuando success = false)
    private ErrorInfo error;

    //Timestamp de la respuesta
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    //Metadatos adicionales (paginación, conteos, etc.)
    private Object metadata;

    //Información de error anidada
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorInfo {
        private String code;
        private String detail;
        private String field;
        private Object rejectedValue;
    }

    // ==========================================
    // MÉTODOS FACTORY PARA RESPUESTAS DE ÉXITO
    // ==========================================

    //Crea una respuesta exitosa con datos
    public static <T> ApiResponseWrapper<T> success(T data) {
        return ApiResponseWrapper.<T>builder()
                .success(true)
                .message("Operación exitosa")
                .data(data)
                .build();
    }

    //Crea una respuesta exitosa con datos y mensaje personalizado
    public static <T> ApiResponseWrapper<T> success(T data, String message) {
        return ApiResponseWrapper.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    //Crea una respuesta exitosa solo con mensaje (sin datos)
    public static <T> ApiResponseWrapper<T> success(String message) {
        return ApiResponseWrapper.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    //Crea una respuesta exitosa con datos y metadatos
    public static <T> ApiResponseWrapper<T> success(T data, String message, Object metadata) {
        return ApiResponseWrapper.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .metadata(metadata)
                .build();
    }

    // ==========================================
    // MÉTODOS FACTORY PARA RESPUESTAS DE ERROR
    // ==========================================

    //Crea una respuesta de error simple
    public static <T> ApiResponseWrapper<T> error(String message) {
        return ApiResponseWrapper.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    //Crea una respuesta de error con código
    public static <T> ApiResponseWrapper<T> error(String message, String errorCode) {
        return ApiResponseWrapper.<T>builder()
                .success(false)
                .message(message)
                .error(ErrorInfo.builder()
                        .code(errorCode)
                        .build())
                .build();
    }

    //Crea una respuesta de error completa
    public static <T> ApiResponseWrapper<T> error(String message, String errorCode, String detail) {
        return ApiResponseWrapper.<T>builder()
                .success(false)
                .message(message)
                .error(ErrorInfo.builder()
                        .code(errorCode)
                        .detail(detail)
                        .build())
                .build();
    }

    //Crea una respuesta de error de validación
    public static <T> ApiResponseWrapper<T> validationError(String message, String field, Object rejectedValue) {
        return ApiResponseWrapper.<T>builder()
                .success(false)
                .message(message)
                .error(ErrorInfo.builder()
                        .code("VALIDATION_ERROR")
                        .field(field)
                        .rejectedValue(rejectedValue)
                        .build())
                .build();
    }

    // ==========================================
    // MÉTODOS DE CONVENIENCIA
    // ==========================================


    //Verifica si la respuesta es de error
    public boolean isError() {
        return !success;
    }

    //Obtiene el código de error si existe
    public String getErrorCode() {
        return error != null ? error.getCode() : null;
    }

    //Obtiene el detalle del error si existe
    public String getErrorDetail() {
        return error != null ? error.getDetail() : null;
    }
}