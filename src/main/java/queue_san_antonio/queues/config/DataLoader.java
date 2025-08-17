package queue_san_antonio.queues.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import queue_san_antonio.queues.models.*;
import queue_san_antonio.queues.repositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Carga datos de prueba al iniciar la aplicación
 * Solo se ejecuta si no existen datos previos
 * Versión completa para testing exhaustivo de endpoints
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final SectorRepository sectorRepository;
    private final EmpleadoRepository empleadoRepository;
    private final CiudadanoRepository ciudadanoRepository;
    private final TurnoRepository turnoRepository;
    private final ConfiguracionPantallaRepository configuracionPantallaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando carga de datos de prueba completos...");

        if (empleadoRepository.count() == 0) {
            crearDatosPruebaCompletos();
            log.info("Datos de prueba completos cargados exitosamente");
        } else {
            log.info("Ya existen datos en la base de datos, omitiendo carga de datos de prueba");
        }
    }

    private void crearDatosPruebaCompletos() {
        log.info("=== INICIANDO CARGA MASIVA DE DATOS DE PRUEBA ===");

        // 1. CREAR SECTORES COMPLETOS
        log.debug("Creando sectores completos...");

        Sector intendencia = Sector.builder()
                .codigo("INT")
                .nombre("Intendencia")
                .descripcion("Atención al público - Intendente y secretarios")
                .tipoSector(TipoSector.ESPECIAL)
                .requiereCitaPrevia(true)
                .activo(true)
                .capacidadMaxima(1)
                .tiempoEstimadoAtencion(30)
                .color("#FF6B6B")
                .ordenVisualizacion(1)
                .build();
        intendencia = sectorRepository.save(intendencia);

        Sector contabilidad = Sector.builder()
                .codigo("CON")
                .nombre("Contabilidad")
                .descripcion("Gestión contable, financiera y presupuestaria")
                .tipoSector(TipoSector.NORMAL)
                .requiereCitaPrevia(false)
                .activo(true)
                .capacidadMaxima(2)
                .tiempoEstimadoAtencion(15)
                .color("#4ECDC4")
                .ordenVisualizacion(2)
                .build();
        contabilidad = sectorRepository.save(contabilidad);

        Sector rentas = Sector.builder()
                .codigo("REN")
                .nombre("Rentas")
                .descripcion("Impuestos, tasas municipales y cobranzas")
                .tipoSector(TipoSector.NORMAL)
                .requiereCitaPrevia(false)
                .activo(true)
                .capacidadMaxima(4)
                .tiempoEstimadoAtencion(10)
                .color("#45B7D1")
                .ordenVisualizacion(3)
                .build();
        rentas = sectorRepository.save(rentas);

        Sector obras = Sector.builder()
                .codigo("OBR")
                .nombre("Obras Públicas")
                .descripcion("Permisos de construcción, obras y planificación urbana")
                .tipoSector(TipoSector.NORMAL)
                .requiereCitaPrevia(false)
                .activo(true)
                .capacidadMaxima(3)
                .tiempoEstimadoAtencion(20)
                .color("#F7DC6F")
                .ordenVisualizacion(4)
                .build();
        obras = sectorRepository.save(obras);

        Sector salud = Sector.builder()
                .codigo("SAL")
                .nombre("Salud")
                .descripcion("Atención sanitaria, permisos de salud y bromatología")
                .tipoSector(TipoSector.ESPECIAL)
                .requiereCitaPrevia(true)
                .activo(true)
                .capacidadMaxima(2)
                .tiempoEstimadoAtencion(25)
                .color("#96CEB4")
                .ordenVisualizacion(5)
                .build();
        salud = sectorRepository.save(salud);

        Sector transito = Sector.builder()
                .codigo("TRA")
                .nombre("Tránsito")
                .descripcion("Licencias de conducir, infracciones y control vehicular")
                .tipoSector(TipoSector.NORMAL)
                .requiereCitaPrevia(false)
                .activo(true)
                .capacidadMaxima(3)
                .tiempoEstimadoAtencion(12)
                .color("#FFEAA7")
                .ordenVisualizacion(6)
                .build();
        transito = sectorRepository.save(transito);

        // Sector inactivo para testing
        Sector sectorInactivo = Sector.builder()
                .codigo("INA")
                .nombre("Sector Inactivo")
                .descripcion("Sector desactivado para testing")
                .tipoSector(TipoSector.NORMAL)
                .requiereCitaPrevia(false)
                .activo(false)
                .capacidadMaxima(1)
                .tiempoEstimadoAtencion(15)
                .color("#CCCCCC")
                .ordenVisualizacion(99)
                .build();
        sectorInactivo = sectorRepository.save(sectorInactivo);

        // 2. CREAR EMPLEADOS COMPLETOS
        log.debug("Creando empleados completos...");

        // === ADMINISTRADORES ===
        Empleado superAdmin = Empleado.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .nombre("Juan Carlos")
                .apellido("García")
                .email("admin@sanantonio.gov.ar")
                .dni("12345678")
                .rol(RolEmpleado.ADMIN)
                .activo(true)
                .sector(null)
                .build();
        superAdmin = empleadoRepository.save(superAdmin);

        Empleado adminSistemas = Empleado.builder()
                .username("admin_sys")
                .password(passwordEncoder.encode("sistemas123"))
                .nombre("Ana Clara")
                .apellido("Mendoza")
                .email("sistemas@sanantonio.gov.ar")
                .dni("11223344")
                .rol(RolEmpleado.ADMIN)
                .activo(true)
                .sector(null)
                .build();
        adminSistemas = empleadoRepository.save(adminSistemas);

        // === RESPONSABLES DE SECTOR ===
        Empleado respIntendencia = Empleado.builder()
                .username("resp_int")
                .password(passwordEncoder.encode("resp123"))
                .nombre("María Elena")
                .apellido("López")
                .email("mlopez@sanantonio.gov.ar")
                .dni("23456789")
                .rol(RolEmpleado.RESPONSABLE_SECTOR)
                .activo(true)
                .sector(intendencia)
                .build();
        respIntendencia = empleadoRepository.save(respIntendencia);
        intendencia.setResponsable(respIntendencia);
        sectorRepository.save(intendencia);

        Empleado respContabilidad = Empleado.builder()
                .username("resp_con")
                .password(passwordEncoder.encode("cont123"))
                .nombre("Carlos Alberto")
                .apellido("Rodríguez")
                .email("crodriguez@sanantonio.gov.ar")
                .dni("34567890")
                .rol(RolEmpleado.RESPONSABLE_SECTOR)
                .activo(true)
                .sector(contabilidad)
                .build();
        respContabilidad = empleadoRepository.save(respContabilidad);
        contabilidad.setResponsable(respContabilidad);
        sectorRepository.save(contabilidad);

        Empleado respRentas = Empleado.builder()
                .username("resp_ren")
                .password(passwordEncoder.encode("rentas123"))
                .nombre("Patricia Susana")
                .apellido("Herrera")
                .email("pherrera@sanantonio.gov.ar")
                .dni("4455677")
                .rol(RolEmpleado.RESPONSABLE_SECTOR)
                .activo(true)
                .sector(rentas)
                .build();
        respRentas = empleadoRepository.save(respRentas);
        rentas.setResponsable(respRentas);
        sectorRepository.save(rentas);

        Empleado respObras = Empleado.builder()
                .username("resp_obr")
                .password(passwordEncoder.encode("obras123"))
                .nombre("Miguel Ángel")
                .apellido("Vargas")
                .email("mvargas@sanantonio.gov.ar")
                .dni("55667788")
                .rol(RolEmpleado.RESPONSABLE_SECTOR)
                .activo(true)
                .sector(obras)
                .build();
        respObras = empleadoRepository.save(respObras);
        obras.setResponsable(respObras);
        sectorRepository.save(obras);

        Empleado respSalud = Empleado.builder()
                .username("resp_sal")
                .password(passwordEncoder.encode("salud123"))
                .nombre("Laura Beatriz")
                .apellido("Campos")
                .email("lcampos@sanantonio.gov.ar")
                .dni("66778899")
                .rol(RolEmpleado.RESPONSABLE_SECTOR)
                .activo(true)
                .sector(salud)
                .build();
        respSalud = empleadoRepository.save(respSalud);
        salud.setResponsable(respSalud);
        sectorRepository.save(salud);

        Empleado respTransito = Empleado.builder()
                .username("resp_tra")
                .password(passwordEncoder.encode("transito123"))
                .nombre("Fernando José")
                .apellido("Moreno")
                .email("fmoreno@sanantonio.gov.ar")
                .dni("77889900")
                .rol(RolEmpleado.RESPONSABLE_SECTOR)
                .activo(true)
                .sector(transito)
                .build();
        respTransito = empleadoRepository.save(respTransito);
        transito.setResponsable(respTransito);
        sectorRepository.save(transito);

        // === OPERADORES ===
        // Operadores de Rentas (4)
        Empleado opRentas1 = Empleado.builder()
                .username("op_ren1")
                .password(passwordEncoder.encode("op1234"))
                .nombre("Ana Patricia")
                .apellido("Martínez")
                .email("amartinez@sanantonio.gov.ar")
                .dni("45678901")
                .rol(RolEmpleado.OPERADOR)
                .activo(true)
                .sector(rentas)
                .build();
        empleadoRepository.save(opRentas1);

        Empleado opRentas2 = Empleado.builder()
                .username("op_ren2")
                .password(passwordEncoder.encode("op1234"))
                .nombre("Roberto Daniel")
                .apellido("González")
                .email("rgonzalez@sanantonio.gov.ar")
                .dni("56789012")
                .rol(RolEmpleado.OPERADOR)
                .activo(true)
                .sector(rentas)
                .build();
        empleadoRepository.save(opRentas2);

        Empleado opRentas3 = Empleado.builder()
                .username("op_ren3")
                .password(passwordEncoder.encode("op1234"))
                .nombre("Claudia Mónica")
                .apellido("Torres")
                .email("ctorres@sanantonio.gov.ar")
                .dni("33445566")
                .rol(RolEmpleado.OPERADOR)
                .activo(true)
                .sector(rentas)
                .build();
        empleadoRepository.save(opRentas3);

        Empleado opRentas4 = Empleado.builder()
                .username("op_ren4")
                .password(passwordEncoder.encode("op1234"))
                .nombre("Diego Fernando")
                .apellido("Castro")
                .email("dcastro@sanantonio.gov.ar")
                .dni("44556677")
                .rol(RolEmpleado.OPERADOR)
                .activo(true)
                .sector(rentas)
                .build();
        empleadoRepository.save(opRentas4);

        // Operadores de Obras (3)
        Empleado opObras1 = Empleado.builder()
                .username("op_obr1")
                .password(passwordEncoder.encode("op1234"))
                .nombre("Lucía Fernanda")
                .apellido("Pérez")
                .email("lperez@sanantonio.gov.ar")
                .dni("67890123")
                .rol(RolEmpleado.OPERADOR)
                .activo(true)
                .sector(obras)
                .build();
        empleadoRepository.save(opObras1);

        Empleado opObras2 = Empleado.builder()
                .username("op_obr2")
                .password(passwordEncoder.encode("op1234"))
                .nombre("Sebastián Luis")
                .apellido("Ramírez")
                .email("sramirez@sanantonio.gov.ar")
                .dni("55443322")
                .rol(RolEmpleado.OPERADOR)
                .activo(true)
                .sector(obras)
                .build();
        empleadoRepository.save(opObras2);

        Empleado opObras3 = Empleado.builder()
                .username("op_obr3")
                .password(passwordEncoder.encode("op1234"))
                .nombre("Valeria Soledad")
                .apellido("Díaz")
                .email("vdiaz@sanantonio.gov.ar")
                .dni("66554433")
                .rol(RolEmpleado.OPERADOR)
                .activo(true)
                .sector(obras)
                .build();
        empleadoRepository.save(opObras3);

        // Operadores de Contabilidad (2)
        Empleado opCont1 = Empleado.builder()
                .username("op_con1")
                .password(passwordEncoder.encode("op1234"))
                .nombre("Mariana Isabel")
                .apellido("Sánchez")
                .email("msanchez@sanantonio.gov.ar")
                .dni("77665544")
                .rol(RolEmpleado.OPERADOR)
                .activo(true)
                .sector(contabilidad)
                .build();
        empleadoRepository.save(opCont1);

        Empleado opCont2 = Empleado.builder()
                .username("op_con2")
                .password(passwordEncoder.encode("op1234"))
                .nombre("Alejandro Raúl")
                .apellido("Medina")
                .email("amedina@sanantonio.gov.ar")
                .dni("88776655")
                .rol(RolEmpleado.OPERADOR)
                .activo(true)
                .sector(contabilidad)
                .build();
        empleadoRepository.save(opCont2);

        // Operadores de Salud (2)
        Empleado opSalud1 = Empleado.builder()
                .username("op_sal1")
                .password(passwordEncoder.encode("op1234"))
                .nombre("Carmen Rosa")
                .apellido("Flores")
                .email("cflores@sanantonio.gov.ar")
                .dni("99887766")
                .rol(RolEmpleado.OPERADOR)
                .activo(true)
                .sector(salud)
                .build();
        empleadoRepository.save(opSalud1);

        Empleado opSalud2 = Empleado.builder()
                .username("op_sal2")
                .password(passwordEncoder.encode("op1234"))
                .nombre("Javier Eduardo")
                .apellido("Ríos")
                .email("jrios@sanantonio.gov.ar")
                .dni("11998877")
                .rol(RolEmpleado.OPERADOR)
                .activo(true)
                .sector(salud)
                .build();
        empleadoRepository.save(opSalud2);

        // Operadores de Tránsito (3)
        Empleado opTransito1 = Empleado.builder()
                .username("op_tra1")
                .password(passwordEncoder.encode("op1234"))
                .nombre("Sandra Viviana")
                .apellido("Ruiz")
                .email("sruiz@sanantonio.gov.ar")
                .dni("22109988")
                .rol(RolEmpleado.OPERADOR)
                .activo(true)
                .sector(transito)
                .build();
        empleadoRepository.save(opTransito1);

        Empleado opTransito2 = Empleado.builder()
                .username("op_tra2")
                .password(passwordEncoder.encode("op1234"))
                .nombre("Gabriel Andrés")
                .apellido("Silva")
                .email("gsilva@sanantonio.gov.ar")
                .dni("33210099")
                .rol(RolEmpleado.OPERADOR)
                .activo(true)
                .sector(transito)
                .build();
        empleadoRepository.save(opTransito2);

        Empleado opTransito3 = Empleado.builder()
                .username("op_tra3")
                .password(passwordEncoder.encode("op1234"))
                .nombre("Mónica Alejandra")
                .apellido("Vega")
                .email("mvega@sanantonio.gov.ar")
                .dni("44321100")
                .rol(RolEmpleado.OPERADOR)
                .activo(true)
                .sector(transito)
                .build();
        empleadoRepository.save(opTransito3);

        // === EMPLEADOS INACTIVOS (para testing) ===
        Empleado empleadoInactivo1 = Empleado.builder()
                .username("inactivo1")
                .password(passwordEncoder.encode("test123"))
                .nombre("Usuario")
                .apellido("Inactivo Uno")
                .email("inactivo1@sanantonio.gov.ar")
                .dni("78901234")
                .rol(RolEmpleado.OPERADOR)
                .activo(false)
                .sector(rentas)
                .build();
        empleadoRepository.save(empleadoInactivo1);

        Empleado respInactivo = Empleado.builder()
                .username("resp_inactivo")
                .password(passwordEncoder.encode("test123"))
                .nombre("Responsable")
                .apellido("Inactivo")
                .email("respinactivo@sanantonio.gov.ar")
                .dni("87654321")
                .rol(RolEmpleado.RESPONSABLE_SECTOR)
                .activo(false)
                .sector(sectorInactivo)
                .build();
        empleadoRepository.save(respInactivo);

        // 3. CREAR CIUDADANOS MASIVOS
        log.debug("Creando ciudadanos de prueba masivos...");
        crearCiudadanosMasivos();

        // 4. CREAR TURNOS DE PRUEBA
        log.debug("Creando turnos de prueba...");
        crearTurnosPrueba(rentas, obras, contabilidad, salud, transito);

        // 5. CREAR CONFIGURACIÓN DE PANTALLA
        log.debug("Creando configuración de pantalla...");
        ConfiguracionPantalla configPantalla = ConfiguracionPantalla.builder()
                .nombre("Configuración Principal")
                .tiempoMensaje(10)
                .tiempoTurno(5)
                .sonidoActivo(true)
                .volumenSonido(70)
                .animacionesActivas(true)
                .temaColor("default")
                .mostrarLogo(true)
                .textoEncabezado("SISTEMA DE TURNOS - MUNICIPALIDAD DE SAN ANTONIO")
                .activo(true)
                .build();
        configuracionPantallaRepository.save(configPantalla);

        // LOGGING DE RESUMEN COMPLETO
        logResumenCompleto();
    }

    private void crearCiudadanosMasivos() {
        String[][] ciudadanosData = {
                // DNI, Nombre, Apellido, Teléfono, Dirección, Prioritario, Motivo
                {"20123456", "Pedro José", "Fernández", "0351-4567890", "Av. San Martín 123", "false", ""},
                {"30987654", "Rosa María", "Silva", "0351-9876543", "Calle Belgrano 456", "true", "Adulto mayor"},
                {"40111222", "Luis Alberto", "Morales", "0351-1112223", "Barrio Centro 789", "false", ""},
                {"25333444", "Elena Cristina", "Vázquez", "0351-3334445", "Av. Córdoba 234", "true", "Embarazada"},
                {"35555666", "Carlos Enrique", "Mendoza", "0351-5556667", "Calle Rivadavia 567", "false", ""},
                {"28777888", "María del Carmen", "Gutiérrez", "0351-7778889", "Barrio Norte 890", "true", "Discapacidad"},
                {"32999000", "Jorge Alejandro", "Romero", "0351-9990001", "Av. Libertad 345", "false", ""},
                {"26111222", "Susana Beatriz", "Castro", "0351-1112224", "Calle Mitre 678", "false", ""},
                {"38333444", "Ricardo Fabián", "Herrera", "0351-3334446", "Barrio Sur 901", "false", ""},
                {"41555666", "Graciela Noemí", "Peralta", "0351-5556668", "Av. Independencia 456", "true", "Adulto mayor"},
                {"29777888", "Héctor Raúl", "Jiménez", "0351-7778890", "Calle Sarmiento 789", "false", ""},
                {"33999000", "Patricia Mónica", "Ortiz", "0351-9990002", "Barrio Oeste 012", "false", ""},
                {"27111222", "Daniel Osvaldo", "Cabrera", "0351-1112225", "Av. Mayo 567", "false", ""},
                {"39333444", "Liliana Rosa", "Aguilar", "0351-3334447", "Calle Tucumán 890", "true", "Madre con menor"},
                {"42555666", "Osvaldo Miguel", "Vargas", "0351-5556669", "Barrio Este 123", "false", ""},
                {"31777888", "Carmen Elisa", "Molina", "0351-7778891", "Av. Colón 456", "false", ""},
                {"24999000", "Alberto José", "Ríos", "0351-9990003", "Calle Chacabuco 789", "false", ""},
                {"36111222", "Marta Estela", "Fuentes", "0351-1112226", "Barrio Centro Sur 234", "true", "Discapacidad"},
                {"43333444", "Fernando Luis", "Sosa", "0351-3334448", "Av. Pellegrini 567", "false", ""},
                {"28555666", "Norma Beatriz", "Paz", "0351-5556670", "Calle 9 de Julio 890", "false", ""},
                {"37777888", "Rubén Darío", "Luna", "0351-7778892", "Barrio Villa Nueva 345", "false", ""},
                {"44999000", "Teresa del Valle", "Campos", "0351-9990004", "Av. Fuerza Aérea 678", "true", "Embarazada"},
                {"30111222", "Guillermo Antonio", "Blanco", "0351-1112227", "Calle Caseros 901", "false", ""},
                {"41333444", "Alicia Mercedes", "Morales", "0351-3334449", "Barrio Alberdi 456", "false", ""},
                {"26555666", "Raúl Eduardo", "Domínguez", "0351-5556671", "Av. Rafael Núñez 789", "false", ""}
        };

        for (String[] data : ciudadanosData) {
            Ciudadano ciudadano = Ciudadano.builder()
                    .dni(data[0])
                    .nombre(data[1])
                    .apellido(data[2])
                    .telefono(data[3])
                    .direccion(data[4])
                    .esPrioritario(Boolean.parseBoolean(data[5]))
                    .motivoPrioridad(data[6].isEmpty() ? null : data[6])
                    .build();
            ciudadanoRepository.save(ciudadano);
        }
    }

    private void crearTurnosPrueba(Sector rentas, Sector obras, Sector contabilidad, Sector salud, Sector transito) {
        // Obtener algunos ciudadanos para los turnos
        var ciudadanos = ciudadanoRepository.findAll();

        LocalDate hoy = LocalDate.now();
        LocalDate ayer = hoy.minusDays(1);
        LocalDate manana = hoy.plusDays(1);

        // Turnos de HOY - GENERADOS (en espera)
        crearTurno(rentas, ciudadanos.get(0), hoy, LocalTime.of(9, 0), EstadoTurno.GENERADO, TipoTurno.NORMAL, 0);
        crearTurno(rentas, ciudadanos.get(2), hoy, LocalTime.of(9, 30), EstadoTurno.GENERADO, TipoTurno.NORMAL, 0);
        crearTurno(obras, ciudadanos.get(3), hoy, LocalTime.of(9, 5), EstadoTurno.GENERADO, TipoTurno.NORMAL, 0);
        crearTurno(obras, ciudadanos.get(4), hoy, LocalTime.of(9, 25), EstadoTurno.GENERADO, TipoTurno.NORMAL, 0);
        crearTurno(contabilidad, ciudadanos.get(5), hoy, LocalTime.of(10, 0), EstadoTurno.GENERADO, TipoTurno.NORMAL, 0);
        crearTurno(transito, ciudadanos.get(6), hoy, LocalTime.of(10, 15), EstadoTurno.GENERADO, TipoTurno.NORMAL, 0);
        crearTurno(transito, ciudadanos.get(7), hoy, LocalTime.of(10, 30), EstadoTurno.GENERADO, TipoTurno.NORMAL, 0);

        // Turnos de HOY - LLAMADOS
        crearTurno(rentas, ciudadanos.get(8), hoy, LocalTime.of(8, 30), EstadoTurno.LLAMADO, TipoTurno.NORMAL, 0);
        crearTurno(obras, ciudadanos.get(9), hoy, LocalTime.of(8, 35), EstadoTurno.LLAMADO, TipoTurno.NORMAL, 0);

        // Turnos de HOY - EN ATENCIÓN
        crearTurnoEnAtencion(rentas, ciudadanos.get(10), hoy, LocalTime.of(8, 45), TipoTurno.NORMAL, 0);
        crearTurnoEnAtencion(obras, ciudadanos.get(11), hoy, LocalTime.of(8, 50), TipoTurno.NORMAL, 0);

        // Turnos de AYER - FINALIZADOS
        crearTurnoFinalizado(rentas, ciudadanos.get(12), ayer, LocalTime.of(9, 0), TipoTurno.NORMAL, 0);
        crearTurnoFinalizado(rentas, ciudadanos.get(13), ayer, LocalTime.of(9, 15), TipoTurno.NORMAL, 0);
        crearTurnoFinalizado(rentas, ciudadanos.get(14), ayer, LocalTime.of(9, 30), TipoTurno.NORMAL, 0);
        crearTurnoFinalizado(obras, ciudadanos.get(15), ayer, LocalTime.of(10, 0), TipoTurno.NORMAL, 0);
        crearTurnoFinalizado(contabilidad, ciudadanos.get(16), ayer, LocalTime.of(11, 0), TipoTurno.NORMAL, 0);
        crearTurnoFinalizado(salud, ciudadanos.get(17), ayer, LocalTime.of(14, 30), TipoTurno.ESPECIAL, 0);

        // Turnos de AYER - CANCELADOS
        crearTurno(rentas, ciudadanos.get(18), ayer, LocalTime.of(15, 0), EstadoTurno.CANCELADO, TipoTurno.NORMAL, 0);
        crearTurno(transito, ciudadanos.get(19), ayer, LocalTime.of(16, 30), EstadoTurno.CANCELADO, TipoTurno.NORMAL, 0);

        // Turnos de AYER - AUSENTES
        crearTurno(obras, ciudadanos.get(20), ayer, LocalTime.of(17, 0), EstadoTurno.AUSENTE, TipoTurno.NORMAL, 0);
        crearTurno(salud, ciudadanos.get(21), ayer, LocalTime.of(17, 30), EstadoTurno.AUSENTE, TipoTurno.ESPECIAL, 0);

        // Turnos FUTUROS - ESPECIALES (citas previas)
        crearTurnoCitaPrevia(salud, ciudadanos.get(22), manana, LocalTime.of(9, 0));
        crearTurnoCitaPrevia(salud, ciudadanos.get(23), manana, LocalTime.of(10, 0));
        crearTurnoCitaPrevia(salud, ciudadanos.get(24), manana.plusDays(1), LocalTime.of(14, 30));

        // Turnos prioritarios de HOY
        crearTurnoPrioritario(rentas, ciudadanos.get(1), hoy, LocalTime.of(8, 30), EstadoTurno.GENERADO, 1); // Rosa María (adulto mayor)
        crearTurnoPrioritario(transito, ciudadanos.get(3), hoy, LocalTime.of(9, 0), EstadoTurno.GENERADO, 2); // Elena (embarazada)
    }

    private void crearTurno(Sector sector, Ciudadano ciudadano, LocalDate fecha, LocalTime hora,
                            EstadoTurno estado, TipoTurno tipo, Integer prioridad) {
        String codigo = generarCodigoTurno(sector);

        Turno turno = Turno.builder()
                .codigo(codigo)
                .sector(sector)
                .ciudadano(ciudadano)
                .estado(estado)
                .tipo(tipo)
                .prioridad(prioridad)
                .observaciones("Turno de prueba generado automáticamente")
                .build();

        // Simular tiempo de generación en el pasado
        turno.setFechaHoraGeneracion(fecha.atTime(hora));

        turnoRepository.save(turno);
    }

