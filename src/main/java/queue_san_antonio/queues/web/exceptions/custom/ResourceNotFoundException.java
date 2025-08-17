package queue_san_antonio.queues.web.exceptions.custom;

import lombok.Getter;

//Excepción para recursos no encontrados
//Se lanza cuando se intenta acceder a un recurso que no existe
@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;
    private final String errorCode;

    //Constructor básico
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = "Resource";
        this.resourceId = "unknown";
        this.errorCode = "RESOURCE_NOT_FOUND";
    }

    //Constructor con tipo y ID de recurso
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s con ID '%s' no encontrado", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.errorCode = "RESOURCE_NOT_FOUND";
    }

    //Constructor completo con mensaje personalizado
    public ResourceNotFoundException(String message, String resourceType, String resourceId, String errorCode) {
        super(message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.errorCode = errorCode;
    }

    // ==========================================
    // MÉTODOS FACTORY PARA RECURSOS ESPECÍFICOS
    // ==========================================

    //Ciudadano no encontrado por DNI
    public static ResourceNotFoundException ciudadano(String dni) {
        return new ResourceNotFoundException(
                "Ciudadano con DNI '" + dni + "' no encontrado",
                "Ciudadano",
                dni,
                "CIUDADANO_NOT_FOUND"
        );
    }

    //Ciudadano no encontrado por ID
    public static ResourceNotFoundException ciudadano(Long id) {
        return new ResourceNotFoundException(
                "Ciudadano con ID '" + id + "' no encontrado",
                "Ciudadano",
                String.valueOf(id),
                "CIUDADANO_NOT_FOUND"
        );
    }

    //Empleado no encontrado por username
    public static ResourceNotFoundException empleado(String username) {
        return new ResourceNotFoundException(
                "Empleado con username '" + username + "' no encontrado",
                "Empleado",
                username,
                "EMPLEADO_NOT_FOUND"
        );
    }

    //Empleado no encontrado por ID
    public static ResourceNotFoundException empleado(Long id) {
        return new ResourceNotFoundException(
                "Empleado con ID '" + id + "' no encontrado",
                "Empleado",
                String.valueOf(id),
                "EMPLEADO_NOT_FOUND"
        );
    }

    //Sector no encontrado por código
    public static ResourceNotFoundException sector(String codigo) {
        return new ResourceNotFoundException(
                "Sector con código '" + codigo + "' no encontrado",
                "Sector",
                codigo,
                "SECTOR_NOT_FOUND"
        );
    }

    //Sector no encontrado por ID
    public static ResourceNotFoundException sector(Long id) {
        return new ResourceNotFoundException(
                "Sector con ID '" + id + "' no encontrado",
                "Sector",
                String.valueOf(id),
                "SECTOR_NOT_FOUND"
        );
    }

    //Turno no encontrado por código
    public static ResourceNotFoundException turno(String codigo) {
        return new ResourceNotFoundException(
                "Turno con código '" + codigo + "' no encontrado",
                "Turno",
                codigo,
                "TURNO_NOT_FOUND"
        );
    }

    //Turno no encontrado por ID
    public static ResourceNotFoundException turno(Long id) {
        return new ResourceNotFoundException(
                "Turno con ID '" + id + "' no encontrado",
                "Turno",
                String.valueOf(id),
                "TURNO_NOT_FOUND"
        );
    }

    //Configuración no encontrada
    public static ResourceNotFoundException configuracion(Long id) {
        return configuracionPantalla(id);
    }

    //Horario no encontrado
    public static ResourceNotFoundException horario(Long id) {
        return new ResourceNotFoundException(
                "Horario con ID '" + id + "' no encontrado",
                "HorarioAtencion",
                String.valueOf(id),
                "HORARIO_NOT_FOUND"
        );
    }

    //Mensaje institucional no encontrado
    public static ResourceNotFoundException mensaje(Long id) {
        return new ResourceNotFoundException(
                "Mensaje con ID '" + id + "' no encontrado",
                "MensajeInstitucional",
                String.valueOf(id),
                "MENSAJE_NOT_FOUND"
        );
    }

    //Recurso genérico no encontrado
    public static ResourceNotFoundException recurso(String tipo, Object id) {
        return new ResourceNotFoundException(
                tipo + " con ID '" + id + "' no encontrado",
                tipo,
                String.valueOf(id),
                "RESOURCE_NOT_FOUND"
        );
    }

    //ConfiguracionPantalla no encontrada por ID
    public static ResourceNotFoundException configuracionPantalla(Long id) {
        return new ResourceNotFoundException(
                "Configuración de pantalla con ID '" + id + "' no encontrada",
                "ConfiguracionPantalla",
                String.valueOf(id),
                "CONFIGURACION_PANTALLA_NOT_FOUND"
        );
    }

    //MensajeInstitucional no encontrado por ID
    public static ResourceNotFoundException mensajeInstitucional(Long id) {
        return new ResourceNotFoundException(
                "Mensaje institucional con ID '" + id + "' no encontrado",
                "MensajeInstitucional",
                String.valueOf(id),
                "MENSAJE_INSTITUCIONAL_NOT_FOUND"
        );
    }
}