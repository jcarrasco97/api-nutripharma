package com.nutripharma.api_nutripharma.sales.suministros.controller;

import com.nutripharma.api_nutripharma.sales.suministros.controller.dto.SuministroDTO.*;
import com.nutripharma.api_nutripharma.sales.suministros.service.SuministroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/suministros")
@RequiredArgsConstructor
public class SuministroController {

    private final SuministroService service;

    @PostMapping("/materiales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaterialResponse> crearMaterial(@RequestBody MaterialRequest req) { return ResponseEntity.ok(service.crearMaterial(req)); }

    @GetMapping("/materiales")
    @PreAuthorize("hasRole('ADMIN') or hasRole('NUTRICIONISTA')")
    public ResponseEntity<List<MaterialResponse>> listarMateriales() { return ResponseEntity.ok(service.listarMateriales()); }

    @PostMapping("/peticiones")
    @PreAuthorize("hasRole('NUTRICIONISTA')")
    public ResponseEntity<PeticionResponse> crearPeticion(@RequestBody PeticionRequest req) { return ResponseEntity.ok(service.crearPeticion(req)); }

    @GetMapping("/peticiones")
    @PreAuthorize("hasRole('ADMIN') or hasRole('NUTRICIONISTA')") // <-- Añadido el Nutricionista
    public ResponseEntity<List<PeticionResponse>> listarPeticiones() {
        return ResponseEntity.ok(service.listarPeticiones());
    }
}