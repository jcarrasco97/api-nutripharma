package com.nutripharma.api_nutripharma.operations.consultas.controller;

import com.nutripharma.api_nutripharma.operations.consultas.controller.dto.ConsultaDTO.ConsultaRequest;
import com.nutripharma.api_nutripharma.operations.consultas.controller.dto.ConsultaDTO.ConsultaResponse;
import com.nutripharma.api_nutripharma.operations.consultas.service.ConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
public class ConsultaController {

    private final ConsultaService consultaService;

    @PostMapping
    @PreAuthorize("hasRole('NUTRICIONISTA') or hasRole('ADMIN')")
    public ResponseEntity<ConsultaResponse> registrarTurno(@RequestBody ConsultaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultaService.registrarTurno(request));
    }

    @PutMapping("/{id}/confirmar")
    @PreAuthorize("hasRole('NUTRICIONISTA') or hasRole('ADMIN')")
    public ResponseEntity<ConsultaResponse> confirmarTurno(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.confirmarTurno(id));
    }

    @PutMapping("/{id}/incidencia")
    @PreAuthorize("hasRole('NUTRICIONISTA')")
    public ResponseEntity<ConsultaResponse> reportarIncidencia(@PathVariable Long id, @RequestBody String mensaje) {
        return ResponseEntity.ok(consultaService.reportarIncidencia(id, mensaje));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('NUTRICIONISTA')") // <-- Añadido el Nutricionista
    public ResponseEntity<List<ConsultaResponse>> listarTodas() {
        return ResponseEntity.ok(consultaService.obtenerTodas());
    }

}