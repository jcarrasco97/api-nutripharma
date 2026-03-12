package com.nutripharma.api_nutripharma.clinical.pacientes.controller;

import com.nutripharma.api_nutripharma.clinical.pacientes.controller.dto.PacienteRequest;
import com.nutripharma.api_nutripharma.clinical.pacientes.controller.dto.PacienteResponse;
import com.nutripharma.api_nutripharma.clinical.pacientes.service.PacienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @GetMapping
    public ResponseEntity<List<PacienteResponse>> getAll() {
        return ResponseEntity.ok(pacienteService.getAllPacientes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.getPacienteById(id));
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> create(@RequestBody PacienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.createPaciente(request));
    }
    @GetMapping("/buscar")
    public ResponseEntity<PacienteResponse> buscar(
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String email) {
        return ResponseEntity.ok(pacienteService.findByCriteria(dni, email));
    }
// ... (otros endpoints)

    // Cambiamos la ruta para que no choque con el 'buscar' antiguo
    @GetMapping("/predictivo")
    public ResponseEntity<List<PacienteResponse>> buscarPredictivo(@RequestParam String dni) {
        List<PacienteResponse> resultados = pacienteService.buscarPorDniPredictivo(dni);
        return ResponseEntity.ok(resultados);
    }
}