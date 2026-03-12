package com.nutripharma.api_nutripharma.clinical.consultas.controller;

import com.nutripharma.api_nutripharma.clinical.consultas.controller.dto.ConsultaRequest;
import com.nutripharma.api_nutripharma.clinical.consultas.controller.dto.ConsultaResponse;
import com.nutripharma.api_nutripharma.clinical.consultas.service.ConsultaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
public class ConsultaController {

    private final ConsultaService consultaService;

    @PostMapping
    public ResponseEntity<ConsultaResponse> registrarConsulta(@Valid @RequestBody ConsultaRequest request) {
        ConsultaResponse response = consultaService.registrarConsulta(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<ConsultaResponse>> obtenerHistorialPaciente(@PathVariable Long pacienteId) {
        List<ConsultaResponse> historial = consultaService.obtenerConsultasPorPaciente(pacienteId);
        return ResponseEntity.ok(historial);
    }
}