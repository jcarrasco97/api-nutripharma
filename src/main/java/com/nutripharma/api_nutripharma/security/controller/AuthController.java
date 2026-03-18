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
}

// Cambiamos email por username
record LoginRequest(String username, String password) {}
record AuthResponse(String token) {}