package com.nutripharma.api_nutripharma.security.config;

// import com.nutripharma.api_nutripharma.sales.suministros.domain.Material;
// import com.nutripharma.api_nutripharma.sales.suministros.repository.MaterialRepository;
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

    // Los materiales ya no se siembran desde aquí — gestionado por data.sql
    // private final MaterialRepository materialRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Iniciando la validación y siembra de datos maestros de Seguridad...");

        // Los roles base los crea data.sql. Los recuperamos aquí solo para
        // asignárselos al usuario administrador principal.
        Rol superAdminRol = crearRolSiNoExiste("ROLE_SUPERADMIN");
        Rol adminRol = crearRolSiNoExiste("ROLE_ADMIN");
        // crearRolSiNoExiste("ROLE_NUTRICIONISTA"); // gestionado por data.sql
        // crearRolSiNoExiste("ROLE_FARMACIA");      // gestionado por data.sql

        // Asegurar la existencia del usuario administrador principal (Paco)
        String adminEmail = "admin@nutripharma.com";

        if (!usuarioRepository.existsByEmail(adminEmail)) {
            Usuario adminUser = Usuario.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .activo(true)
                    .roles(Set.of(superAdminRol, adminRol))
                    .build();

            usuarioRepository.save(adminUser);
            log.info("Usuario administrador base creado exitosamente: {}", adminEmail);
        } else {
            log.info("El usuario administrador base ya existe. Omitiendo creación.");
        }

        // El catálogo de materiales y el resto de datos maestros
        // se cargan desde data.sql al arrancar la aplicación.
        // if (materialRepository.count() == 0) {
        //     log.info("Sembrando el catálogo de suministros y materiales corporativos...");
        //     materialRepository.save(Material.builder().nombre("Hojas de dietas").cantidadEstandar(50).build());
        //     materialRepository.save(Material.builder().nombre("Folios").cantidadEstandar(100).build());
        //     materialRepository.save(Material.builder().nombre("Semillas").cantidadEstandar(10).build());
        //     materialRepository.save(Material.builder().nombre("Tóner de impresora").cantidadEstandar(1).build());
        //     materialRepository.save(Material.builder().nombre("Resistencias").cantidadEstandar(5).build());
        //     materialRepository.save(Material.builder().nombre("Agenda").cantidadEstandar(1).build());
        //     log.info("Catálogo de materiales sembrado con éxito.");
        // }
    }

    // Este método es seguro aunque los roles ya existan en BD:
    // findByNombre los recupera sin duplicar, y solo crea si no existen.
    private Rol crearRolSiNoExiste(String nombreRol) {
        return rolRepository.findByNombre(nombreRol)
                .orElseGet(() -> {
                    Rol nuevoRol = Rol.builder().nombre(nombreRol).build();
                    return rolRepository.save(nuevoRol);
                });
    }
}