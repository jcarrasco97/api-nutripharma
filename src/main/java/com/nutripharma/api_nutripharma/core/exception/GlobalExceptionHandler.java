package com.nutripharma.api_nutripharma.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. REGLAS DE NEGOCIO (DNI duplicados, etc.) -> 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 2. SEGURIDAD (Roles y Permisos insuficientes) -> 403 Forbidden
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "Acceso denegado: No tienes el rol necesario para esta acción.");
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    // 3. CONFLICTOS DE ESTADO (Solapamiento de turnos, acciones inválidas) -> 409
    // Conflict
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // 3.5 CONFLICTOS DE BD (Entradas duplicadas, constraints) -> 409 Conflict
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(
            org.springframework.dao.DataIntegrityViolationException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());

        String customMessage = "Error de integridad de datos. Revisa si estás duplicando un valor único.";
        // Intentar parsear el mensaje de MySQL si es una entrada duplicada
        if (ex.getCause() != null && ex.getCause().getCause() != null) {
            String sqlMessage = ex.getCause().getCause().getMessage();
            if (sqlMessage != null && sqlMessage.contains("Duplicate entry")) {
                customMessage = "Ya existe un registro con este dato único.";
            }
        }

        body.put("message", customMessage);
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // 4. RECURSO NO ENCONTRADO -> 404 Not Found
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // 5. ERRORES INTERNOS GRAVES -> 500 Internal Server Error
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        // ¡VITAL! Imprimimos el error real en la consola del Backend para no estar
        // ciegos
        ex.printStackTrace();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "Error interno del servidor. Revisa la consola del Backend.");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}