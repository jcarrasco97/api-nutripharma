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

    public String login(String username, String password) {
        // 1. Delegamos a Spring Security la comprobación de la contraseña encriptada
        // Si la contraseña es incorrecta, saltará una excepción automáticamente aquí.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // 2. Si llegamos a esta línea, la contraseña era correcta. Recuperamos al usuario.
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado en BD."));

        // 3. Comprobación de negocio adicional
        if (!usuario.getActivo()) {
            throw new IllegalStateException("La cuenta de usuario se encuentra inactiva.");
        }

        // 4. Generamos el JWT pasándole el UserDetails (que ahora incluye sus roles)
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
                .password(passwordEncoder.encode(password)) // Encriptamos antes de guardar
                .activo(true)
                .roles(Set.of(rolAsignado))
                .build();

        return usuarioRepository.save(nuevoUsuario).getId();
    }

    @Transactional
    public void solicitarResetPassword(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado en BD."));

        // 1. Generamos un Token aleatorio e impronunciable
        String token = java.util.UUID.randomUUID().toString();

        // 2. Caduca en 15 minutos exactos
        usuario.setResetPasswordToken(token);
        usuario.setResetPasswordExpiration(java.time.LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(usuario);

        // 3. SIMULAMOS EL ENVÍO DEL EMAIL (Cámbialo por JavaMailSender en Producción)
        String enlaceReset = "http://localhost:5173/reset-password?token=" + token;
        System.out.println("============================================================================");
        System.out.println("SIMULACIÓN DE EMAIL A: " + email);
        System.out.println("Asunto: Recuperación de Contraseña - NutriPharma");
        System.out.println("Cuerpo: Haz clic aquí para restablecer tu contraseña: " + enlaceReset);
        System.out.println("============================================================================");
    }

    @Transactional
    public void cambiarPasswordConToken(String token, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o expirado."));

        // 1. Verificamos que no hayan pasado los 15 minutos
        if (usuario.getResetPasswordExpiration().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalStateException("El token de recuperación ha caducado.");
        }

        // 2. Cambiamos la contraseña (¡ENCRIPTADA SIEMPRE!)
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));

        // 3. Destruimos el token para que no pueda usarse dos veces
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordExpiration(null);

        usuarioRepository.save(usuario);
    }
}