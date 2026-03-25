package com.nutripharma.api_nutripharma.organization.personal.service;

import com.nutripharma.api_nutripharma.organization.personal.controller.PersonalInternoController;
import com.nutripharma.api_nutripharma.organization.personal.domain.Administrador;
import com.nutripharma.api_nutripharma.organization.personal.repository.AdministradorRepository;
import com.nutripharma.api_nutripharma.security.domain.Rol;
import com.nutripharma.api_nutripharma.security.domain.Usuario;
import com.nutripharma.api_nutripharma.security.repository.RolRepository;
import com.nutripharma.api_nutripharma.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PersonalInternoService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdministradorRepository administradorRepository; // <-- AÑADIDO

    @Transactional
    public void crearAdmin(String email, String password, String nombre, String apellidos) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El email ya está registrado en el sistema.");
        }

        Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Error: Rol ROLE_ADMIN no encontrado."));

        // 1. Creamos las credenciales
        Usuario nuevoUsuarioAdmin = Usuario.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .activo(true)
                .roles(Set.of(rolAdmin))
                .build();

        usuarioRepository.save(nuevoUsuarioAdmin);

        // 2. Creamos la identidad física del Administrador
        Administrador nuevoAdministrador = Administrador.builder()
                .usuario(nuevoUsuarioAdmin)
                .nombre(nombre)
                .apellidos(apellidos)
                .build();

        administradorRepository.save(nuevoAdministrador);
    }

    @Transactional(readOnly = true)
    public List<PersonalInternoController.AdminResponse> obtenerTodos() {
        return administradorRepository.findAll().stream()
                .map(admin -> new com.nutripharma.api_nutripharma.organization.personal.controller.PersonalInternoController.AdminResponse(
                        admin.getId(),
                        admin.getUsuario().getEmail(),
                        admin.getNombre(),
                        admin.getApellidos()
                )).toList();
    }

    @Transactional
    public void eliminarAdmin(Long id) {
        Administrador admin = administradorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado."));

        boolean esSuperAdmin = admin.getUsuario().getRoles().stream()
                .anyMatch(rol -> rol.getNombre().equals("ROLE_SUPERADMIN"));

        if (esSuperAdmin) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Acción denegada: No se puede eliminar a un SuperAdministrador."
            );
        }

        // --- APLICAMOS SOFT DELETE AL ALMA ---
        Usuario usuario = admin.getUsuario();
        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        // --- APLICAMOS SOFT DELETE AL CUERPO ---
        // (El @SQLDelete de la entidad interceptará esto y hará el UPDATE)
        administradorRepository.delete(admin);
    }

    @Transactional
    public void restaurarAdmin(Long id) {
        administradorRepository.reactivarUsuario(id);
        administradorRepository.reactivarAdministrador(id);
    }

    @Transactional(readOnly = true)
    public List<com.nutripharma.api_nutripharma.organization.personal.repository.AdministradorRepository.AdminInactivoProjection> obtenerBajas() {
        return administradorRepository.findHistorialBajas();
    }

}