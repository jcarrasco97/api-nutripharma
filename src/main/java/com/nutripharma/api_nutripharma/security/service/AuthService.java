package com.nutripharma.api_nutripharma.security.service;

import com.nutripharma.api_nutripharma.security.domain.Rol;
import com.nutripharma.api_nutripharma.security.domain.Usuario;
import com.nutripharma.api_nutripharma.security.repository.RolRepository;
import com.nutripharma.api_nutripharma.security.repository.UsuarioRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nutripharma.api_nutripharma.core.events.LoginSuccessEvent; // <-- IMPORTAR EVENTO
import jakarta.servlet.http.HttpServletRequest; // <-- IMPORTAR REQUEST
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final Environment env;

    // Inyecciones para la auditoría de seguridad
    private final HttpServletRequest request;
    private final ApplicationEventPublisher eventPublisher;

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

        // =========================================================================
        // 🛡️ INICIO BLOQUE AUDITORÍA: TRAZABILIDAD DE RED (El "Gran Hermano")
        // =========================================================================

        // A) Extraemos la IP real del usuario.
        // Comprobamos primero 'X-Forwarded-For' por si la app está detrás de un proxy (ej. Nginx en el VPS).
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            // Si está vacía, cogemos la IP de la conexión directa (útil en localhost).
            ipAddress = request.getRemoteAddr();
        }

        // B) Extraemos el dispositivo, SO y navegador (User-Agent)
        String userAgent = request.getHeader("User-Agent");

        // C) Disparamos el evento de forma Asíncrona.
        // Nuestro 'SeguridadEventListener' lo cazará al vuelo y lo guardará en BD.
        // Esto permite que el login siga siendo instantáneo para el usuario.
        eventPublisher.publishEvent(new LoginSuccessEvent(
                usuario.getEmail(),
                ipAddress,
                userAgent
        ));

        // =========================================================================
        // 🛡️ FIN BLOQUE AUDITORÍA
        // =========================================================================

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

        String frontendUrl = env.getProperty("app.frontend.url", "http://localhost:5173");
        String enlaceReset = frontendUrl + "/reset-password?token=" + token;

        Context ctx = new Context();
        ctx.setVariable("nombre", usuario.getEmail());
        ctx.setVariable("enlaceReset", enlaceReset);
        String html = templateEngine.process("email-reset-password", ctx);

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom("soporte@innaforem.es");
            helper.setTo(usuario.getEmail());
            helper.setSubject("Recuperación de contraseña — Nutripharma ERP");
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Correo de recuperación enviado a: {}", usuario.getEmail());
        } catch (MessagingException | org.springframework.mail.MailException e) {
            log.error("Error al enviar correo de recuperación a {}: {}", usuario.getEmail(), e.getMessage());
            throw new RuntimeException("Error al enviar el correo de recuperación.", e);
        }
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