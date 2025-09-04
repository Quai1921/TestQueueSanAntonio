package queue_san_antonio.queues.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import queue_san_antonio.queues.models.Ciudadano;
import queue_san_antonio.queues.repositories.CiudadanoRepository;
import queue_san_antonio.queues.services.CiudadanoService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CiudadanoServiceImpl implements CiudadanoService {

    private final CiudadanoRepository ciudadanoRepository;

    @Override
    public Ciudadano guardar(Ciudadano ciudadano) {
        log.debug("Guardando ciudadano: {}", ciudadano.getDni());

        // Validación de DNI duplicado para ciudadanos nuevos
        if (ciudadano.getId() == null && existePorDni(ciudadano.getDni())) {
            throw new IllegalArgumentException("Ya existe un ciudadano con DNI: " + ciudadano.getDni());
        }

        return ciudadanoRepository.save(ciudadano);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ciudadano> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return ciudadanoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ciudadano> buscarPorDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return Optional.empty();
        }
        return ciudadanoRepository.findByDni(dni.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ciudadano> listarTodos() {
        log.debug("Listando todos los ciudadanos");

        // Ordenar por apellido, luego por nombre para mejor UX
        return ciudadanoRepository.findAllByOrderByApellidoAscNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ciudadano> buscarPorDniOApellido(String dni, String apellido) {
        if ((dni == null || dni.trim().isEmpty()) &&
                (apellido == null || apellido.trim().isEmpty())) {
            return List.of();
        }

        String dniLimpio = dni != null ? dni.trim() : "";
        String apellidoLimpio = apellido != null ? apellido.trim() : "";

        String apellidoNormalizado = normalizarTexto(apellidoLimpio);

        return ciudadanoRepository.findByDniOrApellidoNormalizado(dniLimpio, apellidoNormalizado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ciudadano> buscarPorApellido(String apellido) {
        if (apellido == null || apellido.trim().isEmpty()) {
            return List.of();
        }

        String apellidoNormalizado = normalizarTexto(apellido.trim());
        return ciudadanoRepository.findByApellidoNormalizado(apellidoNormalizado);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return false;
        }
        return ciudadanoRepository.existsByDni(dni.trim());
    }

    @Override
    public Ciudadano crearOActualizar(String dni, String nombre, String apellido, String telefono, String direccion, String observaciones) {
        // Validar parámetros obligatorios
        validarDatosObligatorios(dni, nombre, apellido, telefono, direccion);

        // Limpiar datos
        String dniLimpio = dni.trim();
        String nombreLimpio = nombre.trim();
        String apellidoLimpio = apellido.trim();
        String telefonoLimpio = telefono.trim();
        String direccionLimpia = direccion.trim();
        String observacionesLimpias = observaciones != null ? observaciones.trim() : null;

        log.info("Creando/actualizando ciudadano con DNI: {}", dniLimpio);

        // Buscar si ya existe
        Optional<Ciudadano> ciudadanoExistente = buscarPorDni(dniLimpio);

        if (ciudadanoExistente.isPresent()) {
            // Actualizar datos existentes
            Ciudadano ciudadano = ciudadanoExistente.get();
            ciudadano.actualizarDatos(nombreLimpio, apellidoLimpio, telefonoLimpio, direccionLimpia, observacionesLimpias);

            log.debug("Actualizando ciudadano existente: {}", dniLimpio);
            return guardar(ciudadano);
        } else {
            // Crear nuevo ciudadano
            Ciudadano nuevoCiudadano = Ciudadano.builder()
                    .dni(dniLimpio)
                    .nombre(nombreLimpio)
                    .apellido(apellidoLimpio)
                    .telefono(telefonoLimpio)
                    .direccion(direccionLimpia)
                    .observaciones(observacionesLimpias)
                    .esPrioritario(false)
                    .build();

            log.debug("Creando nuevo ciudadano: {}", dniLimpio);
            return guardar(nuevoCiudadano);
        }
    }

    @Override
    public void establecerPrioridad(Long ciudadanoId, boolean prioritario, String motivo) {
        if (ciudadanoId == null) {
            throw new IllegalArgumentException("El ID del ciudadano no puede ser nulo");
        }

        log.info("Estableciendo prioridad {} para ciudadano ID: {}", prioritario, ciudadanoId);

        Ciudadano ciudadano = buscarPorId(ciudadanoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ciudadano con ID: " + ciudadanoId));

        // Validar motivo si es prioritario
        if (prioritario && (motivo == null || motivo.trim().isEmpty())) {
            throw new IllegalArgumentException("Debe especificar un motivo para establecer prioridad");
        }

        ciudadano.establecerPrioridad(prioritario, motivo != null ? motivo.trim() : null);
        guardar(ciudadano);

        log.debug("Prioridad establecida para ciudadano {}: {} - {}",
                ciudadano.getDni(), prioritario, motivo);

    }

//    Valida que todos los datos obligatorios estén presentes
    private void validarDatosObligatorios(String dni, String nombre, String apellido,
                                          String telefono, String direccion) {
        if (dni == null || dni.trim().isEmpty()) {
            throw new IllegalArgumentException("Por favor ingrese el DNI del ciudadano");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Por favor ingrese el nombre del ciudadano");
        }
        if (apellido == null || apellido.trim().isEmpty()) {
            throw new IllegalArgumentException("Por favor ingrese el apellido del ciudadano");
        }
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new IllegalArgumentException("Por favor ingrese el teléfono del ciudadano");
        }
        if (direccion == null || direccion.trim().isEmpty()) {
            throw new IllegalArgumentException("Por favor ingrese la dirección del ciudadano");
        }

        // Validar formato DNI (7-8 dígitos)
        String dniLimpio = dni.trim();
        if (!dniLimpio.matches("^[0-9]{7,8}$")) {
            throw new IllegalArgumentException("El DNI debe tener entre 7 y 8 dígitos");
        }
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "";
        }

        String textoLimpio = texto.trim();

        // Normalizar y remover acentos/tildes
        return java.text.Normalizer.normalize(textoLimpio, java.text.Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase();
    }


}