//    private void crearTurnoEnAtencion(Sector sector, Ciudadano ciudadano, LocalDate fecha, LocalTime hora,
//                                      TipoTurno tipo, Integer prioridad) {
//        String codigo = generarCodigoTurno(sector);
//
//        Turno turno = Turno.builder()
//                .codigo(codigo)
//                .sector(sector)
//                .ciudadano(ciudadano)
//                .estado(EstadoTurno.EN_ATENCION)
//                .tipo(tipo)
//                .prioridad(prioridad)
//                .observaciones("Turno de prueba en atención")
//                .build();
//
//        // Simular flujo completo: generado -> llamado -> en atención
//        LocalDateTime fechaGeneracion = fecha.atTime(hora);
//        turno.setFechaHoraGeneracion(fechaGeneracion);
//        turno.setFechaHoraLlamado(fechaGeneracion.plusMinutes(5));
//        turno.setFechaHoraAtencion(fechaGeneracion.plusMinutes(10));
//
//        turnoRepository.save(turno);
//    }

    private void crearTurnoEnAtencion(Sector sector, Ciudadano ciudadano, LocalDate fecha, LocalTime hora,
                                  TipoTurno tipo, Integer prioridad) {
    String codigo = generarCodigoTurno(sector);

    // ✅ BUSCAR UN OPERADOR DEL SECTOR
    List<Empleado> operadores = empleadoRepository.findBySectorIdAndActivoTrue(sector.getId())
            .stream()
            .filter(emp -> emp.getRol() == RolEmpleado.OPERADOR)
            .toList();

    Empleado empleadoAsignado = !operadores.isEmpty() ? operadores.get(0) : null;

    Turno turno = Turno.builder()
            .codigo(codigo)
            .sector(sector)
            .ciudadano(ciudadano)
            .estado(EstadoTurno.EN_ATENCION)
            .tipo(tipo)
            .prioridad(prioridad)
            .empleadoAtencion(empleadoAsignado)  // ✅ ASIGNAR EMPLEADO
            .observaciones("Turno de prueba en atención")
            .build();

    // Simular flujo completo: generado -> llamado -> en atención
    LocalDateTime fechaGeneracion = fecha.atTime(hora);
    turno.setFechaHoraGeneracion(fechaGeneracion);
    turno.setFechaHoraLlamado(fechaGeneracion.plusMinutes(5));
    turno.setFechaHoraAtencion(fechaGeneracion.plusMinutes(10));

        // ✅ LOG PARA DEBUG
        log.info("Turno EN_ATENCION creado: {} - Empleado: {}",
                codigo, empleadoAsignado != null ? empleadoAsignado.getUsername() : "NULL");

    turnoRepository.save(turno);
}

