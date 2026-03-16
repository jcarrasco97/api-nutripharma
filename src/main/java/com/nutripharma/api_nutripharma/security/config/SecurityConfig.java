package com.nutripharma.api_nutripharma.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Desactivamos CSRF (No es necesario porque usamos tokens JWT)
                .csrf(csrf -> csrf.disable())

                // 2. Configuración de rutas
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas: El Login debe ser accesible por todos
                        .requestMatchers("/api/auth/**").permitAll()
                        // Cualquier otra ruta requerirá estar autenticado
                        .anyRequest().authenticated()
                )

                // 3. Gestión de sesiones: SIN ESTADO (Stateless)
                // Spring no guardará la sesión en memoria, exigirá el JWT en cada petición
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Asignamos nuestro proveedor y nuestro filtro antes del filtro por defecto
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}