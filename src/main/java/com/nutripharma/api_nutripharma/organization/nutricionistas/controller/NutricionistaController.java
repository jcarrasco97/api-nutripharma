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

    @GetMapping("/bajas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<com.nutripharma.api_nutripharma.organization.nutricionistas.repository.NutricionistaRepository.NutriInactivoProjection>> listarBajas() {
        return ResponseEntity.ok(nutricionistaService.obtenerBajas());
    }

    @PutMapping("/{id}/restaurar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restaurarNutricionista(@PathVariable Long id) {
        // En un proyecto gigante esto pasaría por el Service, pero al ser una query nativa
        // directa de 1 línea, podemos inyectar el repositorio o crear el método en el service.
        // Asumiendo que lo pasamos por el Service (Añade el método en NutricionistaService):
        nutricionistaService.restaurarNutricionista(id);
        return ResponseEntity.ok().build();
    }
}