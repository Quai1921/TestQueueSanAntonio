package queue_san_antonio.queues.models;

public enum EstadoTurno {
    GENERADO,    // Recién creado, esperando ser llamado
    LLAMADO,     // Llamado en pantalla, esperando al ciudadano
    EN_ATENCION, // Siendo atendido por un empleado
    FINALIZADO,  // Atención completada
    AUSENTE,     // Ciudadano no se presentó
    REDIRIGIDO,  // Enviado a otro sector
    CANCELADO    // Cancelado por algún motivo

}
