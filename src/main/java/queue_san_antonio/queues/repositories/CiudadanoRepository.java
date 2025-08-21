package queue_san_antonio.queues.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import queue_san_antonio.queues.models.Ciudadano;

import java.util.List;
import java.util.Optional;

@Repository
public interface CiudadanoRepository extends JpaRepository<Ciudadano, Long> {

    // Búsqueda por DNI (para verificar si existe y cargar datos)
    Optional<Ciudadano> findByDni(String dni);

    // Búsqueda por apellido (para autocompletar)
    List<Ciudadano> findByApellidoContainingIgnoreCase(String apellido);

    // Búsqueda por DNI o apellido (para el formulario de turno)
    @Query("SELECT c FROM Ciudadano c WHERE " + "(:dni IS NULL OR :dni = '' OR c.dni = :dni) AND " + "(:apellido IS NULL OR :apellido = '' OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :apellido, '%')))")
    List<Ciudadano> findByDniOrApellido(@Param("dni") String dni, @Param("apellido") String apellido);

    // Verificar si DNI ya existe
    boolean existsByDni(String dni);

    List<Ciudadano> findAllByOrderByApellidoAscNombreAsc();

    // Búsqueda por apellido normalizado (sin tildes)
    @Query("SELECT c FROM Ciudadano c WHERE " + "LOWER(FUNCTION('REGEXP_REPLACE', " + "FUNCTION('REGEXP_REPLACE', " + "FUNCTION('REGEXP_REPLACE', " + "FUNCTION('REGEXP_REPLACE', " + "FUNCTION('REGEXP_REPLACE', LOWER(c.apellido), '[áàäâã]', 'a'), " + "'[éèëê]', 'e'), '[íìïî]', 'i'), '[óòöôõ]', 'o'), '[úùüû]', 'u')) " + "LIKE LOWER(CONCAT('%', :apellidoNormalizado, '%'))")
    List<Ciudadano> findByApellidoNormalizado(@Param("apellidoNormalizado") String apellidoNormalizado);

    // Búsqueda combinada con apellido normalizado
    @Query("SELECT c FROM Ciudadano c WHERE " + "(:dni IS NULL OR :dni = '' OR c.dni = :dni) AND " + "(:apellidoNormalizado IS NULL OR :apellidoNormalizado = '' OR " + "LOWER(FUNCTION('REGEXP_REPLACE', " + "FUNCTION('REGEXP_REPLACE', " + "FUNCTION('REGEXP_REPLACE', " + "FUNCTION('REGEXP_REPLACE', " + "FUNCTION('REGEXP_REPLACE', LOWER(c.apellido), '[áàäâã]', 'a'), " + "'[éèëê]', 'e'), '[íìïî]', 'i'), '[óòöôõ]', 'o'), '[úùüû]', 'u')) " + "LIKE LOWER(CONCAT('%', :apellidoNormalizado, '%')))")
    List<Ciudadano> findByDniOrApellidoNormalizado(@Param("dni") String dni, @Param("apellidoNormalizado") String apellidoNormalizado);



}
