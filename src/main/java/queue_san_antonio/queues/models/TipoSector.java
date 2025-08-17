package queue_san_antonio.queues.models;

public enum TipoSector {
    NORMAL,     // Atención por orden de llegada
    ESPECIAL,   // Requiere cita previa (ej: Intendencia)
    VIP,        // Atención prioritaria
    URGENTE     // Atención inmediata
}
