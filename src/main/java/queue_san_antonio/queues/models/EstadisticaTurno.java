package queue_san_antonio.queues.models;

import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "estadisticas_turnos")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EstadisticaTurno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "fecha", nullable = false)
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado; // null para estadísticas generales del sector

    @Column(name = "turnos_generados", nullable = false)
    @Min(value = 0, message = "Los turnos generados no pueden ser negativos")
    @Builder.Default
    private Integer turnosGenerados = 0;

    @Column(name = "turnos_atendidos", nullable = false)
    @Min(value = 0, message = "Los turnos atendidos no pueden ser negativos")
    @Builder.Default
    private Integer turnosAtendidos = 0;

    @Column(name = "turnos_ausentes", nullable = false)
    @Min(value = 0, message = "Los turnos ausentes no pueden ser negativos")
    @Builder.Default
    private Integer turnosAusentes = 0;

    @Column(name = "turnos_redirigidos", nullable = false)
    @Min(value = 0, message = "Los turnos redirigidos no pueden ser negativos")
    @Builder.Default
    private Integer turnosRedirigidos = 0;

    @Column(name = "turnos_cancelados", nullable = false)
    @Min(value = 0, message = "Los turnos cancelados no pueden ser negativos")
    @Builder.Default
    private Integer turnosCancelados = 0;

    @Column(name = "tiempo_promedio_espera")
    @Min(value = 0, message = "El tiempo promedio de espera no puede ser negativo")
    @Builder.Default
    private Integer tiempoPromedioEspera = 0; // minutos

    @Column(name = "tiempo_promedio_atencion")
    @Min(value = 0, message = "El tiempo promedio de atención no puede ser negativo")
    @Builder.Default
    private Integer tiempoPromedioAtencion = 0; // minutos

    @Column(name = "tiempo_total_atencion")
    @Min(value = 0, message = "El tiempo total de atención no puede ser negativo")
    @Builder.Default
    private Integer tiempoTotalAtencion = 0; // minutos totales trabajados

    @Column(name = "hora_pico")
    private LocalTime horaPico; // Hora con mayor demanda

    @Column(name = "cantidad_pico")
    @Min(value = 0, message = "La cantidad pico no puede ser negativa")
    @Builder.Default
    private Integer cantidadPico = 0; // Turnos en hora pico

    @Column(name = "tiempo_maximo_espera")
    @Min(value = 0, message = "El tiempo máximo de espera no puede ser negativo")
    private Integer tiempoMaximoEspera = 0; // Tiempo de espera más largo

    @Column(name = "tiempo_minimo_espera")
    @Min(value = 0, message = "El tiempo mínimo de espera no puede ser negativo")
    private Integer tiempoMinimoEspera = 0; // Tiempo de espera más corto

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Métodos helper

    /**
     * Verifica si es una estadística general del sector (sin empleado específico)
     * @return true si es estadística general
     */
    public boolean esEstadisticaGeneral() {
        return empleado == null;
    }

    /**
     * Verifica si es una estadística de empleado específico
     * @return true si es de empleado específico
     */
    public boolean esEstadisticaEmpleado() {
        return empleado != null;
    }

    /**
     * Calcula el total de turnos procesados
     * @return suma de atendidos + ausentes + cancelados
     */
    public int getTotalTurnosProcesados() {
        return turnosAtendidos + turnosAusentes + turnosCancelados;
    }

    /**
     * Calcula el porcentaje de eficiencia (atendidos vs generados)
     * @return porcentaje de 0 a 100
     */
    public BigDecimal getPorcentajeEficiencia() {
        if (turnosGenerados == 0) return BigDecimal.ZERO;

        BigDecimal atendidos = new BigDecimal(turnosAtendidos);
        BigDecimal generados = new BigDecimal(turnosGenerados);

        return atendidos.multiply(new BigDecimal("100"))
                .divide(generados, 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el porcentaje de ausencias
     * @return porcentaje de ausencias
     */
    public BigDecimal getPorcentajeAusencias() {
        if (turnosGenerados == 0) return BigDecimal.ZERO;

        BigDecimal ausentes = new BigDecimal(turnosAusentes);
        BigDecimal generados = new BigDecimal(turnosGenerados);

        return ausentes.multiply(new BigDecimal("100"))
                .divide(generados, 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el porcentaje de redirecciones
     * @return porcentaje de redirecciones
     */
    public BigDecimal getPorcentajeRedirecciones() {
        if (turnosGenerados == 0) return BigDecimal.ZERO;

        BigDecimal redirigidos = new BigDecimal(turnosRedirigidos);
        BigDecimal generados = new BigDecimal(turnosGenerados);

        return redirigidos.multiply(new BigDecimal("100"))
                .divide(generados, 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula la productividad del empleado (turnos por hora)
     * @return turnos atendidos por hora trabajada
     */
    public BigDecimal getProductividadPorHora() {
        if (tiempoTotalAtencion == 0) return BigDecimal.ZERO;

        BigDecimal atendidos = new BigDecimal(turnosAtendidos);
        BigDecimal horasTrabajadas = new BigDecimal(tiempoTotalAtencion).divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP);

        if (horasTrabajadas.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return atendidos.divide(horasTrabajadas, 2, RoundingMode.HALF_UP);
    }

    /**
     * Incrementa el contador de turnos generados
     */
    public void incrementarGenerados() {
        this.turnosGenerados++;
    }

    /**
     * Incrementa el contador de turnos atendidos
     */
    public void incrementarAtendidos() {
        this.turnosAtendidos++;
    }

    /**
     * Incrementa el contador de turnos ausentes
     */
    public void incrementarAusentes() {
        this.turnosAusentes++;
    }

    /**
     * Incrementa el contador de turnos redirigidos
     */
    public void incrementarRedirigidos() {
        this.turnosRedirigidos++;
    }

    /**
     * Incrementa el contador de turnos cancelados
     */
    public void incrementarCancelados() {
        this.turnosCancelados++;
    }

    /**
     * Actualiza el tiempo promedio de espera
     * @param nuevoTiempo tiempo de espera del nuevo turno
     */
//    public void actualizarTiempoEspera(int nuevoTiempo) {
//        if (turnosAtendidos == 0) {
//            this.tiempoPromedioEspera = nuevoTiempo;
//            this.tiempoMinimoEspera = nuevoTiempo;
//            this.tiempoMaximoEspera = nuevoTiempo;
//        } else {
//            // Recalcular promedio
//            int totalTiempo = this.tiempoPromedioEspera * (turnosAtendidos - 1) + nuevoTiempo;
//            this.tiempoPromedioEspera = totalTiempo / turnosAtendidos;
//
//            // Actualizar mínimo y máximo
//            if (nuevoTiempo < this.tiempoMinimoEspera) {
//                this.tiempoMinimoEspera = nuevoTiempo;
//            }
//            if (nuevoTiempo > this.tiempoMaximoEspera) {
//                this.tiempoMaximoEspera = nuevoTiempo;
//            }
//        }
    public void actualizarTiempoEspera(int nuevoTiempo) {
        // Primera atención de la serie
        if (turnosAtendidos <= 1) {
            this.tiempoPromedioEspera = nuevoTiempo;
            this.tiempoMinimoEspera = nuevoTiempo;
            this.tiempoMaximoEspera = nuevoTiempo;
            return;
        }

        // Recalcular promedio incremental
        int totalTiempo = this.tiempoPromedioEspera * (turnosAtendidos - 1) + nuevoTiempo;
        this.tiempoPromedioEspera = totalTiempo / turnosAtendidos;

        // Actualizar min/max
        if (nuevoTiempo < this.tiempoMinimoEspera) this.tiempoMinimoEspera = nuevoTiempo;
        if (nuevoTiempo > this.tiempoMaximoEspera) this.tiempoMaximoEspera = nuevoTiempo;
    }

    /**
     * Actualiza el tiempo promedio de atención
     * @param nuevoTiempo tiempo de atención del nuevo turno
     */
    public void actualizarTiempoAtencion(int nuevoTiempo) {
        if (turnosAtendidos == 1) {
            this.tiempoPromedioAtencion = nuevoTiempo;
        } else {
            // Recalcular promedio
            int totalTiempo = this.tiempoPromedioAtencion * (turnosAtendidos - 1) + nuevoTiempo;
            this.tiempoPromedioAtencion = totalTiempo / turnosAtendidos;
        }

        // Sumar al tiempo total
        this.tiempoTotalAtencion += nuevoTiempo;
    }

    /**
     * Actualiza la hora pico si la demanda actual es mayor
     * @param hora hora actual
     * @param cantidad cantidad de turnos en esa hora
     */
    public void actualizarHoraPico(LocalTime hora, int cantidad) {
        if (cantidad > this.cantidadPico) {
            this.horaPico = hora;
            this.cantidadPico = cantidad;
        }
    }

    /**
     * Resetea todas las estadísticas a cero
     */
    public void resetear() {
        this.turnosGenerados = 0;
        this.turnosAtendidos = 0;
        this.turnosAusentes = 0;
        this.turnosRedirigidos = 0;
        this.turnosCancelados = 0;
        this.tiempoPromedioEspera = 0;
        this.tiempoPromedioAtencion = 0;
        this.tiempoTotalAtencion = 0;
        this.horaPico = null;
        this.cantidadPico = 0;
        this.tiempoMaximoEspera = 0;
        this.tiempoMinimoEspera = 0;
    }

    /**
     * Obtiene un resumen textual de las estadísticas
     * @return resumen de estadísticas
     */
    public String getResumen() {
        return String.format(
                "Fecha: %s | Sector: %s | Generados: %d | Atendidos: %d | Ausentes: %d | " +
                        "Eficiencia: %.2f%% | Tiempo espera: %d min | Tiempo atención: %d min",
                fecha,
                sector != null ? sector.getNombre() : "N/A",
                turnosGenerados,
                turnosAtendidos,
                turnosAusentes,
                getPorcentajeEficiencia(),
                tiempoPromedioEspera,
                tiempoPromedioAtencion
        );
    }

    /**
     * Crea una nueva estadística para un sector y fecha específicos
     * @param fecha fecha de la estadística
     * @param sector sector
     * @param empleado empleado (null para estadística general)
     * @return nueva estadística inicializada
     */
    public static EstadisticaTurno crearNueva(LocalDate fecha, Sector sector, Empleado empleado) {
        return EstadisticaTurno.builder()
                .fecha(fecha)
                .sector(sector)
                .empleado(empleado)
                .build();
    }

    @Override
    public String toString() {
        return String.format("EstadisticaTurno{id=%d, fecha=%s, sector=%s, empleado=%s, generados=%d, atendidos=%d}",
                id, fecha,
                sector != null ? sector.getCodigo() : "null",
                empleado != null ? empleado.getUsername() : "General",
                turnosGenerados, turnosAtendidos);
    }
}