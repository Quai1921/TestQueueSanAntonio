package queue_san_antonio.queues.web.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import queue_san_antonio.queues.web.dto.common.ApiResponseWrapper;
import queue_san_antonio.queues.web.dto.common.ErrorResponse;
import queue_san_antonio.queues.web.exceptions.custom.BusinessException;
import queue_san_antonio.queues.web.exceptions.custom.ResourceNotFoundException;
import queue_san_antonio.queues.web.exceptions.custom.ValidationException;
import queue_san_antonio.queues.web.exceptions.dto.ValidationErrorResponse;

import java.time.LocalDateTime;

//Manejador global de excepciones para toda la aplicación
//Captura y maneja todas las excepciones de forma consistente
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ==========================================
    // EXCEPCIONES PERSONALIZADAS
    // ==========================================

    //Maneja errores de lógica de negocio
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        log.warn("Error de negocio: {} - Path: {} - Code: {}",
                ex.getMessage(), request.getRequestURI(), ex.getErrorCode());

        return ResponseEntity.badRequest()
                .body(ApiResponseWrapper.error(ex.getMessage(), ex.getErrorCode()));
    }

    //Maneja recursos no encontrados
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Recurso no encontrado: {} {} - Path: {}",
                ex.getResourceType(), ex.getResourceId(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseWrapper.error(ex.getMessage(), ex.getErrorCode()));
    }

    //Maneja errores de validación personalizados
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {

        log.warn("Error de validación: {} - Fields: {} - Path: {}",
                ex.getMessage(), ex.getFieldErrors().keySet(), request.getRequestURI());

        ValidationErrorResponse response = ValidationErrorResponse
                .fromValidationException(ex, request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    // ==========================================
    // EXCEPCIONES DE VALIDACIÓN SPRING
    // ==========================================

    //Maneja errores de validación de Bean Validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Error de validación Bean Validation - Path: {} - Errors: {}",
                request.getRequestURI(), ex.getBindingResult().getErrorCount());

        ValidationErrorResponse response = ValidationErrorResponse
                .fromBindingResult(ex.getBindingResult(), request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    //Maneja errores de validación en formularios
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ValidationErrorResponse> handleBindException(
            BindException ex, HttpServletRequest request) {

        log.warn("Error de bind en formulario - Path: {} - Errors: {}",
                request.getRequestURI(), ex.getBindingResult().getErrorCount());

        ValidationErrorResponse response = ValidationErrorResponse
                .fromBindingResult(ex.getBindingResult(), request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    //Maneja violaciones de constraints de validación
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        log.warn("Violación de constraint - Path: {} - Violations: {}",
                request.getRequestURI(), ex.getConstraintViolations().size());

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .status(400)
                .message("Error de validación de constraints")
                .errorCode("CONSTRAINT_VIOLATION")
                .path(request.getRequestURI())
                .build();

        // Convertir constraint violations a errores de campo
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            response.addFieldError(fieldName, message);
        });

        return ResponseEntity.badRequest().body(response);
    }

    // ==========================================
    // EXCEPCIONES DE SEGURIDAD
    // ==========================================

    //Maneja errores de autenticación
    @ExceptionHandler({
            AuthenticationException.class,
            BadCredentialsException.class
    })
    public ResponseEntity<ApiResponseWrapper<Object>> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        log.warn("Error de autenticación: {} - Path: {} - IP: {}",
                ex.getMessage(), request.getRequestURI(), getClientIp(request));

        String message = "Credenciales inválidas";
        String code = "AUTHENTICATION_FAILED";

        if (ex instanceof BadCredentialsException) {
            message = "Usuario o contraseña incorrectos";
            code = "INVALID_CREDENTIALS";
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseWrapper.error(message, code));
    }

    //Maneja cuentas deshabilitadas
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleDisabledException(
            DisabledException ex, HttpServletRequest request) {

        log.warn("Intento de acceso con cuenta deshabilitada - Path: {} - IP: {}",
                request.getRequestURI(), getClientIp(request));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseWrapper.error("Cuenta de usuario deshabilitada", "ACCOUNT_DISABLED"));
    }

    //Maneja cuentas bloqueadas
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleLockedException(
            LockedException ex, HttpServletRequest request) {

        log.warn("Intento de acceso con cuenta bloqueada - Path: {} - IP: {}",
                request.getRequestURI(), getClientIp(request));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseWrapper.error("Cuenta de usuario bloqueada", "ACCOUNT_LOCKED"));
    }

    //Maneja errores de autorización/permisos
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Acceso denegado: {} - Path: {} - IP: {}",
                ex.getMessage(), request.getRequestURI(), getClientIp(request));

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponseWrapper.error("No tiene permisos para acceder a este recurso", "ACCESS_DENIED"));
    }

    // ==========================================
    // EXCEPCIONES DE BASE DE DATOS
    // ==========================================

    //Maneja violaciones de integridad de datos
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Violación de integridad de datos - Path: {} - Error: {}",
                request.getRequestURI(), ex.getMessage());

        String message = "Error de integridad de datos";
        String code = "DATA_INTEGRITY_VIOLATION";

        // Detectar tipos específicos de violación
        String rootCause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "";

        if (rootCause.toLowerCase().contains("unique") || rootCause.toLowerCase().contains("duplicate")) {
            message = "Ya existe un registro con estos datos";
            code = "DUPLICATE_ENTRY";
        } else if (rootCause.toLowerCase().contains("foreign key")) {
            message = "No se puede completar la operación por referencias existentes";
            code = "FOREIGN_KEY_VIOLATION";
        } else if (rootCause.toLowerCase().contains("not null")) {
            message = "Faltan datos obligatorios";
            code = "NULL_VALUE_NOT_ALLOWED";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseWrapper.error(message, code));
    }

    // ==========================================
    // EXCEPCIONES HTTP Y WEB
    // ==========================================

    //Maneja métodos HTTP no soportados
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.warn("Método HTTP no soportado: {} - Path: {}",
                ex.getMethod(), request.getRequestURI());

        String message = String.format("Método %s no soportado. Métodos permitidos: %s",
                ex.getMethod(), String.join(", ", ex.getSupportedMethods()));

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponseWrapper.error(message, "METHOD_NOT_ALLOWED"));
    }

    //Maneja endpoints no encontrados
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {

        log.warn("Endpoint no encontrado: {} {} - IP: {}",
                ex.getHttpMethod(), ex.getRequestURL(), getClientIp(request));

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseWrapper.error("Endpoint no encontrado", "ENDPOINT_NOT_FOUND"));
    }

    //Maneja errores de formato JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Error al leer mensaje HTTP - Path: {} - Error: {}",
                request.getRequestURI(), ex.getMessage());

        String message = "Formato de datos inválido";

        if (ex.getMessage().contains("JSON")) {
            message = "Formato JSON inválido";
        } else if (ex.getMessage().contains("Required request body is missing")) {
            message = "El cuerpo de la petición es obligatorio";
        }

        return ResponseEntity.badRequest()
                .body(ApiResponseWrapper.error(message, "INVALID_REQUEST_FORMAT"));
    }

    //Maneja parámetros faltantes
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ValidationErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.warn("Parámetro faltante: {} - Path: {}",
                ex.getParameterName(), request.getRequestURI());

        ValidationErrorResponse response = ValidationErrorResponse.singleFieldError(
                ex.getParameterName(),
                "Este parámetro es obligatorio",
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    //Maneja errores de tipo de argumento
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ValidationErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("Error de tipo en parámetro: {} - Valor: {} - Path: {}",
                ex.getName(), ex.getValue(), request.getRequestURI());

        String message = String.format("El valor '%s' no es válido para el parámetro '%s'",
                ex.getValue(), ex.getName());

        ValidationErrorResponse response = ValidationErrorResponse.singleFieldError(
                ex.getName(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponseWrapper<Object>> handleBadRequest(
            RuntimeException ex, HttpServletRequest request) {

        var err = ErrorResponse.builder()
                .status(400).error("Bad Request")
                .message(ex.getMessage())
                .code("BAD_REQUEST")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        // Tu wrapper actual parece recibir un String:
        return ResponseEntity.badRequest().body(ApiResponseWrapper.error(err.getMessage()));
    }

    // ==========================================
    // EXCEPCIÓN GENÉRICA
    // ==========================================

    //Maneja cualquier excepción no capturada específicamente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Error interno no controlado - Path: {} - Error: {}",
                request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(500)
                .error("Internal Server Error")
                .message("Ha ocurrido un error interno en el servidor")
                .code("INTERNAL_ERROR")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        // En desarrollo, incluir stack trace
        if (isDevelopmentMode()) {
            errorResponse.setTrace(getStackTrace(ex));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    //Obtiene la IP del cliente
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    //Verifica si está en modo desarrollo
    private boolean isDevelopmentMode() {
        String profile = System.getProperty("spring.profiles.active", "");
        return profile.contains("dev") || profile.contains("local") || profile.isEmpty();
    }

    //Obtiene el stack trace como string
    private String getStackTrace(Exception ex) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }


}