//    private void crearTurnoFinalizado(Sector sector, Ciudadano ciudadano, LocalDate fecha, LocalTime hora,
//                                      TipoTurno tipo, Integer prioridad) {
//        String codigo = generarCodigoTurno(sector);
//
//        Turno turno = Turno.builder()
//                .codigo(codigo)
//                .sector(sector)
//                .ciudadano(ciudadano)
//                .estado(EstadoTurno.FINALIZADO)
//                .tipo(tipo)
//                .prioridad(prioridad)
//                .observaciones("Turno de prueba finalizado")
//                .build();
//
//        // Simular flujo completo: generado -> llamado -> en atención -> finalizado
//        LocalDateTime fechaGeneracion = fecha.atTime(hora);
//        turno.setFechaHoraGeneracion(fechaGeneracion);
//        turno.setFechaHoraLlamado(fechaGeneracion.plusMinutes(5));
//        turno.setFechaHoraAtencion(fechaGeneracion.plusMinutes(10));
//        turno.setFechaHoraFinalizacion(fechaGeneracion.plusMinutes(10 + sector.getTiempoEstimadoAtencion()));
//
//        turnoRepository.save(turno);
//    }

    private void crearTurnoFinalizado(Sector sector, Ciudadano ciudadano, LocalDate fecha, LocalTime hora,
                                      TipoTurno tipo, Integer prioridad) {
        String codigo = generarCodigoTurno(sector);

        // ✅ BUSCAR UN OPERADOR DEL SECTOR
        List<Empleado> operadores = empleadoRepository.findBySectorIdAndActivoTrue(sector.getId())
                .stream()
                .filter(emp -> emp.getRol() == RolEmpleado.OPERADOR)
                .toList();

        Empleado empleadoAsignado = !operadores.isEmpty() ? operadores.get(0) : null;

        Turno turno = Turno.builder()
                .codigo(codigo)
                .sector(sector)
                .ciudadano(ciudadano)
                .estado(EstadoTurno.FINALIZADO)
                .tipo(tipo)
                .prioridad(prioridad)
                .empleadoAtencion(empleadoAsignado)  // ✅ ASIGNAR EMPLEADO
                .observaciones("Turno de prueba finalizado")
                .build();

        // Simular flujo completo: generado -> llamado -> en atención -> finalizado
        LocalDateTime fechaGeneracion = fecha.atTime(hora);
        turno.setFechaHoraGeneracion(fechaGeneracion);
        turno.setFechaHoraLlamado(fechaGeneracion.plusMinutes(5));
        turno.setFechaHoraAtencion(fechaGeneracion.plusMinutes(10));
        turno.setFechaHoraFinalizacion(fechaGeneracion.plusMinutes(10 + sector.getTiempoEstimadoAtencion()));

        turnoRepository.save(turno);
    }

