package com.nutripharma.api_nutripharma.security.config;

import com.nutripharma.api_nutripharma.security.domain.Rol;
import com.nutripharma.api_nutripharma.security.domain.Usuario;
import com.nutripharma.api_nutripharma.security.repository.RolRepository;
import com.nutripharma.api_nutripharma.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Componente de inicialización de datos maestros para el entorno de desarrollo.
 * Implementa CommandLineRunner para ejecutarse automáticamente justo después de
 * que el contexto de Spring Boot se haya levantado completamente.
 * Nota arquitectónica: En fases avanzadas o producción, este tipo de inserciones
 * se migrarán a herramientas de versionado de base de datos como Flyway o Liquibase.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Iniciando la validación y siembra de datos maestros de Seguridad...");

        // 1. Asegurar la existencia de los roles base del sistema (RBAC)
        Rol adminRol = crearRolSiNoExiste("ROLE_ADMIN");
        Rol nutriRol = crearRolSiNoExiste("ROLE_NUTRICIONISTA");
        Rol farmaciaRol = crearRolSiNoExiste("ROLE_FARMACIA");

        // 2. Asegurar la existencia del usuario administrador principal (Dueña del negocio)
        String adminEmail = "admin@nutripharma.com";
        if (!usuarioRepository.existsByEmail(adminEmail)) {
            Usuario adminUser = Usuario.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123")) // Contraseña por defecto para desarrollo
                    .activo(true)
                    // Como indicaron los requisitos, la dueña tiene ambos roles
                    .roles(Set.of(adminRol, nutriRol))
                    .build();

            usuarioRepository.save(adminUser);
            log.info("Usuario administrador base creado exitosamente: {}", adminEmail);
        } else {
            log.info("El usuario administrador base ya existe. Omitiendo creación.");
        }
    }

    /**
     * Busca un rol por su nombre; si no existe, lo instancia y persiste.
     */
    private Rol crearRolSiNoExiste(String nombreRol) {
        return rolRepository.findByNombre(nombreRol)
                .orElseGet(() -> {
                    Rol nuevoRol = Rol.builder().nombre(nombreRol).build();
                    return rolRepository.save(nuevoRol);
                });
    }
}