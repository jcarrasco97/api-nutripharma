package com.nutripharma.api_nutripharma.organization.nutricionistas.controller;

import com.nutripharma.api_nutripharma.organization.nutricionistas.controller.dto.NutricionistaDTO.NutricionistaRequest;
import com.nutripharma.api_nutripharma.organization.nutricionistas.controller.dto.NutricionistaDTO.NutricionistaResponse;
import com.nutripharma.api_nutripharma.organization.nutricionistas.service.NutricionistaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nutricionistas")
@RequiredArgsConstructor
public class NutricionistaController {

    private final NutricionistaService nutricionistaService;

    @PostMapping
    // ¡Seguridad! Solo los usuarios que tengan el rol ADMIN pueden ejecutar este endpoint
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
}