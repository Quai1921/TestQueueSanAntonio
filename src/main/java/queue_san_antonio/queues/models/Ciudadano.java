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
@Table(name = "ciudadanos")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Ciudadano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "dni", unique = true, nullable = false, length = 20)
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener entre 7 y 8 dígitos")
    @EqualsAndHashCode.Include
    private String dni;

    @Column(name = "apellido", nullable = false, length = 100)
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String apellido;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Column(name = "telefono", nullable = false, length = 50)
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{8,20}$", message = "Formato de teléfono inválido")
    private String telefono;

    @Column(name = "direccion", nullable = false, length = 200)
    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;

    @Column(name = "es_prioritario", nullable = false)
    @Builder.Default
    private Boolean esPrioritario = false;

    @Column(name = "motivo_prioridad", length = 100)
    @Size(max = 100, message = "El motivo de prioridad no puede exceder 100 caracteres")
    private String motivoPrioridad;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relación con turnos
    @OneToMany(mappedBy = "ciudadano", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Turno> turnos = new ArrayList<>();



    // Métodos helper
    /**
     * Obtiene el nombre completo del ciudadano
     * @return apellido, nombre
     */
    public String getNombreCompleto() {
        System.out.println("DEBUG - getNombreCompleto(): nombre='" + nombre + "', apellido='" + apellido + "'");
        // Validar que nombre y apellido no sean null
        String apellidoSeguro = (apellido != null && !apellido.trim().isEmpty())
                ? apellido.trim() : "Sin apellido";
        String nombreSeguro = (nombre != null && !nombre.trim().isEmpty())
                ? nombre.trim() : "Sin nombre";

//        return String.format("%s, %s", apellidoSeguro, nombreSeguro);
        String resultado = String.format("%s, %s", apellidoSeguro, nombreSeguro);
        System.out.println("DEBUG - resultado: '" + resultado + "'");
        return resultado;
    }

    /**
     * Obtiene el nombre completo en formato normal
     * @return nombre apellido
     */
    public String getNombreCompletoNormal() {
        // Validar que nombre y apellido no sean null
        String nombreSeguro = (nombre != null && !nombre.trim().isEmpty())
                ? nombre.trim() : "Sin nombre";
        String apellidoSeguro = (apellido != null && !apellido.trim().isEmpty())
                ? apellido.trim() : "Sin apellido";

        return String.format("%s %s", nombreSeguro, apellidoSeguro);
    }

    /**
     * Verifica si el ciudadano tiene prioridad
     * @return true si es prioritario
     */
    public boolean tienePrioridad() {
        return esPrioritario != null && esPrioritario;
    }

    /**
     * Establece la prioridad del ciudadano
     * @param prioritario estado de prioridad
     * @param motivo motivo de la prioridad
     */
    public void establecerPrioridad(boolean prioritario, String motivo) {
        this.esPrioritario = prioritario;
        this.motivoPrioridad = prioritario ? motivo : null;
    }

    /**
     * Obtiene la cantidad de turnos del ciudadano
     * @return número de turnos
     */
    public int getCantidadTurnos() {
        return turnos != null ? turnos.size() : 0;
    }


    /**
     * Actualiza los datos básicos del ciudadano
     * @param nombre nuevo nombre
     * @param apellido nuevo apellido
     * @param telefono nuevo teléfono
     * @param direccion nueva dirección
     */
    public void actualizarDatos(String nombre, String apellido, String telefono,
                                String direccion, String observaciones) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.direccion = direccion;
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return String.format("Ciudadano{id=%d, dni='%s', nombre='%s', apellido='%s', prioritario=%s}",
                id, dni, nombre, apellido, esPrioritario);
    }


}
