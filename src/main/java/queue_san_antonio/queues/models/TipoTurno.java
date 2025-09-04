package queue_san_antonio.queues.models;

public enum TipoTurno {
    NORMAL,      // Turno regular por orden de llegada
    PRIORITARIO, // Turno con prioridad (mayores, discapacidad)
    ESPECIAL,    // Turno con cita previa
    REDIRIGIDO,  // Turno redirigido desde otro sector
    URGENTE      // Turno de emergencia

}
