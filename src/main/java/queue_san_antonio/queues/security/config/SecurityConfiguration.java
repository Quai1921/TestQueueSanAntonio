package queue_san_antonio.queues.security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import queue_san_antonio.queues.security.jwt.JwtAuthenticationEntryPoint;
import queue_san_antonio.queues.security.jwt.JwtRequestFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfiguration {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;

    //Configuración principal de seguridad HTTP
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configurando cadena de filtros de seguridad");

        http
                // Deshabilitar CSRF para APIs REST
                .csrf(AbstractHttpConfigurer::disable)

                // Configurar CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configurar autorización de requests
                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos - NO requieren autenticación
                        .requestMatchers("/api/auth/login", "/api/auth/refresh",
                                "/api/sectores/publicos", "/api/sectores/especiales",
                                "api/configuraciones-pantalla/activa",
                                "api/mensajes-institucionales/vigentes", "api/mensajes-institucionales/configuracion/{configuracionId}",
                                "api/turnos/codigo/{codigo}",
                                "/api/sectores/*/stream").permitAll()
                        .requestMatchers("/api/pantalla/**").permitAll()
                        .requestMatchers("/api/turnos/consulta/**").permitAll()

                        // Endpoints de desarrollo y monitoreo
//                        .requestMatchers("/h2-console/**").permitAll()
//                        .requestMatchers("/actuator/**").permitAll()
//                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Endpoints administrativos - Solo ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/empleados/**").hasRole("ADMIN")
                        .requestMatchers("/api/sectores/crear").hasRole("ADMIN")
                        .requestMatchers("/api/sectores/*/asignar-responsable").hasRole("ADMIN")
                        .requestMatchers("/api/configuracion/**").hasRole("ADMIN")

                        // Endpoints de responsable de sector - ADMIN o RESPONSABLE_SECTOR
                        .requestMatchers("/api/responsable/**").hasAnyRole("ADMIN", "RESPONSABLE_SECTOR")
                        .requestMatchers("/api/estadisticas/**").hasAnyRole("ADMIN", "RESPONSABLE_SECTOR")
                        .requestMatchers("/api/horarios/**").hasAnyRole("ADMIN", "RESPONSABLE_SECTOR")
                        .requestMatchers("/api/mensajes/**").hasAnyRole("ADMIN", "RESPONSABLE_SECTOR")

                        // Endpoints de operación - Todos los roles autenticados
                        .requestMatchers("/api/turnos/**").hasAnyRole("ADMIN", "RESPONSABLE_SECTOR", "OPERADOR")
                        .requestMatchers("/api/ciudadanos/**").hasAnyRole("ADMIN", "RESPONSABLE_SECTOR", "OPERADOR")
                        .requestMatchers("/api/sectores/listar").hasAnyRole("ADMIN", "RESPONSABLE_SECTOR", "OPERADOR")

                        // Endpoints de consulta básica - Todos los roles
                        .requestMatchers("/api/historial/**").hasAnyRole("ADMIN", "RESPONSABLE_SECTOR", "OPERADOR")

                        // Cualquier otro endpoint requiere autenticación
                        .anyRequest().authenticated()
                )

                // Configurar manejo de excepciones de autenticación
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // Configurar gestión de sesiones como STATELESS (para JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configurar provider de autenticación
                .authenticationProvider(authenticationProvider())

                // Agregar filtro JWT antes del filtro de autenticación por usuario/password
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)

                // Configuración especial para H2 Console (solo desarrollo)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Permitir iframes para H2 Console
                );

        log.info("Configuración de seguridad completada");
        return http.build();
    }

    //Configuración de CORS para permitir requests desde el frontend
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.debug("Configurando CORS");

        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (ajustar según el entorno)
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",    // React dev server
                "http://localhost:5173",    // Vite dev server
                "http://localhost:8080",    // Spring Boot dev
                "https://*.municipalidad.local", // Dominio local
                "https://*.sanantonio.gov.ar"    // Dominio productivo
        ));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Headers expuestos al cliente
        configuration.setExposedHeaders(Arrays.asList(
                "X-Token-Remaining-Minutes",
                "X-Token-Warning"
        ));

        // Permitir credenciales
        configuration.setAllowCredentials(true);

        // Tiempo de cache para preflight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    //Bean para el encoder de passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Fuerza 12 para mayor seguridad
    }

    //Bean para el manager de autenticación
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    //Provider de autenticación usando DAO
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false); // Para mejor debugging
        return authProvider;
    }

    //Configuración de seguridad para WebSockets (si se implementa)
    // @Bean
    // public WebSocketMessageBrokerConfigurer webSocketSecurity() {
    //     return new WebSocketMessageBrokerConfigurer() {
    //         @Override
    //         public void configureClientInboundChannel(ChannelRegistration registration) {
    //             registration.interceptors(new JwtWebSocketInterceptor());
    //         }
    //     };
    // }
}