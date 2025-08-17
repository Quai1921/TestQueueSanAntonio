package queue_san_antonio.queues.models;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.ArrayList;


@Entity
@Table(name = "horarios_atencion")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class HorarioAtencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DayOfWeek diaSemana; // MONDAY, TUESDAY, etc.

    @Column(name = "hora_inicio", nullable = false)
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;

    @Column(name = "intervalo_citas")
    @Min(value = 5, message = "El intervalo mínimo entre citas es 5 minutos")
    @Max(value = 120, message = "El intervalo máximo entre citas es 120 minutos")
    @Builder.Default
    private Integer intervaloCitas = 30; // minutos entre cada cita

    @Column(name = "capacidad_maxima")
    @Min(value = 1, message = "La capacidad máxima debe ser mayor a 0")
    @Builder.Default
    private Integer capacidadMaxima = 1; // turnos simultáneos en este horario

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "observaciones", length = 200)
    @Size(max = 200, message = "Las observaciones no pueden exceder 200 caracteres")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Métodos helper

    /**
     * Verifica si el horario está activo
     * @return true si está activo
     */
    public boolean estaActivo() {
        return activo != null && activo;
    }

    /**
     * Verifica si una hora específica está dentro del horario de atención
     * @param hora hora a verificar
     * @return true si está dentro del horario
     */
    public boolean estaEnHorario(LocalTime hora) {
        if (hora == null) return false;
        return !hora.isBefore(horaInicio) && !hora.isAfter(horaFin);
    }

    /**
     * Calcula la duración total del horario en minutos
     * @return duración en minutos
     */
    public int getDuracionMinutos() {
        if (horaInicio == null || horaFin == null) return 0;
        return (int) java.time.Duration.between(horaInicio, horaFin).toMinutes();
    }

    /**
     * Calcula cuántas citas pueden programarse en este horario
     * @return número máximo de citas
     */
    public int getMaximoCitas() {
        int duracionTotal = getDuracionMinutos();
        if (intervaloCitas == null || intervaloCitas <= 0) return 0;
        return (duracionTotal / intervaloCitas) * (capacidadMaxima != null ? capacidadMaxima : 1);
    }

    /**
     * Genera lista de horarios disponibles para citas
     * @return lista de horarios posibles
     */
    public List<LocalTime> getHorariosDisponibles() {
        List<LocalTime> horarios = new ArrayList<>();

        if (horaInicio == null || horaFin == null || intervaloCitas == null) {
            return horarios;
        }

        LocalTime horaActual = horaInicio;
        while (!horaActual.isAfter(horaFin.minusMinutes(intervaloCitas))) {
            horarios.add(horaActual);
            horaActual = horaActual.plusMinutes(intervaloCitas);
        }

        return horarios;
    }

    /**
     * Obtiene el siguiente horario disponible después de una hora dada
     * @param horaReferencia hora de referencia
     * @return siguiente horario disponible o null si no hay
     */
    public LocalTime getSiguienteHorarioDisponible(LocalTime horaReferencia) {
        if (horaReferencia == null) return horaInicio;

        List<LocalTime> horariosDisponibles = getHorariosDisponibles();
        return horariosDisponibles.stream()
                .filter(hora -> hora.isAfter(horaReferencia))
                .findFirst()
                .orElse(null);
    }

    /**
     * Verifica si se superpone con otro horario
     * @param otroHorario horario para comparar
     * @return true si hay superposición
     */
    public boolean seSuperponeCon(HorarioAtencion otroHorario) {
        if (otroHorario == null || !this.diaSemana.equals(otroHorario.diaSemana)) {
            return false;
        }

        return !(this.horaFin.isBefore(otroHorario.horaInicio) ||
                this.horaInicio.isAfter(otroHorario.horaFin));
    }

    /**
     * Activa el horario
     */
    public void activar() {
        this.activo = true;
    }

    /**
     * Desactiva el horario
     */
    public void desactivar() {
        this.activo = false;
    }

    /**
     * Actualiza los datos del horario
     * @param horaInicio nueva hora de inicio
     * @param horaFin nueva hora de fin
     * @param intervaloCitas nuevo intervalo
     * @param capacidadMaxima nueva capacidad
     */
    public void actualizarHorario(LocalTime horaInicio, LocalTime horaFin,
                                  Integer intervaloCitas, Integer capacidadMaxima) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.intervaloCitas = intervaloCitas;
        this.capacidadMaxima = capacidadMaxima;
    }

    /**
     * Obtiene una representación textual del horario
     * @return string con día y horario
     */
    public String getDescripcionCompleta() {
        return String.format("%s de %s a %s",
                getDiaSemanaTexto(),
                horaInicio != null ? horaInicio.toString() : "00:00",
                horaFin != null ? horaFin.toString() : "00:00");
    }

    /**
     * Convierte el día de la semana a texto en español
     * @return día en español
     */
    public String getDiaSemanaTexto() {
        if (diaSemana == null) return "";

        return switch (diaSemana) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }

    /**
     * Valida que el horario sea consistente
     * @return true si es válido
     */
    public boolean esValido() {
        if (horaInicio == null || horaFin == null) return false;
        if (horaInicio.isAfter(horaFin)) return false;
        if (intervaloCitas != null && intervaloCitas <= 0) return false;
        if (capacidadMaxima != null && capacidadMaxima <= 0) return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("HorarioAtencion{id=%d, sector=%s, dia=%s, horario=%s-%s, activo=%s}",
                id,
                sector != null ? sector.getCodigo() : "null",
                diaSemana,
                horaInicio != null ? horaInicio.toString() : "null",
                horaFin != null ? horaFin.toString() : "null",
                activo);
    }
}