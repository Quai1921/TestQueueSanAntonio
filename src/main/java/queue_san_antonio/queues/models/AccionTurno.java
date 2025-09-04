package queue_san_antonio.queues.models;

public enum AccionTurno {
    GENERADO,               // Turno creado
    LLAMADO,               // Turno llamado en pantalla
    INICIADA_ATENCION,     // Comenzó la atención
    FINALIZADA_ATENCION,   // Terminó la atención
    REDIRIGIDO,           // Enviado a otro sector
    MARCADO_AUSENTE,      // Marcado como ausente
    CANCELADO,            // Cancelado
    CAMBIO_PRIORIDAD,     // Cambió la prioridad
    CAMBIO_ESTADO         // Cambio manual de estado
}
