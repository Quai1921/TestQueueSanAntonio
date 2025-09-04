package queue_san_antonio.queues.web.exceptions.custom;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

//Excepción para errores de validación personalizados
//Se lanza cuando hay errores de validación que no son capturados por Bean Validation
@Getter
public class ValidationException extends RuntimeException {

    private final String errorCode;
    private final Map<String, String> fieldErrors;
    private final Object rejectedValue;

    //Constructor básico con mensaje
    public ValidationException(String message) {
        super(message);
        this.errorCode = "VALIDATION_ERROR";
        this.fieldErrors = new HashMap<>();
        this.rejectedValue = null;
    }

    //Constructor con campo específico
    public ValidationException(String message, String field, Object rejectedValue) {
        super(message);
        this.errorCode = "VALIDATION_ERROR";
        this.fieldErrors = new HashMap<>();
        this.fieldErrors.put(field, message);
        this.rejectedValue = rejectedValue;
    }

    //Constructor con múltiples errores de campo
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.errorCode = "VALIDATION_ERROR";
        this.fieldErrors = new HashMap<>(fieldErrors);
        this.rejectedValue = null;
    }

    //Constructor completo
    public ValidationException(String message, String errorCode, Map<String, String> fieldErrors, Object rejectedValue) {
        super(message);
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors != null ? new HashMap<>(fieldErrors) : new HashMap<>();
        this.rejectedValue = rejectedValue;
    }

    // ==========================================
    // MÉTODOS FACTORY PARA VALIDACIONES COMUNES
    // ==========================================

    //DNI inválido
    public static ValidationException dniInvalido(String dni) {
        return new ValidationException(
                "El DNI debe tener entre 7 y 8 dígitos numéricos",
                "dni",
                dni
        );
    }

    //Email inválido
    public static ValidationException emailInvalido(String email) {
        return new ValidationException(
                "El formato del email es inválido",
                "email",
                email
        );
    }

    //Teléfono inválido
    public static ValidationException telefonoInvalido(String telefono) {
        return new ValidationException(
                "El formato del teléfono es inválido",
                "telefono",
                telefono
        );
    }

    //Código de sector inválido
    public static ValidationException codigoSectorInvalido(String codigo) {
        return new ValidationException(
                "El código del sector debe contener solo letras mayúsculas (2-10 caracteres)",
                "codigo",
                codigo
        );
    }

    //Username inválido
    public static ValidationException usernameInvalido(String username) {
        return new ValidationException(
                "El username debe tener 3-50 caracteres y solo puede contener letras, números, puntos, guiones y guiones bajos",
                "username",
                username
        );
    }

    //Contraseña inválida
    public static ValidationException passwordInvalida() {
        return new ValidationException(
                "La contraseña debe tener al menos 6 caracteres",
                "password",
                "[OCULTA]"
        );
    }

    //Rango de fechas inválido
    public static ValidationException rangoFechasInvalido() {
        Map<String, String> errors = new HashMap<>();
        errors.put("fechaInicio", "La fecha de inicio debe ser anterior a la fecha de fin");
        errors.put("fechaFin", "La fecha de fin debe ser posterior a la fecha de inicio");

        return new ValidationException(
                "Rango de fechas inválido",
                "INVALID_DATE_RANGE",
                errors,
                null
        );
    }

    //Horario inválido
    public static ValidationException horarioInvalido() {
        Map<String, String> errors = new HashMap<>();
        errors.put("horaInicio", "La hora de inicio debe ser anterior a la hora de fin");
        errors.put("horaFin", "La hora de fin debe ser posterior a la hora de inicio");

        return new ValidationException(
                "Horario inválido",
                "INVALID_TIME_RANGE",
                errors,
                null
        );
    }

    //Capacidad inválida
    public static ValidationException capacidadInvalida(Integer capacidad) {
        return new ValidationException(
                "La capacidad debe ser mayor a 0",
                "capacidadMaxima",
                capacidad
        );
    }

    //Prioridad inválida
    public static ValidationException prioridadInvalida(Integer prioridad) {
        return new ValidationException(
                "La prioridad debe estar entre 0 y 10",
                "prioridad",
                prioridad
        );
    }

    //Campos obligatorios faltantes
    public static ValidationException camposObligatorios(String... campos) {
        Map<String, String> errors = new HashMap<>();
        for (String campo : campos) {
            errors.put(campo, "Este campo es obligatorio");
        }

        return new ValidationException(
                "Faltan campos obligatorios",
                "REQUIRED_FIELDS",
                errors,
                null
        );
    }

    //Valor fuera de rango
    public static ValidationException fueraDeRango(String campo, Object valor, Object min, Object max) {
        return new ValidationException(
                String.format("El valor debe estar entre %s y %s", min, max),
                campo,
                valor
        );
    }

    // ==========================================
    // MÉTODOS DE CONVENIENCIA
    // ==========================================

    //Agrega un error de campo
    public ValidationException addFieldError(String field, String message) {
        this.fieldErrors.put(field, message);
        return this;
    }

    //Verifica si hay errores de campo
    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }

    //Obtiene el número de errores
    public int getErrorCount() {
        return fieldErrors.size();
    }
}