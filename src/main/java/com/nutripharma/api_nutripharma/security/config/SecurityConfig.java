package com.nutripharma.api_nutripharma.security.config;

import com.nutripharma.api_nutripharma.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración central de seguridad del sistema (Spring Security).
 * Establece una arquitectura sin estado (Stateless) orientada a APIs REST con JWT,
 * definiendo las reglas de autorización HTTP y los proveedores de autenticación.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Configura el filtro principal de seguridad HTTP.
     * Deshabilita CSRF (innecesario y problemático al usar tokens JWT en lugar de cookies),
     * establece la política de sesiones como STATELESS y restringe el acceso a los endpoints.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        // Si usas .hasRole("ADMIN"), Spring busca "ROLE_ADMIN" en el token
                        .requestMatchers("/api/pacientes/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_NUTRICIONISTA")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Define el algoritmo estándar de la aplicación para el cifrado unidireccional de contraseñas.
     * Se emplea BCrypt por su robustez e incorporación de "salting" interno automático.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el proveedor de autenticación de acceso a datos (DAO).
     * Requerido explícitamente para evitar dependencias circulares en el contexto de Spring.
     * Se inyecta el UserDetailsService por constructor para asegurar que la validación contra
     * la base de datos esté acoplada correctamente desde la instanciación.
     */
    @Bean
    public org.springframework.security.authentication.dao.DaoAuthenticationProvider authenticationProvider() {
        org.springframework.security.authentication.dao.DaoAuthenticationProvider authProvider =
                new org.springframework.security.authentication.dao.DaoAuthenticationProvider(userDetailsService);

        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * Expone el gestor de autenticación principal como Bean.
     * Esto permite inyectarlo en los servicios de negocio (ej. AuthService)
     * para disparar el proceso de validación de credenciales manualmente durante el login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}