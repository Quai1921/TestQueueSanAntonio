package queue_san_antonio.queues;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import queue_san_antonio.queues.models.Empleado;
import queue_san_antonio.queues.models.RolEmpleado;
import queue_san_antonio.queues.repositories.EmpleadoRepository;

@SpringBootApplication
public class QueuesApplication {

    @Autowired
    private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(QueuesApplication.class, args);
	}

    @Bean
    public CommandLineRunner initData(EmpleadoRepository empleadoRepository) {
        return args -> {

            // Solo crear si no existe ningún empleado
            if (empleadoRepository.count() == 0) {
                Empleado admin = Empleado.builder()
                        .username("quai")
                        .password(passwordEncoder.encode("123456"))
                        .nombre("Edgardo")
                        .apellido("Quaino")
                        .email("equaino@sanantonio.gob.ar")
                        .dni("29374131")
                        .rol(RolEmpleado.ADMIN)
                        .activo(true)
                        .sector(null)
                        .build();

                empleadoRepository.save(admin);

                System.out.println("✅ Usuario administrador creado:");
                System.out.println("   Username: Quai");
                System.out.println("   Password: 123456");
                System.out.println("   Rol: ADMIN");
            } else {
                System.out.println("ℹ️ Ya existen empleados en la base de datos");
            }
        };
    }



}
