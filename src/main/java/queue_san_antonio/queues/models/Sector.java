package queue_san_antonio.queues.models;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "sectores")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "codigo", unique = true, nullable = false, length = 10)
    @NotBlank(message = "El código del sector es obligatorio")
    @Pattern(regexp = "^[A-Z]{2,10}$", message = "El código debe contener solo letras mayúsculas (2-10 caracteres)")
    @EqualsAndHashCode.Include
    private String codigo; // "INT", "CON", "INS", "REN", etc.

    @Column(name = "nombre", nullable = false, length = 100)
    @NotBlank(message = "El nombre del sector es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre; // "Intendencia", "Contabilidad", etc.

    @Column(name = "descripcion", length = 200)
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "orden_visualizacion")
    @Min(value = 0, message = "El orden de visualización debe ser mayor o igual a 0")
    private Integer ordenVisualizacion;

    @Column(name = "color", length = 7)
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe ser un código hexadecimal válido (#RRGGBB)")
    private String color; // Para identificación visual en la UI

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sector", nullable = false)
    @Builder.Default
    private TipoSector tipoSector = TipoSector.NORMAL;

    @Column(name = "requiere_cita_previa", nullable = false)
    @Builder.Default
    private Boolean requiereCitaPrevia = false;

    @Column(name = "capacidad_maxima")
    @Min(value = 1, message = "La capacidad máxima debe ser mayor a 0")
    private Integer capacidadMaxima = 1; // Turnos simultáneos que puede atender

    @Column(name = "tiempo_estimado_atencion")
    @Min(value = 1, message = "El tiempo estimado debe ser mayor a 0")
    private Integer tiempoEstimadoAtencion = 15; // minutos promedio por turno

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Empleado responsable;

    @OneToMany(mappedBy = "sector", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Empleado> empleados = new ArrayList<>();

    @OneToMany(mappedBy = "sector", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Turno> turnos = new ArrayList<>();

    @OneToMany(mappedBy = "sector", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<HorarioAtencion> horariosAtencion = new ArrayList<>();

    // Métodos helper

    /**
     * Verifica si el sector está activo
     * @return true si está activo
     */
    public boolean estaActivo() {
        return activo != null && activo;
    }

    /**
     * Verifica si es un sector especial (requiere cita previa)
     * @return true si es especial
     */
    public boolean esEspecial() {
        return tipoSector == TipoSector.ESPECIAL ||
                (requiereCitaPrevia != null && requiereCitaPrevia);
    }

    /**
     * Obtiene la representación visual del sector (código + nombre)
     * @return código - nombre
     */
    public String getNombreCompleto() {
        return String.format("%s - %s", codigo, nombre);
    }

    /**
     * Obtiene la cantidad de empleados activos del sector
     * @return número de empleados activos
     */
    public int getCantidadEmpleadosActivos() {
        if (empleados == null) return 0;
        return (int) empleados.stream()
                .filter(empleado -> empleado.getActivo() != null && empleado.getActivo())
                .count();
    }

    /**
     * Obtiene la cantidad de turnos pendientes (no finalizados)
     * @return número de turnos pendientes
     */
    public int getTurnosPendientes() {
        if (turnos == null) return 0;
        return (int) turnos.stream()
                .filter(turno -> turno.getEstado() != EstadoTurno.FINALIZADO &&
                        turno.getEstado() != EstadoTurno.CANCELADO &&
                        turno.getEstado() != EstadoTurno.AUSENTE)
                .count();
    }

    /**
     * Verifica si el sector tiene horarios de atención configurados
     * @return true si tiene horarios
     */
    public boolean tieneHorariosConfigurados() {
        return horariosAtencion != null && !horariosAtencion.isEmpty() &&
                horariosAtencion.stream().anyMatch(h -> h.getActivo() != null && h.getActivo());
    }

    /**
     * Activa el sector
     */
    public void activar() {
        this.activo = true;
    }

    /**
     * Desactiva el sector
     */
    public void desactivar() {
        this.activo = false;
    }

    /**
     * Establece el responsable del sector
     * @param empleado nuevo responsable
     */
    public void establecerResponsable(Empleado empleado) {
        this.responsable = empleado;
    }

    /**
     * Actualiza la configuración básica del sector
     * @param nombre nuevo nombre
     * @param descripcion nueva descripción
     * @param color nuevo color
     * @param capacidadMaxima nueva capacidad
     * @param tiempoEstimado nuevo tiempo estimado
     */
    public void actualizarConfiguracion(String nombre, String descripcion, String color,
                                        Integer capacidadMaxima, Integer tiempoEstimado) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.color = color;
        this.capacidadMaxima = capacidadMaxima;
        this.tiempoEstimadoAtencion = tiempoEstimado;
    }

    /**
     * Establece el tipo y configuración especial del sector
     * @param tipoSector tipo del sector
     * @param requiereCita si requiere cita previa
     */
    public void configurarTipoEspecial(TipoSector tipoSector, boolean requiereCita) {
        this.tipoSector = tipoSector;
        this.requiereCitaPrevia = requiereCita;
    }

    // Obtiene lista de operadores del sector
    public List<Empleado> getOperadores() {
        return empleados.stream()
                .filter(empleado -> empleado.getRol() == RolEmpleado.OPERADOR)
                .filter(Empleado::getActivo)
                .toList();
    }

    // Obtiene total de turnos atendidos del sector
    public Integer getTurnosAtendidos() {
        return empleados.stream()
                .mapToInt(Empleado::getCantidadTurnosAtendidos)
                .sum();
    }

    @Override
    public String toString() {
        return String.format("Sector{id=%d, codigo='%s', nombre='%s', tipo=%s, activo=%s}",
                id, codigo, nombre, tipoSector, activo);
    }



}

