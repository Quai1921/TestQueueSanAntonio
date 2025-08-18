//package queue_san_antonio.queues.config;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import queue_san_antonio.queues.models.*;
//import queue_san_antonio.queues.repositories.*;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.List;
//
///**
// * Carga datos de prueba al iniciar la aplicación
// * Solo se ejecuta si no existen datos previos
// * Versión completa para testing exhaustivo de endpoints
// */
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class DataLoader implements CommandLineRunner {
//
//    private final SectorRepository sectorRepository;
//    private final EmpleadoRepository empleadoRepository;
//    private final CiudadanoRepository ciudadanoRepository;
//    private final TurnoRepository turnoRepository;
//    private final ConfiguracionPantallaRepository configuracionPantallaRepository;
//    private final PasswordEncoder passwordEncoder;

//    @Override
//    public void run(String... args) throws Exception {
//        log.info("Iniciando carga de datos de prueba completos...");
//
//        if (empleadoRepository.count() == 0) {
//            crearDatosPruebaCompletos();
//            log.info("Datos de prueba completos cargados exitosamente");
//        } else {
//            log.info("Ya existen datos en la base de datos, omitiendo carga de datos de prueba");
//        }
//    }
//
//    private void crearDatosPruebaCompletos() {
//        log.info("=== INICIANDO CARGA MASIVA DE DATOS DE PRUEBA ===");
//
//        // 1. CREAR SECTORES COMPLETOS
//        log.debug("Creando sectores completos...");
//
//        Sector intendencia = Sector.builder()
//                .codigo("INT")
//                .nombre("Intendencia")
//                .descripcion("Atención al público - Intendente y secretarios")
//                .tipoSector(TipoSector.ESPECIAL)
//                .requiereCitaPrevia(true)
//                .activo(true)
//                .capacidadMaxima(1)
//                .tiempoEstimadoAtencion(30)
//                .color("#FF6B6B")
//                .ordenVisualizacion(1)
//                .build();
//        intendencia = sectorRepository.save(intendencia);
//
//        Sector contabilidad = Sector.builder()
//                .codigo("CON")
//                .nombre("Contabilidad")
//                .descripcion("Gestión contable, financiera y presupuestaria")
//                .tipoSector(TipoSector.NORMAL)
//                .requiereCitaPrevia(false)
//                .activo(true)
//                .capacidadMaxima(2)
//                .tiempoEstimadoAtencion(15)
//                .color("#4ECDC4")
//                .ordenVisualizacion(2)
//                .build();
//        contabilidad = sectorRepository.save(contabilidad);
//
//        Sector rentas = Sector.builder()
//                .codigo("REN")
//                .nombre("Rentas")
//                .descripcion("Impuestos, tasas municipales y cobranzas")
//                .tipoSector(TipoSector.NORMAL)
//                .requiereCitaPrevia(false)
//                .activo(true)
//                .capacidadMaxima(4)
//                .tiempoEstimadoAtencion(10)
//                .color("#45B7D1")
//                .ordenVisualizacion(3)
//                .build();
//        rentas = sectorRepository.save(rentas);
//
//        Sector obras = Sector.builder()
//                .codigo("OBR")
//                .nombre("Obras Públicas")
//                .descripcion("Permisos de construcción, obras y planificación urbana")
//                .tipoSector(TipoSector.NORMAL)
//                .requiereCitaPrevia(false)
//                .activo(true)
//                .capacidadMaxima(3)
//                .tiempoEstimadoAtencion(20)
//                .color("#F7DC6F")
//                .ordenVisualizacion(4)
//                .build();
//        obras = sectorRepository.save(obras);
//
//        // 2. CREAR EMPLEADOS COMPLETOS
//        log.debug("Creando empleados completos...");
//
//        // === ADMINISTRADORES ===
//        Empleado superAdmin = Empleado.builder()
//                .username("admin")
//                .password(passwordEncoder.encode("admin123"))
//                .nombre("Juan Carlos")
//                .apellido("García")
//                .email("admin@sanantonio.gov.ar")
//                .dni("12345678")
//                .rol(RolEmpleado.ADMIN)
//                .activo(true)
//                .sector(null)
//                .build();
//        superAdmin = empleadoRepository.save(superAdmin);
//
//        // === RESPONSABLES DE SECTOR ===
//        Empleado respIntendencia = Empleado.builder()
//                .username("resp_int")
//                .password(passwordEncoder.encode("resp123"))
//                .nombre("María Elena")
//                .apellido("López")
//                .email("mlopez@sanantonio.gov.ar")
//                .dni("23456789")
//                .rol(RolEmpleado.RESPONSABLE_SECTOR)
//                .activo(true)
//                .sector(intendencia)
//                .build();
//        respIntendencia = empleadoRepository.save(respIntendencia);
//        intendencia.setResponsable(respIntendencia);
//        sectorRepository.save(intendencia);
//
//        Empleado respContabilidad = Empleado.builder()
//                .username("resp_con")
//                .password(passwordEncoder.encode("cont123"))
//                .nombre("Carlos Alberto")
//                .apellido("Rodríguez")
//                .email("crodriguez@sanantonio.gov.ar")
//                .dni("34567890")
//                .rol(RolEmpleado.RESPONSABLE_SECTOR)
//                .activo(true)
//                .sector(contabilidad)
//                .build();
//        respContabilidad = empleadoRepository.save(respContabilidad);
//        contabilidad.setResponsable(respContabilidad);
//        sectorRepository.save(contabilidad);
//
//        Empleado respRentas = Empleado.builder()
//                .username("resp_ren")
//                .password(passwordEncoder.encode("rentas123"))
//                .nombre("Patricia Susana")
//                .apellido("Herrera")
//                .email("pherrera@sanantonio.gov.ar")
//                .dni("4455677")
//                .rol(RolEmpleado.RESPONSABLE_SECTOR)
//                .activo(true)
//                .sector(rentas)
//                .build();
//        respRentas = empleadoRepository.save(respRentas);
//        rentas.setResponsable(respRentas);
//        sectorRepository.save(rentas);
//
//        Empleado respObras = Empleado.builder()
//                .username("resp_obr")
//                .password(passwordEncoder.encode("obras123"))
//                .nombre("Miguel Ángel")
//                .apellido("Vargas")
//                .email("mvargas@sanantonio.gov.ar")
//                .dni("55667788")
//                .rol(RolEmpleado.RESPONSABLE_SECTOR)
//                .activo(true)
//                .sector(obras)
//                .build();
//        respObras = empleadoRepository.save(respObras);
//        obras.setResponsable(respObras);
//        sectorRepository.save(obras);
//
//        // === OPERADORES ===
//        // Operadores de Rentas (4)
//        Empleado opRentas1 = Empleado.builder()
//                .username("op_ren1")
//                .password(passwordEncoder.encode("op1234"))
//                .nombre("Ana Patricia")
//                .apellido("Martínez")
//                .email("amartinez@sanantonio.gov.ar")
//                .dni("45678901")
//                .rol(RolEmpleado.OPERADOR)
//                .activo(true)
//                .sector(rentas)
//                .build();
//        empleadoRepository.save(opRentas1);
//
//        Empleado opRentas2 = Empleado.builder()
//                .username("op_ren2")
//                .password(passwordEncoder.encode("op1234"))
//                .nombre("Roberto Daniel")
//                .apellido("González")
//                .email("rgonzalez@sanantonio.gov.ar")
//                .dni("56789012")
//                .rol(RolEmpleado.OPERADOR)
//                .activo(true)
//                .sector(rentas)
//                .build();
//        empleadoRepository.save(opRentas2);
//
//        // Operadores de Obras (3)
//        Empleado opObras1 = Empleado.builder()
//                .username("op_obr1")
//                .password(passwordEncoder.encode("op1234"))
//                .nombre("Lucía Fernanda")
//                .apellido("Pérez")
//                .email("lperez@sanantonio.gov.ar")
//                .dni("67890123")
//                .rol(RolEmpleado.OPERADOR)
//                .activo(true)
//                .sector(obras)
//                .build();
//        empleadoRepository.save(opObras1);
//
//        Empleado opObras2 = Empleado.builder()
//                .username("op_obr2")
//                .password(passwordEncoder.encode("op1234"))
//                .nombre("Sebastián Luis")
//                .apellido("Ramírez")
//                .email("sramirez@sanantonio.gov.ar")
//                .dni("55443322")
//                .rol(RolEmpleado.OPERADOR)
//                .activo(true)
//                .sector(obras)
//                .build();
//        empleadoRepository.save(opObras2);
//
//        // Operadores de Contabilidad (2)
//        Empleado opCont1 = Empleado.builder()
//                .username("op_con1")
//                .password(passwordEncoder.encode("op1234"))
//                .nombre("Mariana Isabel")
//                .apellido("Sánchez")
//                .email("msanchez@sanantonio.gov.ar")
//                .dni("77665544")
//                .rol(RolEmpleado.OPERADOR)
//                .activo(true)
//                .sector(contabilidad)
//                .build();
//        empleadoRepository.save(opCont1);
//
//        // === EMPLEADOS INACTIVOS (para testing) ===
//        Empleado empleadoInactivo1 = Empleado.builder()
//                .username("inactivo1")
//                .password(passwordEncoder.encode("test123"))
//                .nombre("Usuario")
//                .apellido("Inactivo Uno")
//                .email("inactivo1@sanantonio.gov.ar")
//                .dni("78901234")
//                .rol(RolEmpleado.OPERADOR)
//                .activo(false)
//                .sector(rentas)
//                .build();
//        empleadoRepository.save(empleadoInactivo1);
//
//        // 3. CREAR CIUDADANOS MASIVOS
//        log.debug("Creando ciudadanos de prueba masivos...");
//        crearCiudadanosMasivos();
//
//        // 4. CREAR TURNOS DE PRUEBA
//        log.debug("Creando turnos de prueba...");
//        crearTurnosPrueba(intendencia,rentas, obras, contabilidad);
//
//        // 5. CREAR CONFIGURACIÓN DE PANTALLA
//        log.debug("Creando configuración de pantalla...");
//        ConfiguracionPantalla configPantalla = ConfiguracionPantalla.builder()
//                .nombre("Configuración Principal")
//                .tiempoMensaje(10)
//                .tiempoTurno(5)
//                .sonidoActivo(true)
//                .volumenSonido(70)
//                .animacionesActivas(true)
//                .temaColor("default")
//                .mostrarLogo(true)
//                .textoEncabezado("SISTEMA DE TURNOS - MUNICIPALIDAD DE SAN ANTONIO")
//                .activo(true)
//                .build();
//        configuracionPantallaRepository.save(configPantalla);
//
//    }
//
//    private void crearCiudadanosMasivos() {
//        String[][] ciudadanosData = {
//                // DNI, Nombre, Apellido, Teléfono, Dirección, Prioritario, Motivo
//                {"20123456", "Pedro José", "Fernández", "0351-4567890", "Av. San Martín 123", "false", ""},
//                {"30987654", "Rosa María", "Silva", "0351-9876543", "Calle Belgrano 456", "true", "Adulto mayor"},
//                {"40111222", "Luis Alberto", "Morales", "0351-1112223", "Barrio Centro 789", "false", ""},
//                {"25333444", "Elena Cristina", "Vázquez", "0351-3334445", "Av. Córdoba 234", "true", "Embarazada"},
//                {"35555666", "Carlos Enrique", "Mendoza", "0351-5556667", "Calle Rivadavia 567", "false", ""},
//                {"28777888", "María del Carmen", "Gutiérrez", "0351-7778889", "Barrio Norte 890", "true", "Discapacidad"},
//                {"32999000", "Jorge Alejandro", "Romero", "0351-9990001", "Av. Libertad 345", "false", ""},
//                {"26111222", "Susana Beatriz", "Castro", "0351-1112224", "Calle Mitre 678", "false", ""},
//                {"38333444", "Ricardo Fabián", "Herrera", "0351-3334446", "Barrio Sur 901", "false", ""},
//                {"41555666", "Graciela Noemí", "Peralta", "0351-5556668", "Av. Independencia 456", "true", "Adulto mayor"},
//
//        };
//
//        for (String[] data : ciudadanosData) {
//            Ciudadano ciudadano = Ciudadano.builder()
//                    .dni(data[0])
//                    .nombre(data[1])
//                    .apellido(data[2])
//                    .telefono(data[3])
//                    .direccion(data[4])
//                    .esPrioritario(Boolean.parseBoolean(data[5]))
//                    .motivoPrioridad(data[6].isEmpty() ? null : data[6])
//                    .build();
//            ciudadanoRepository.save(ciudadano);
//        }
//    }
//
//    private void crearTurnosPrueba(Sector intendencia, Sector rentas, Sector obras, Sector contabilidad) {
//        // Obtener algunos ciudadanos para los turnos
//        var ciudadanos = ciudadanoRepository.findAll();
//
//        LocalDate hoy = LocalDate.now();
//        LocalDate ayer = hoy.minusDays(1);
//        LocalDate manana = hoy.plusDays(1);
//
//        // Turnos de HOY - GENERADOS (en espera)
//        crearTurno(rentas, ciudadanos.get(0), hoy, LocalTime.of(9, 0), EstadoTurno.GENERADO, TipoTurno.NORMAL, 0);
//        crearTurno(rentas, ciudadanos.get(2), hoy, LocalTime.of(9, 30), EstadoTurno.GENERADO, TipoTurno.NORMAL, 0);
//        crearTurno(obras, ciudadanos.get(3), hoy, LocalTime.of(9, 5), EstadoTurno.GENERADO, TipoTurno.NORMAL, 0);
//
//        // Turnos de HOY - LLAMADOS
//        crearTurno(rentas, ciudadanos.get(0), hoy, LocalTime.of(9, 30), EstadoTurno.LLAMADO, TipoTurno.NORMAL, 0);
//        crearTurno(obras, ciudadanos.get(2), hoy, LocalTime.of(10, 45), EstadoTurno.LLAMADO, TipoTurno.NORMAL, 0);
//
//        // Turnos de HOY - EN ATENCIÓN
//        crearTurnoEnAtencion(rentas, ciudadanos.get(0), hoy, LocalTime.of(9, 31), TipoTurno.NORMAL, 0);
//        crearTurnoEnAtencion(obras, ciudadanos.get(2), hoy, LocalTime.of(10, 48), TipoTurno.NORMAL, 0);
//
//        // Turnos de AYER - FINALIZADOS
//        crearTurnoFinalizado(rentas, ciudadanos.get(4), ayer, LocalTime.of(9, 0), TipoTurno.NORMAL, 0);
//        crearTurnoFinalizado(rentas, ciudadanos.get(5), ayer, LocalTime.of(9, 15), TipoTurno.NORMAL, 0);
//
//
//        // Turnos de AYER - CANCELADOS
//        crearTurno(rentas, ciudadanos.get(6), ayer, LocalTime.of(15, 0), EstadoTurno.CANCELADO, TipoTurno.NORMAL, 0);
//
//        // Turnos de AYER - AUSENTES
//        crearTurno(obras, ciudadanos.get(7), ayer, LocalTime.of(17, 0), EstadoTurno.AUSENTE, TipoTurno.NORMAL, 0);
//
//        // Turnos FUTUROS - ESPECIALES (citas previas)
//        crearTurnoCitaPrevia(intendencia, ciudadanos.get(8), manana, LocalTime.of(9, 0));
//        crearTurnoCitaPrevia(intendencia, ciudadanos.get(9), manana, LocalTime.of(10, 0));
//
//        // Turnos prioritarios de HOY
//        crearTurnoPrioritario(rentas, ciudadanos.get(1), hoy, LocalTime.of(8, 30), EstadoTurno.GENERADO, 1); // Rosa María (adulto mayor)
//        crearTurnoPrioritario(intendencia, ciudadanos.get(3), hoy, LocalTime.of(9, 0), EstadoTurno.GENERADO, 2); // Elena (embarazada)
//    }
//
//    private void crearTurno(Sector sector, Ciudadano ciudadano, LocalDate fecha, LocalTime hora,
//                            EstadoTurno estado, TipoTurno tipo, Integer prioridad) {
//        String codigo = generarCodigoTurno(sector);
//
//        Turno turno = Turno.builder()
//                .codigo(codigo)
//                .sector(sector)
//                .ciudadano(ciudadano)
//                .estado(estado)
//                .tipo(tipo)
//                .prioridad(prioridad)
//                .observaciones("Turno de prueba generado automáticamente")
//                .build();
//
//        // Simular tiempo de generación en el pasado
//        turno.setFechaHoraGeneracion(fecha.atTime(hora));
//
//        turnoRepository.save(turno);
//    }
//
////    private void crearTurnoEnAtencion(Sector sector, Ciudadano ciudadano, LocalDate fecha, LocalTime hora,
////                                      TipoTurno tipo, Integer prioridad) {
////        String codigo = generarCodigoTurno(sector);
////
////        Turno turno = Turno.builder()
////                .codigo(codigo)
////                .sector(sector)
////                .ciudadano(ciudadano)
////                .estado(EstadoTurno.EN_ATENCION)
////                .tipo(tipo)
////                .prioridad(prioridad)
////                .observaciones("Turno de prueba en atención")
////                .build();
////
////        // Simular flujo completo: generado -> llamado -> en atención
////        LocalDateTime fechaGeneracion = fecha.atTime(hora);
////        turno.setFechaHoraGeneracion(fechaGeneracion);
////        turno.setFechaHoraLlamado(fechaGeneracion.plusMinutes(5));
////        turno.setFechaHoraAtencion(fechaGeneracion.plusMinutes(10));
////
////        turnoRepository.save(turno);
////    }
//
//    private void crearTurnoEnAtencion(Sector sector, Ciudadano ciudadano, LocalDate fecha, LocalTime hora,
//                                  TipoTurno tipo, Integer prioridad) {
//    String codigo = generarCodigoTurno(sector);
//
//    // ✅ BUSCAR UN OPERADOR DEL SECTOR
//    List<Empleado> operadores = empleadoRepository.findBySectorIdAndActivoTrue(sector.getId())
//            .stream()
//            .filter(emp -> emp.getRol() == RolEmpleado.OPERADOR)
//            .toList();
//
//    Empleado empleadoAsignado = !operadores.isEmpty() ? operadores.get(0) : null;
//
//    Turno turno = Turno.builder()
//            .codigo(codigo)
//            .sector(sector)
//            .ciudadano(ciudadano)
//            .estado(EstadoTurno.EN_ATENCION)
//            .tipo(tipo)
//            .prioridad(prioridad)
//            .empleadoAtencion(empleadoAsignado)
//            .observaciones("Turno de prueba en atención")
//            .build();
//
//    // Simular flujo completo: generado -> llamado -> en atención
//    LocalDateTime fechaGeneracion = fecha.atTime(hora);
//    turno.setFechaHoraGeneracion(fechaGeneracion);
//    turno.setFechaHoraLlamado(fechaGeneracion.plusMinutes(5));
//    turno.setFechaHoraAtencion(fechaGeneracion.plusMinutes(10));
//
//        // ✅ LOG PARA DEBUG
//        log.info("Turno EN_ATENCION creado: {} - Empleado: {}",
//                codigo, empleadoAsignado != null ? empleadoAsignado.getUsername() : "NULL");
//
//    turnoRepository.save(turno);
//}
//
////    private void crearTurnoFinalizado(Sector sector, Ciudadano ciudadano, LocalDate fecha, LocalTime hora,
////                                      TipoTurno tipo, Integer prioridad) {
////        String codigo = generarCodigoTurno(sector);
////
////        Turno turno = Turno.builder()
////                .codigo(codigo)
////                .sector(sector)
////                .ciudadano(ciudadano)
////                .estado(EstadoTurno.FINALIZADO)
////                .tipo(tipo)
////                .prioridad(prioridad)
////                .observaciones("Turno de prueba finalizado")
////                .build();
////
////        // Simular flujo completo: generado -> llamado -> en atención -> finalizado
////        LocalDateTime fechaGeneracion = fecha.atTime(hora);
////        turno.setFechaHoraGeneracion(fechaGeneracion);
////        turno.setFechaHoraLlamado(fechaGeneracion.plusMinutes(5));
////        turno.setFechaHoraAtencion(fechaGeneracion.plusMinutes(10));
////        turno.setFechaHoraFinalizacion(fechaGeneracion.plusMinutes(10 + sector.getTiempoEstimadoAtencion()));
////
////        turnoRepository.save(turno);
////    }
//
//    private void crearTurnoFinalizado(Sector sector, Ciudadano ciudadano, LocalDate fecha, LocalTime hora,
//                                      TipoTurno tipo, Integer prioridad) {
//        String codigo = generarCodigoTurno(sector);
//
//        // ✅ BUSCAR UN OPERADOR DEL SECTOR
//        List<Empleado> operadores = empleadoRepository.findBySectorIdAndActivoTrue(sector.getId())
//                .stream()
//                .filter(emp -> emp.getRol() == RolEmpleado.OPERADOR)
//                .toList();
//
//        Empleado empleadoAsignado = !operadores.isEmpty() ? operadores.get(0) : null;
//
//        Turno turno = Turno.builder()
//                .codigo(codigo)
//                .sector(sector)
//                .ciudadano(ciudadano)
//                .estado(EstadoTurno.FINALIZADO)
//                .tipo(tipo)
//                .prioridad(prioridad)
//                .empleadoAtencion(empleadoAsignado)  // ✅ ASIGNAR EMPLEADO
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
//
////    private void crearTurnoCitaPrevia(Sector sector, Ciudadano ciudadano, LocalDate fecha, LocalTime hora) {
////        String codigo = generarCodigoTurno(sector);
////
////        Turno turno = Turno.builder()
////                .codigo(codigo)
////                .sector(sector)
////                .ciudadano(ciudadano)
////                .estado(EstadoTurno.GENERADO)
////                .tipo(TipoTurno.ESPECIAL)
////                .prioridad(0)
////                .fechaCita(fecha)
////                .horaCita(hora)
////                .observaciones("Cita previa programada")
////                .build();
////
////        turnoRepository.save(turno);
////    }
//
//
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
//        // ✅ ESTABLECER FECHA DE GENERACIÓN (antes faltaba esto)
//        turno.setFechaHoraGeneracion(LocalDate.now().atTime(8, 0)); // Generada hoy temprano
//
//        turnoRepository.save(turno);
//    }
//
//    private void crearTurnoPrioritario(Sector sector, Ciudadano ciudadano, LocalDate fecha, LocalTime hora,
//                                       EstadoTurno estado, Integer prioridad) {
//        String codigo = generarCodigoTurno(sector);
//
//        Turno turno = Turno.builder()
//                .codigo(codigo)
//                .sector(sector)
//                .ciudadano(ciudadano)
//                .estado(estado)
//                .tipo(TipoTurno.PRIORITARIO)
//                .prioridad(prioridad)
//                .observaciones("Turno prioritario: " + ciudadano.getMotivoPrioridad())
//                .build();
//
//        turno.setFechaHoraGeneracion(fecha.atTime(hora));
//        turnoRepository.save(turno);
//    }
//
//    private String generarCodigoTurno(Sector sector) {
//        // Generar código simple para testing: REN001, OBR002, etc.
//        long count = turnoRepository.count() + 1;
//        return String.format("%s%03d", sector.getCodigo(), count);
//    }


//}
