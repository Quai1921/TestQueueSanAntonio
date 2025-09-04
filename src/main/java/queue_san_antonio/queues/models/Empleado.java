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
@Table(name = "empleados")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El username solo puede contener letras, números, puntos, guiones y guiones bajos")
    @EqualsAndHashCode.Include
    private String username;

    @Column(name = "password", nullable = false)
    @NotBlank(message = "La contraseña es obligatoria")
    private String password; // Almacenada con hash

    @Column(name = "nombre", nullable = false, length = 100)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String apellido;

    @Column(name = "email", unique = true, length = 150)
    @Email(message = "Formato de email inválido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;

    @Column(name = "dni", unique = true, length = 20)
    @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener entre 7 y 8 dígitos")
    private String dni;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    @Builder.Default
    private RolEmpleado rol = RolEmpleado.OPERADOR;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private Sector sector; // Sector al que pertenece (null para admins)

    @OneToMany(mappedBy = "responsable", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Sector> sectoresResponsable = new ArrayList<>(); // Sectores que administra

    @OneToMany(mappedBy = "empleadoAtencion", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Turno> turnosAtendidos = new ArrayList<>();

        // Métodos helper

    /**
     * Verifica si el empleado está activo
     * @return true si está activo
     */
    public boolean puedeAcceder() {
        return activo != null && activo;
    }

    /**
     * Verifica si es administrador general
     * @return true si es admin
     */
    public boolean esAdministrador() {
        return rol == RolEmpleado.ADMIN;
    }

    /**
     * Verifica si es responsable de algún sector
     * @return true si es responsable
     */
    public boolean esResponsable() {
        return rol == RolEmpleado.RESPONSABLE_SECTOR;
    }

    /**
     * Verifica si es operador
     * @return true si es operador
     */
    public boolean esOperador() {
        return rol == RolEmpleado.OPERADOR;
    }

    /**
     * Obtiene el nombre completo del empleado
     * @return apellido, nombre
     */
    public String getNombreCompleto() {
        return String.format("%s, %s", apellido.trim(), nombre.trim());
    }

    /**
     * Obtiene el nombre completo en formato normal
     * @return nombre apellido
     */
    public String getNombreCompletoNormal() {
        return String.format("%s %s", nombre.trim(), apellido.trim());
    }

    /**
     * Verifica si pertenece a un sector específico
     * @param sectorId ID del sector
     * @return true si pertenece al sector
     */
    public boolean perteneceASector(Long sectorId) {
        return sector != null && sector.getId().equals(sectorId);
    }

    /**
     * Verifica si es responsable de un sector específico
     * @param sectorId ID del sector
     * @return true si es responsable del sector
     */
    public boolean esResponsableDeSector(Long sectorId) {
        if (sectoresResponsable == null) return false;
        return sectoresResponsable.stream()
                .anyMatch(s -> s.getId().equals(sectorId));
    }

    /**
     * Registra un acceso exitoso
     */
    public void registrarAccesoExitoso() {
        this.ultimoAcceso = LocalDateTime.now();
    }

    /**
     * Cambia la contraseña del empleado
     * @param nuevaPassword nueva contraseña (ya hasheada)
     */
    public void cambiarPassword(String nuevaPassword) {
        this.password = nuevaPassword;
    }

    /**
     * Asigna el empleado a un sector
     * @param nuevoSector sector al que asignar
     */
    public void asignarASector(Sector nuevoSector) {
        this.sector = nuevoSector;
    }

    /**
     * Actualiza los datos básicos del empleado
     * @param nombre nuevo nombre
     * @param apellido nuevo apellido
     * @param email nuevo email
     * @param dni nuevo DNI
     */
    public void actualizarDatos(String nombre, String apellido, String email, String dni) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.dni = dni;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    /**
     * Obtiene la cantidad de turnos atendidos
     * @return número de turnos atendidos
     */
    public int getCantidadTurnosAtendidos() {
        return turnosAtendidos != null ? turnosAtendidos.size() : 0;
    }

    /**
     * Obtiene la lista de sectores donde es responsable
     * @return lista de sectores responsables
     */
    public List<Sector> getSectoresResponsable() {
        return sectoresResponsable != null ? sectoresResponsable : new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("Empleado{id=%d, username='%s', nombre='%s', apellido='%s', rol=%s, activo=%s}",
                id, username, nombre, apellido, rol, activo);
    }


}

