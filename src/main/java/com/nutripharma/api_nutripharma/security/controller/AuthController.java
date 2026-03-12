package com.nutripharma.api_nutripharma.security.controller;

import com.nutripharma.api_nutripharma.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint público para la gestión de acceso al sistema.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // Llamamos a la lógica de negocio
        String token = authService.login(request.email(), request.password());

        // Devolvemos un JSON estructurado con el token
        return ResponseEntity.ok(new AuthResponse(token));
    }
}

/* * DTOs (Data Transfer Objects).
 * Usamos "Records" de Java (introducidos en Java 14+) porque son la forma
 * más limpia y moderna de crear clases inmutables que solo transportan datos.
 */
record LoginRequest(String email, String password) {}
record AuthResponse(String token) {}