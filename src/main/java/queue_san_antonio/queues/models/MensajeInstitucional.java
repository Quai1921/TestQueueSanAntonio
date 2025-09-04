package queue_san_antonio.queues.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_institucionales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MensajeInstitucional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuracion_id")
    private ConfiguracionPantalla configuracion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    @Builder.Default
    private TipoMensaje tipo = TipoMensaje.TEXTO;

    @Column(name = "titulo", length = 100)
    @Size(max = 100, message = "El título no puede exceder 100 caracteres")
    private String titulo;

    @Column(name = "contenido", columnDefinition = "TEXT")
    private String contenido; // Texto del mensaje

    @Column(name = "ruta_archivo", length = 300)
    @Size(max = 300, message = "La ruta del archivo no puede exceder 300 caracteres")
    private String rutaArchivo; // Para imágenes/videos

    @Column(name = "duracion")
    @Min(value = 3, message = "La duración mínima es 3 segundos")
    @Max(value = 120, message = "La duración máxima es 120 segundos")
    @Builder.Default
    private Integer duracion = 10; // segundos

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "orden")
    @Min(value = 0, message = "El orden debe ser mayor o igual a 0")
    @Builder.Default
    private Integer orden = 0;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio; // Cuándo empezar a mostrar

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin; // Cuándo dejar de mostrar

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Métodos helper

    /**
     * Verifica si el mensaje está activo
     * @return true si está activo
     */
    public boolean estaActivo() {
        return activo != null && activo;
    }

    /**
     * Verifica si el mensaje está vigente (dentro de fechas)
     * @return true si está vigente
     */
    public boolean estaVigente() {
        LocalDateTime ahora = LocalDateTime.now();

        if (fechaInicio != null && ahora.isBefore(fechaInicio)) {
            return false;
        }

        if (fechaFin != null && ahora.isAfter(fechaFin)) {
            return false;
        }

        return true;
    }

    /**
     * Verifica si debe mostrarse (activo y vigente)
     * @return true si debe mostrarse
     */
    public boolean debeMostrarse() {
        return estaActivo() && estaVigente();
    }

    /**
     * Verifica si es un mensaje de texto
     * @return true si es texto
     */
    public boolean esTexto() {
        return tipo == TipoMensaje.TEXTO;
    }

    /**
     * Verifica si es un mensaje con imagen
     * @return true si es imagen
     */
    public boolean esImagen() {
        return tipo == TipoMensaje.IMAGEN;
    }

    /**
     * Verifica si es un mensaje con video
     * @return true si es video
     */
    public boolean esVideo() {
        return tipo == TipoMensaje.VIDEO;
    }

    /**
     * Activa el mensaje
     */
    public void activar() {
        this.activo = true;
    }

    /**
     * Desactiva el mensaje
     */
    public void desactivar() {
        this.activo = false;
    }

    /**
     * Establece el período de vigencia
     * @param inicio cuándo empezar a mostrar
     * @param fin cuándo dejar de mostrar
     */
    public void establecerVigencia(LocalDateTime inicio, LocalDateTime fin) {
        this.fechaInicio = inicio;
        this.fechaFin = fin;
    }

    /**
     * Actualiza el contenido del mensaje
     * @param titulo nuevo título
     * @param contenido nuevo contenido
     * @param duracion nueva duración
     */
    public void actualizarContenido(String titulo, String contenido, Integer duracion) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.duracion = duracion;
    }

    @Override
    public String toString() {
        return String.format("MensajeInstitucional{id=%d, tipo=%s, titulo='%s', activo=%s, orden=%d}",
                id, tipo, titulo, activo, orden);
    }
}