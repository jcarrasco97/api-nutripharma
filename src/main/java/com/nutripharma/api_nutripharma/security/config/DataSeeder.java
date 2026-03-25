package com.nutripharma.api_nutripharma.security.config;

import com.nutripharma.api_nutripharma.sales.suministros.domain.Material;
import com.nutripharma.api_nutripharma.sales.suministros.repository.MaterialRepository;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    // AÑADIDO: Inyectamos el repositorio de Materiales
    private final MaterialRepository materialRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Iniciando la validación y siembra de datos maestros de Seguridad...");

        // 1. Asegurar la existencia de los roles base del sistema (RBAC)
        Rol superAdminRol = crearRolSiNoExiste("ROLE_SUPERADMIN"); // <-- AÑADIDO
        Rol adminRol = crearRolSiNoExiste("ROLE_ADMIN");
        Rol nutriRol = crearRolSiNoExiste("ROLE_NUTRICIONISTA");
        Rol farmaciaRol = crearRolSiNoExiste("ROLE_FARMACIA");

        // 2. Asegurar la existencia del usuario administrador principal (Paco)
        String adminEmail = "admin@nutripharma.com";

        if (!usuarioRepository.existsByEmail(adminEmail)) {
            Usuario adminUser = Usuario.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .activo(true)
                    // Le damos los 3 sombreros: SuperAdmin y Admin
                    .roles(Set.of(superAdminRol, adminRol)) // <-- ACTUALIZADO
                    .build();

            usuarioRepository.save(adminUser);
            log.info("Usuario administrador base creado exitosamente: {}", adminEmail);
        } else {
            log.info("El usuario administrador base ya existe. Omitiendo creación.");
        }

        // 3. AÑADIDO: Sembrar el catálogo de Materiales inicial si la tabla está vacía
        if (materialRepository.count() == 0) {
            log.info("Sembrando el catálogo de suministros y materiales corporativos...");
            materialRepository.save(Material.builder().nombre("Folletos Promocionales").cantidadEstandar(100).build());
            materialRepository.save(Material.builder().nombre("Báscula Bioimpedancia (Repuesto)").cantidadEstandar(1).build());
            materialRepository.save(Material.builder().nombre("Cinta Métrica").cantidadEstandar(5).build());
            materialRepository.save(Material.builder().nombre("Bolígrafos Corporativos").cantidadEstandar(50).build());
            materialRepository.save(Material.builder().nombre("Tacos de Recetas (Dietas)").cantidadEstandar(10).build());
            log.info("Catálogo de materiales sembrado con éxito.");
        }
    }

    private Rol crearRolSiNoExiste(String nombreRol) {
        return rolRepository.findByNombre(nombreRol)
                .orElseGet(() -> {
                    Rol nuevoRol = Rol.builder().nombre(nombreRol).build();
                    return rolRepository.save(nuevoRol);
                });
    }
}