package queue_san_antonio.queues.models;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;



@Entity
@Table(name = "historial_turnos")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class HistorialTurno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id", nullable = false)
    private Turno turno;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false)
    private AccionTurno accion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_origen_id")
    private Sector sectorOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_destino_id")
    private Sector sectorDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado; // Quien realizó la acción

    @CreationTimestamp
    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "motivo", length = 200)
    @Size(max = 200, message = "El motivo no puede exceder 200 caracteres")
    private String motivo;

    @Column(name = "estado_anterior")
    @Enumerated(EnumType.STRING)
    private EstadoTurno estadoAnterior;

    @Column(name = "estado_nuevo")
    @Enumerated(EnumType.STRING)
    private EstadoTurno estadoNuevo;

    @Column(name = "prioridad_anterior")
    private Integer prioridadAnterior;

    @Column(name = "prioridad_nueva")
    private Integer prioridadNueva;

    // Métodos helper

    /**
     * Verifica si es una acción de redirección
     * @return true si es redirección
     */
    public boolean esRedireccion() {
        return accion == AccionTurno.REDIRIGIDO;
    }

    /**
     * Verifica si es una acción de cambio de estado
     * @return true si cambió el estado
     */
    public boolean esCambioEstado() {
        return estadoAnterior != null && estadoNuevo != null &&
                !estadoAnterior.equals(estadoNuevo);
    }

    /**
     * Verifica si es una acción de cambio de prioridad
     * @return true si cambió la prioridad
     */
    public boolean esCambioPrioridad() {
        return prioridadAnterior != null && prioridadNueva != null &&
                !prioridadAnterior.equals(prioridadNueva);
    }

    /**
     * Obtiene una descripción textual de la acción
     * @return descripción de la acción
     */
    public String getDescripcionAccion() {
        return switch (accion) {
            case GENERADO -> "Turno generado";
            case LLAMADO -> "Turno llamado en pantalla";
            case INICIADA_ATENCION -> "Atención iniciada";
            case FINALIZADA_ATENCION -> "Atención finalizada";
            case REDIRIGIDO -> String.format("Redirigido de %s a %s",
                    sectorOrigen != null ? sectorOrigen.getNombre() : "Desconocido",
                    sectorDestino != null ? sectorDestino.getNombre() : "Desconocido");
            case MARCADO_AUSENTE -> "Marcado como ausente";
            case CANCELADO -> "Turno cancelado";
            case CAMBIO_PRIORIDAD -> String.format("Prioridad cambiada de %d a %d",
                    prioridadAnterior != null ? prioridadAnterior : 0,
                    prioridadNueva != null ? prioridadNueva : 0);
            case CAMBIO_ESTADO -> String.format("Estado cambiado de %s a %s",
                    estadoAnterior != null ? estadoAnterior : "Desconocido",
                    estadoNuevo != null ? estadoNuevo : "Desconocido");
        };
    }

    /**
     * Obtiene el nombre del empleado que realizó la acción
     * @return nombre del empleado o "Sistema" si no hay empleado
     */
    public String getNombreEmpleado() {
        if (empleado == null) return "Sistema";
        return empleado.getNombreCompleto();
    }

    /**
     * Obtiene información del sector origen (para redirecciones)
     * @return nombre del sector origen o null
     */
    public String getNombreSectorOrigen() {
        return sectorOrigen != null ? sectorOrigen.getNombre() : null;
    }

    /**
     * Obtiene información del sector destino (para redirecciones)
     * @return nombre del sector destino o null
     */
    public String getNombreSectorDestino() {
        return sectorDestino != null ? sectorDestino.getNombre() : null;
    }

    /**
     * Crea un registro de historial para generación de turno
     * @param turno turno generado
     * @param empleado empleado que generó (puede ser null para sistema)
     * @return registro de historial
     */
    public static HistorialTurno crearRegistroGeneracion(Turno turno, Empleado empleado) {
        return HistorialTurno.builder()
                .turno(turno)
                .accion(AccionTurno.GENERADO)
                .empleado(empleado)
                .estadoNuevo(EstadoTurno.GENERADO)
                .observaciones("Turno generado en el sistema")
                .build();
    }

    /**
     * Crea un registro de historial para llamado de turno
     * @param turno turno llamado
     * @param empleado empleado que llamó
     * @return registro de historial
     */
    public static HistorialTurno crearRegistroLlamado(Turno turno, Empleado empleado) {
        return HistorialTurno.builder()
                .turno(turno)
                .accion(AccionTurno.LLAMADO)
                .empleado(empleado)
                .estadoAnterior(EstadoTurno.GENERADO)
                .estadoNuevo(EstadoTurno.LLAMADO)
                .observaciones("Turno llamado en pantalla")
                .build();
    }

    /**
     * Crea un registro de historial para inicio de atención
     * @param turno turno en atención
     * @param empleado empleado que atiende
     * @return registro de historial
     */
    public static HistorialTurno crearRegistroInicioAtencion(Turno turno, Empleado empleado) {
        return HistorialTurno.builder()
                .turno(turno)
                .accion(AccionTurno.INICIADA_ATENCION)
                .empleado(empleado)
                .estadoAnterior(EstadoTurno.LLAMADO)
                .estadoNuevo(EstadoTurno.EN_ATENCION)
                .observaciones("Atención iniciada por " + empleado.getNombreCompleto())
                .build();
    }

    /**
     * Crea un registro de historial para finalización de atención
     * @param turno turno finalizado
     * @param empleado empleado que atendió
     * @param observaciones observaciones finales
     * @return registro de historial
     */
    public static HistorialTurno crearRegistroFinalizacion(Turno turno, Empleado empleado, String observaciones) {
        return HistorialTurno.builder()
                .turno(turno)
                .accion(AccionTurno.FINALIZADA_ATENCION)
                .empleado(empleado)
                .estadoAnterior(EstadoTurno.EN_ATENCION)
                .estadoNuevo(EstadoTurno.FINALIZADO)
                .observaciones(observaciones)
                .build();
    }

    /**
     * Crea un registro de historial para redirección
     * @param turno turno redirigido
     * @param empleado empleado que redirigió
     * @param sectorOrigen sector de origen
     * @param sectorDestino sector de destino
     * @param motivo motivo de la redirección
     * @return registro de historial
     */
    public static HistorialTurno crearRegistroRedireccion(Turno turno, Empleado empleado,
                                                          Sector sectorOrigen, Sector sectorDestino, String motivo) {
        return HistorialTurno.builder()
                .turno(turno)
                .accion(AccionTurno.REDIRIGIDO)
                .empleado(empleado)
                .sectorOrigen(sectorOrigen)
                .sectorDestino(sectorDestino)
                .motivo(motivo)
                .observaciones("Turno redirigido: " + motivo)
                .build();
    }

    /**
     * Crea un registro de historial para ausencia
     * @param turno turno ausente
     * @param empleado empleado que marcó ausente
     * @return registro de historial
     */
    public static HistorialTurno crearRegistroAusente(Turno turno, Empleado empleado) {
        return HistorialTurno.builder()
                .turno(turno)
                .accion(AccionTurno.MARCADO_AUSENTE)
                .empleado(empleado)
                .estadoAnterior(turno.getEstado())
                .estadoNuevo(EstadoTurno.AUSENTE)
                .observaciones("Ciudadano no se presentó")
                .build();
    }

    @Override
    public String toString() {
        return String.format("HistorialTurno{id=%d, turno=%s, accion=%s, fecha=%s, empleado=%s}",
                id,
                turno != null ? turno.getCodigo() : "null",
                accion,
                fechaHora,
                empleado != null ? empleado.getUsername() : "Sistema");
    }



}

