package queue_san_antonio.queues.models;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.UniqueConstraint;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "turnos",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_turno_codigo_fecha",
                        columnNames = {"codigo", "fecha_hora_generacion"}
                )
        })
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "codigo", nullable = false, length = 20)
//    unique = true,
    @NotBlank(message = "El código del turno es obligatorio")
    @EqualsAndHashCode.Include
    private String codigo; // A001, B042, etc.

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ciudadano_id", nullable = false)
    private Ciudadano ciudadano;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_original_id")
    private Sector sectorOriginal; // Para tracking de redirecciones

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Builder.Default
    private EstadoTurno estado = EstadoTurno.GENERADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    @Builder.Default
    private TipoTurno tipo = TipoTurno.NORMAL;

    @CreationTimestamp
    @Column(name = "fecha_hora_generacion", nullable = false, updatable = false)
    private LocalDateTime fechaHoraGeneracion;

    @Column(name = "fecha_hora_llamado")
    private LocalDateTime fechaHoraLlamado;

    @Column(name = "fecha_hora_atencion")
    private LocalDateTime fechaHoraAtencion;

    @Column(name = "fecha_hora_finalizacion")
    private LocalDateTime fechaHoraFinalizacion;

    // Para turnos especiales con cita previa
    @Column(name = "fecha_cita")
    private LocalDate fechaCita;

    @Column(name = "hora_cita")
    private LocalTime horaCita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_atencion_id")
    private Empleado empleadoAtencion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "motivo_redireccion", length = 200)
    @Size(max = 200, message = "El motivo de redirección no puede exceder 200 caracteres")
    private String motivoRedireccion;

    @Column(name = "prioridad", nullable = false)
    @Min(value = 0, message = "La prioridad debe ser mayor o igual a 0")
    @Max(value = 10, message = "La prioridad no puede ser mayor a 10")
    @Builder.Default
    private Integer prioridad = 0; // 0=normal, 1=prioritario, 2=urgente, etc.

    @Column(name = "numero_orden")
    private Integer numeroOrden; // Orden dentro del sector para el día

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relaciones
    @OneToMany(mappedBy = "turno", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<HistorialTurno> historial = new ArrayList<>();

    // Métodos helper

    /**
     * Verifica si el turno está activo (no finalizado)
     * @return true si está activo
     */
    public boolean estaActivo() {
        return estado != EstadoTurno.FINALIZADO &&
                estado != EstadoTurno.CANCELADO &&
                estado != EstadoTurno.AUSENTE;
    }

    /**
     * Verifica si el turno ya fue llamado
     * @return true si fue llamado
     */
    public boolean fueLlamado() {
        return fechaHoraLlamado != null;
    }

    /**
     * Verifica si el turno está en atención
     * @return true si está siendo atendido
     */
    public boolean estaEnAtencion() {
        return estado == EstadoTurno.EN_ATENCION;
    }

    /**
     * Verifica si es un turno prioritario
     * @return true si tiene prioridad mayor a 0
     */
    public boolean esPrioritario() {
        return prioridad != null && prioridad > 0;
    }

    /**
     * Verifica si es un turno especial (con cita previa)
     * @return true si es especial
     */
    public boolean esEspecial() {
        return tipo == TipoTurno.ESPECIAL || fechaCita != null;
    }

    /**
     * Verifica si fue redirigido desde otro sector
     * @return true si fue redirigido
     */
    public boolean fueRedirigido() {
        return sectorOriginal != null && !sectorOriginal.equals(sector);
    }

    /**
     * Calcula el tiempo de espera desde la generación hasta el llamado
     * @return duración en minutos, null si no fue llamado
     */
    public Long getTiempoEsperaminutos() {
        if (fechaHoraLlamado == null) return null;
        return Duration.between(fechaHoraGeneracion, fechaHoraLlamado).toMinutes();
    }

    /**
     * Calcula el tiempo de atención
     * @return duración en minutos, null si no fue finalizado
     */
    public Long getTiempoAtencionMinutos() {
        if (fechaHoraAtencion == null || fechaHoraFinalizacion == null) return null;
        return Duration.between(fechaHoraAtencion, fechaHoraFinalizacion).toMinutes();
    }

    /**
     * Calcula el tiempo total desde generación hasta finalización
     * @return duración en minutos, null si no fue finalizado
     */
    public Long getTiempoTotalMinutos() {
        if (fechaHoraFinalizacion == null) return null;
        return Duration.between(fechaHoraGeneracion, fechaHoraFinalizacion).toMinutes();
    }

    /**
     * Llama al turno
     */
    public void llamar() {
        this.estado = EstadoTurno.LLAMADO;
        this.fechaHoraLlamado = LocalDateTime.now();
    }

    /**
     * Inicia la atención del turno
     * @param empleado empleado que atiende
     */
    public void iniciarAtencion(Empleado empleado) {
        this.estado = EstadoTurno.EN_ATENCION;
        this.fechaHoraAtencion = LocalDateTime.now();
        this.empleadoAtencion = empleado;
    }

    /**
     * Finaliza la atención del turno
     * @param observaciones observaciones finales
     */
    public void finalizarAtencion(String observaciones) {
        this.estado = EstadoTurno.FINALIZADO;
        this.fechaHoraFinalizacion = LocalDateTime.now();
        this.observaciones = observaciones;
    }

    /**
     * Marca el turno como ausente
     */
    public void marcarAusente() {
        this.estado = EstadoTurno.AUSENTE;
        this.fechaHoraFinalizacion = LocalDateTime.now();
    }

    /**
     * Cancela el turno
     * @param motivo motivo de cancelación
     */
    public void cancelar(String motivo) {
        this.estado = EstadoTurno.CANCELADO;
        this.fechaHoraFinalizacion = LocalDateTime.now();
        this.observaciones = motivo;
    }

    /**
     * Redirige el turno a otro sector
     * @param nuevoSector sector destino
     * @param motivo motivo de la redirección
     */
    public void redirigirASector(Sector nuevoSector, String motivo) {
        if (this.sectorOriginal == null) {
            this.sectorOriginal = this.sector; // Guardar sector original
        }
        this.sector = nuevoSector;
        this.estado = EstadoTurno.REDIRIGIDO;
        this.motivoRedireccion = motivo;
        this.tipo = TipoTurno.REDIRIGIDO;
        this.prioridad = Math.max(this.prioridad, 1); // Darle prioridad al redirigido
    }

    /**
     * Establece la prioridad del turno
     * @param nuevaPrioridad nivel de prioridad (0-10)
     */
    public void establecerPrioridad(int nuevaPrioridad) {
        if (nuevaPrioridad < 0 || nuevaPrioridad > 10) {
            throw new IllegalArgumentException("La prioridad debe estar entre 0 y 10");
        }
        this.prioridad = nuevaPrioridad;

        if (nuevaPrioridad > 0 && this.tipo == TipoTurno.NORMAL) {
            this.tipo = TipoTurno.PRIORITARIO;
        }
    }

    /**
     * Configura el turno como cita especial
     * @param fecha fecha de la cita
     * @param hora hora de la cita
     */
    public void configurarCitaEspecial(LocalDate fecha, LocalTime hora) {
        this.fechaCita = fecha;
        this.horaCita = hora;
        this.tipo = TipoTurno.ESPECIAL;
    }

    /**
     * Obtiene el código para mostrar en pantalla (ej: A001)
     * @return código del turno
     */
    public String getCodigoDisplay() {
        return codigo != null ? codigo.toUpperCase() : "";
    }

    /**
     * Obtiene el sector actual (considerando redirecciones)
     * @return sector actual
     */
    public Sector getSectorActual() {
        return sector;
    }

    /**
     * Obtiene el sector donde se generó originalmente
     * @return sector original o actual si no fue redirigido
     */
    public Sector getSectorOrigen() {
        return sectorOriginal != null ? sectorOriginal : sector;
    }

    @Override
    public String toString() {
        return String.format("Turno{id=%d, codigo='%s', estado=%s, sector=%s, ciudadano=%s}",
                id, codigo, estado,
                sector != null ? sector.getCodigo() : "null",
                ciudadano != null ? ciudadano.getDni() : "null");
    }


}



