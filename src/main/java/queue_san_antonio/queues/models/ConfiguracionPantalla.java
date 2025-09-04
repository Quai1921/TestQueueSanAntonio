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
@Table(name = "configuracion_pantalla")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ConfiguracionPantalla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotBlank(message = "El nombre de la configuración es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre; // "Pantalla Principal", "Pantalla Sector A", etc.

    @Column(name = "tiempo_mensaje")
    @Min(value = 3, message = "El tiempo mínimo de mensaje es 3 segundos")
    @Max(value = 60, message = "El tiempo máximo de mensaje es 60 segundos")
    @Builder.Default
    private Integer tiempoMensaje = 10; // segundos que se muestra cada mensaje

    @Column(name = "tiempo_turno")
    @Min(value = 3, message = "El tiempo mínimo de turno es 3 segundos")
    @Max(value = 30, message = "El tiempo máximo de turno es 30 segundos")
    @Builder.Default
    private Integer tiempoTurno = 5; // segundos que se muestra cada turno llamado

    @Column(name = "sonido_activo", nullable = false)
    @Builder.Default
    private Boolean sonidoActivo = true;

    @Column(name = "archivo_sonido", length = 200)
    @Size(max = 200, message = "La ruta del archivo de sonido no puede exceder 200 caracteres")
    private String archivoSonido; // Ruta al archivo de sonido personalizado

    @Column(name = "volumen_sonido")
    @Min(value = 0, message = "El volumen mínimo es 0")
    @Max(value = 100, message = "El volumen máximo es 100")
    @Builder.Default
    private Integer volumenSonido = 70; // Volumen del sonido (0-100)

    @Column(name = "animaciones_activas", nullable = false)
    @Builder.Default
    private Boolean animacionesActivas = true;

    @Column(name = "texto_encabezado", length = 200)
    @Size(max = 200, message = "El texto del encabezado no puede exceder 200 caracteres")
    @Builder.Default
    private String textoEncabezado = "SISTEMA DE TURNOS - MUNICIPALIDAD";

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relaciones
    @OneToMany(mappedBy = "configuracion", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<MensajeInstitucional> mensajes = new ArrayList<>();

    // Métodos helper

    /**
     * Verifica si la configuración está activa
     * @return true si está activa
     */
    public boolean estaActiva() {
        return activo != null && activo;
    }

    /**
     * Obtiene los mensajes activos ordenados por orden
     * @return lista de mensajes activos
     */
    public List<MensajeInstitucional> getMensajesActivos() {
        if (mensajes == null) return new ArrayList<>();

        return mensajes.stream()
                .filter(mensaje -> mensaje.getActivo() != null && mensaje.getActivo())
                .sorted((m1, m2) -> Integer.compare(
                        m1.getOrden() != null ? m1.getOrden() : 0,
                        m2.getOrden() != null ? m2.getOrden() : 0))
                .toList();
    }

    /**
     * Verifica si tiene mensajes configurados
     * @return true si tiene mensajes activos
     */
    public boolean tieneMensajes() {
        return !getMensajesActivos().isEmpty();
    }

    /**
     * Activa la configuración
     */
    public void activar() {
        this.activo = true;
    }

    /**
     * Desactiva la configuración
     */
    public void desactivar() {
        this.activo = false;
    }

    /**
     * Actualiza la configuración básica
     * @param nombre nuevo nombre
     * @param tiempoMensaje nuevo tiempo de mensaje
     * @param tiempoTurno nuevo tiempo de turno
     * @param textoEncabezado nuevo encabezado
     */
    public void actualizarConfiguracion(String nombre, Integer tiempoMensaje,
                                        Integer tiempoTurno, String textoEncabezado) {
        this.nombre = nombre;
        this.tiempoMensaje = tiempoMensaje;
        this.tiempoTurno = tiempoTurno;
        this.textoEncabezado = textoEncabezado;
    }

    /**
     * Configura el sonido
     * @param activo si el sonido está activo
     * @param archivo ruta del archivo de sonido
     * @param volumen volumen del sonido
     */
    public void configurarSonido(Boolean activo, String archivo, Integer volumen) {
        this.sonidoActivo = activo;
        this.archivoSonido = archivo;
        this.volumenSonido = volumen;
    }

    /**
     * Configura la apariencia visual
     * @param animaciones si las animaciones están activas
     */
    public void configurarApariencia(Boolean animaciones) {
        this.animacionesActivas = animaciones;
    }

    @Override
    public String toString() {
        return String.format("ConfiguracionPantalla{id=%d, nombre='%s', activo=%s, mensajes=%d}",
                id, nombre, activo, mensajes != null ? mensajes.size() : 0);
    }
}