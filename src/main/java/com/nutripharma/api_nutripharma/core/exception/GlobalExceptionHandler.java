package com.nutripharma.api_nutripharma.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- NUEVO: Interceptor para Reglas de Negocio (Duplicados, Validaciones, etc.) ---
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        // React leerá dinámicamente este campo: error.response.data.message
        body.put("message", ex.getMessage());

        // Un error de validación siempre debe ser un 400 Bad Request
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // --- EL QUE YA TENÍAS: Interceptor Genérico (Fallback) ---
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}