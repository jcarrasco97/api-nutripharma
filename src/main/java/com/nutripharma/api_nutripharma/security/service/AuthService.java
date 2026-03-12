package com.nutripharma.api_nutripharma.security.service;

import com.nutripharma.api_nutripharma.security.domain.Rol;
import com.nutripharma.api_nutripharma.security.domain.Usuario;
import com.nutripharma.api_nutripharma.security.repository.RolRepository;
import com.nutripharma.api_nutripharma.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public String login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado en BD."));

        if (!usuario.getActivo()) {
            throw new IllegalStateException("La cuenta de usuario se encuentra inactiva.");
        }

        // Pasamos el objeto usuario completo (UserDetails) para incluir los roles
        return jwtService.generateToken(usuario);
    }

    @Transactional
    public Long registrarUsuario(String email, String password, String rolName) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El email ya se encuentra registrado.");
        }

        Rol rolAsignado = rolRepository.findByNombre(rolName)
                .orElseThrow(() -> new IllegalArgumentException("El rol no existe."));

        Usuario nuevoUsuario = Usuario.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .activo(true)
                .roles(Set.of(rolAsignado))
                .build();

        return usuarioRepository.save(nuevoUsuario).getId();
    }
}