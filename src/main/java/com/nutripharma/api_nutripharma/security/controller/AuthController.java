package com.nutripharma.api_nutripharma.security.controller;

import com.nutripharma.api_nutripharma.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // <--- ¡AÑADE ESTO!
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // Ahora usamos request.username()
        String token = authService.login(request.username(), request.password());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        // En producción siempre devuelve 200 OK, aunque el correo no exista, para evitar ataques de enumeración (adivinar qué correos existen).
        try {
            authService.solicitarResetPassword(email);
        } catch (Exception e) {
            // Ignoramos silenciosamente si el email no existe
        }
        return ResponseEntity.ok("Si el correo existe, recibirás un enlace de recuperación.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestBody String nuevaPassword) {
        // Limpiamos el texto porque a veces llega con comillas de React
        String cleanPassword = nuevaPassword.replace("\"", "");
        authService.cambiarPasswordConToken(token, cleanPassword);
        return ResponseEntity.ok("Contraseña actualizada con éxito.");
    }
}

// Cambiamos email por username
record LoginRequest(String username, String password) {
}

record AuthResponse(String token) {
}