//    private void crearTurnoCitaPrevia(Sector sector, Ciudadano ciudadano, LocalDate fecha, LocalTime hora) {
//        String codigo = generarCodigoTurno(sector);
//
//        Turno turno = Turno.builder()
//                .codigo(codigo)
//                .sector(sector)
//                .ciudadano(ciudadano)
//                .estado(EstadoTurno.GENERADO)
//                .tipo(TipoTurno.ESPECIAL)
//                .prioridad(0)
//                .fechaCita(fecha)
//                .horaCita(hora)
//                .observaciones("Cita previa programada")
//                .build();
//
//        turnoRepository.save(turno);
//    }


    private void crearTurnoCitaPrevia(Sector sector, Ciudadano ciudadano, LocalDate fecha, LocalTime hora) {
        String codigo = generarCodigoTurno(sector);

        Turno turno = Turno.builder()
                .codigo(codigo)
                .sector(sector)
                .ciudadano(ciudadano)
                .estado(EstadoTurno.GENERADO)
                .tipo(TipoTurno.ESPECIAL)
                .prioridad(0)
                .fechaCita(fecha)
                .horaCita(hora)
                .observaciones("Cita previa programada")
                .build();

        // ✅ ESTABLECER FECHA DE GENERACIÓN (antes faltaba esto)
        turno.setFechaHoraGeneracion(LocalDate.now().atTime(8, 0)); // Generada hoy temprano

        turnoRepository.save(turno);
    }

    private void crearTurnoPrioritario(Sector sector, Ciudadano ciudadano, LocalDate fecha, LocalTime hora,
                                       EstadoTurno estado, Integer prioridad) {
        String codigo = generarCodigoTurno(sector);

        Turno turno = Turno.builder()
                .codigo(codigo)
                .sector(sector)
                .ciudadano(ciudadano)
                .estado(estado)
                .tipo(TipoTurno.PRIORITARIO)
                .prioridad(prioridad)
                .observaciones("Turno prioritario: " + ciudadano.getMotivoPrioridad())
                .build();

        turno.setFechaHoraGeneracion(fecha.atTime(hora));
        turnoRepository.save(turno);
    }

    private String generarCodigoTurno(Sector sector) {
        // Generar código simple para testing: REN001, OBR002, etc.
        long count = turnoRepository.count() + 1;
        return String.format("%s%03d", sector.getCodigo(), count);
    }

    private void logResumenCompleto() {
        log.info("");
        log.info("=== RESUMEN COMPLETO DE DATOS CARGADOS ===");
        log.info("📊 ESTADÍSTICAS:");
        log.info("   • Sectores: {} (6 activos + 1 inactivo)", sectorRepository.count());
        log.info("   • Empleados: {} (2 admins + 6 responsables + 15 operadores + 2 inactivos)", empleadoRepository.count());
        log.info("   • Ciudadanos: {}", ciudadanoRepository.count());
        log.info("   • Turnos: {}", turnoRepository.count());
        log.info("");
        log.info("🏢 SECTORES CREADOS:");
        log.info("   • INT - Intendencia (Especial, cita previa)");
        log.info("   • CON - Contabilidad (Normal, 2 operadores)");
        log.info("   • REN - Rentas (Normal, 4 operadores)");
        log.info("   • OBR - Obras Públicas (Normal, 3 operadores)");
        log.info("   • SAL - Salud (Especial, cita previa, 2 operadores)");
        log.info("   • TRA - Tránsito (Normal, 3 operadores)");
        log.info("   • INA - Sector Inactivo (Para testing)");
        log.info("");
        log.info("👥 USUARIOS DE PRUEBA:");
        log.info("");
        log.info("🔑 ADMINISTRADORES:");
        log.info("   • admin / admin123 (Juan Carlos García)");
        log.info("   • admin_sys / sistemas123 (Ana Clara Mendoza)");
        log.info("");
        log.info("👨‍💼 RESPONSABLES DE SECTOR:");
        log.info("   • resp_int / resp123 (María Elena López - Intendencia)");
        log.info("   • resp_con / cont123 (Carlos Alberto Rodríguez - Contabilidad)");
        log.info("   • resp_ren / rentas123 (Patricia Susana Herrera - Rentas)");
        log.info("   • resp_obr / obras123 (Miguel Ángel Vargas - Obras)");
        log.info("   • resp_sal / salud123 (Laura Beatriz Campos - Salud)");
        log.info("   • resp_tra / transito123 (Fernando José Moreno - Tránsito)");
        log.info("");
        log.info("👨‍💻 OPERADORES (password: op1234):");
        log.info("   RENTAS: op_ren1, op_ren2, op_ren3, op_ren4");
        log.info("   OBRAS: op_obr1, op_obr2, op_obr3");
        log.info("   CONTABILIDAD: op_con1, op_con2");
        log.info("   SALUD: op_sal1, op_sal2");
        log.info("   TRÁNSITO: op_tra1, op_tra2, op_tra3");
        log.info("");
        log.info("❌ USUARIOS INACTIVOS (para testing):");
        log.info("   • inactivo1 / test123 (NO debe poder loguearse)");
        log.info("   • resp_inactivo / test123 (NO debe poder loguearse)");
        log.info("");
        log.info("🎫 TURNOS DE PRUEBA:");
        log.info("   • Turnos de HOY en espera y siendo atendidos");
        log.info("   • Turnos de AYER completados, cancelados y no presentados");
        log.info("   • Turnos FUTUROS programados (sectores con cita previa)");
        log.info("   • Turnos PRIORITARIOS incluidos");
        log.info("");
        log.info("👤 CIUDADANOS:");
        log.info("   • 25 ciudadanos con datos completos");
        log.info("   • Incluye ciudadanos prioritarios (adultos mayores, embarazadas, discapacitados)");
        log.info("   • Variedad de direcciones y teléfonos");
        log.info("");
        log.info("🧪 CASOS DE TESTING DISPONIBLES:");
        log.info("   ✅ Login exitoso con diferentes roles");
        log.info("   ❌ Login fallido con usuarios inactivos");
        log.info("   🏢 Sectores activos e inactivos");
        log.info("   👥 Empleados con y sin sectores asignados");
        log.info("   🎫 Turnos en diferentes estados");
        log.info("   ⭐ Ciudadanos prioritarios y normales");
        log.info("   📅 Citas previas y turnos inmediatos");
        log.info("   📊 Reportes con datos históricos");
        log.info("");
        log.info("📝 PRÓXIMOS PASOS SUGERIDOS:");
        log.info("   1. Probar endpoints de autenticación");
        log.info("   2. Verificar CRUD de sectores y empleados");
        log.info("   3. Testear sistema de turnos y colas");
        log.info("   4. Validar reportes y estadísticas");
        log.info("   5. Comprobar pantalla de turnos");
        log.info("");
        log.info("=== DATOS DE PRUEBA COMPLETOS LISTOS ===");
    }
}
