package queue_san_antonio.queues.models;

public enum RolEmpleado {

    ADMIN,              // Administrador general (acceso total)
    RESPONSABLE_SECTOR, // Responsable de sector (gestiona su sector y empleados)
    OPERADOR           // Operador (solo atiende turnos)
}
