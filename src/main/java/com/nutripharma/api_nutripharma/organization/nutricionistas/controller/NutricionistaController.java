package com.nutripharma.api_nutripharma.organization.nutricionistas.controller;

import com.nutripharma.api_nutripharma.organization.nutricionistas.controller.dto.NutricionistaDTO;
import com.nutripharma.api_nutripharma.organization.nutricionistas.controller.dto.NutricionistaDTO.NutricionistaRequest;
import com.nutripharma.api_nutripharma.organization.nutricionistas.controller.dto.NutricionistaDTO.NutricionistaResponse;
import com.nutripharma.api_nutripharma.organization.nutricionistas.service.NutricionistaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/nutricionistas")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // <-- TRAMPA 2 SOLUCIONADA
public class NutricionistaController {

    private final NutricionistaService nutricionistaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NutricionistaResponse> registrarNutricionista(@RequestBody NutricionistaRequest request) {
        NutricionistaResponse response = nutricionistaService.crearNutricionista(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NutricionistaResponse>> listarTodos() {
        return ResponseEntity.ok(nutricionistaService.obtenerTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NutricionistaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(nutricionistaService.obtenerPorId(id));
    }

    // --- TRAMPA 1 SOLUCIONADA (Cambiado a /perfil/me) ---
    @GetMapping("/perfil/me")
    @PreAuthorize("hasRole('NUTRICIONISTA')")
    public ResponseEntity<NutricionistaResponse> obtenerMiPerfil(Principal principal) {
        return ResponseEntity.ok(nutricionistaService.obtenerMiPerfil(principal.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NutricionistaResponse> actualizarNutricionista(@PathVariable Long id, @RequestBody NutricionistaDTO.NutricionistaUpdateRequest request) {
        return ResponseEntity.ok(nutricionistaService.actualizarNutricionista(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarNutricionista(@PathVariable Long id) {
        nutricionistaService.eliminarNutricionista(id);
        return ResponseEntity.noContent().build();
    }
}