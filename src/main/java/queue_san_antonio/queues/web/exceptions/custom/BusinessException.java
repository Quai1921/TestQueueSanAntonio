package queue_san_antonio.queues.web.exceptions.custom;

import lombok.Getter;

//Excepción para errores de lógica de negocio
//Se lanza cuando se violan reglas de negocio específicas del sistema
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final Object[] args;

    //Constructor con mensaje simple
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.args = null;
    }

    //Constructor con mensaje y código de error
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    //Constructor completo con argumentos
    public BusinessException(String message, String errorCode, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    //Constructor con causa
    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }

    // ==========================================
    // MÉTODOS FACTORY PARA ERRORES COMUNES
    // ==========================================

    //Error cuando un ciudadano ya tiene turno pendiente
    public static BusinessException ciudadanoConTurnoPendiente(String dni) {
        return new BusinessException(
                "El ciudadano con DNI " + dni + " ya tiene un turno pendiente",
                "CIUDADANO_TURNO_PENDIENTE",
                dni
        );
    }

    //Error cuando un sector está inactivo
    public static BusinessException sectorInactivo(String codigoSector) {
        return new BusinessException(
                "El sector " + codigoSector + " está inactivo",
                "SECTOR_INACTIVO",
                codigoSector
        );
    }

    //Error cuando no hay turnos pendientes en cola
    public static BusinessException noHayTurnosPendientes(String codigoSector) {
        return new BusinessException(
                "No hay turnos pendientes en el sector " + codigoSector,
                "NO_HAY_TURNOS_PENDIENTES",
                codigoSector
        );
    }

    //Error cuando un empleado no tiene permisos para un sector
    public static BusinessException sinPermisosSector(String username, String codigoSector) {
        return new BusinessException(
                "El empleado " + username + " no tiene permisos para el sector " + codigoSector,
                "SIN_PERMISOS_SECTOR",
                username, codigoSector
        );
    }

    //Error cuando se intenta una operación en estado inválido
    public static BusinessException estadoInvalido(String operacion, String estadoActual, String estadoRequerido) {
        return new BusinessException(
                String.format("No se puede %s. Estado actual: %s, requerido: %s", operacion, estadoActual, estadoRequerido),
                "ESTADO_INVALIDO",
                operacion, estadoActual, estadoRequerido
        );
    }

    //Error cuando se excede la capacidad máxima
    public static BusinessException capacidadExcedida(String recurso, int actual, int maximo) {
        return new BusinessException(
                String.format("Capacidad excedida en %s: %d/%d", recurso, actual, maximo),
                "CAPACIDAD_EXCEDIDA",
                recurso, actual, maximo
        );
    }

    //Error cuando un recurso está duplicado
    public static BusinessException recursoDuplicado(String tipoRecurso, String identificador) {
        return new BusinessException(
                "Ya existe " + tipoRecurso + " con identificador: " + identificador,
                "RECURSO_DUPLICADO",
                tipoRecurso, identificador
        );
    }

    //Error de horario fuera de atención
    public static BusinessException fueraDeHorario(String sector, String horarioActual, String horarioValido) {
        return new BusinessException(
                String.format("Sector %s fuera de horario. Actual: %s, válido: %s", sector, horarioActual, horarioValido),
                "FUERA_DE_HORARIO",
                sector, horarioActual, horarioValido
        );
    }
}