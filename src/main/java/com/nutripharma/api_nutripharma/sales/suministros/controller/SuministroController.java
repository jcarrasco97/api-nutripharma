package com.nutripharma.api_nutripharma.sales.suministros.controller;

import com.nutripharma.api_nutripharma.sales.suministros.controller.dto.SuministroDTO.*;
import com.nutripharma.api_nutripharma.sales.suministros.service.SuministroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/suministros")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // <-- Importante para React
public class SuministroController {

    private final SuministroService service;

    @PostMapping("/materiales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaterialResponse> crearMaterial(@RequestBody MaterialRequest req) {
        return ResponseEntity.ok(service.crearMaterial(req));
    }

    @GetMapping("/materiales")
    @PreAuthorize("hasRole('ADMIN') or hasRole('NUTRICIONISTA')")
    public ResponseEntity<List<MaterialResponse>> listarMateriales(Principal principal) {
        // Pasamos el email (Principal) para que el servicio calcule qué materiales bloquearle
        return ResponseEntity.ok(service.listarMateriales(principal.getName()));
    }

    @PostMapping("/peticiones")
    @PreAuthorize("hasRole('NUTRICIONISTA')")
    public ResponseEntity<PeticionResponse> crearPeticion(@RequestBody PeticionRequest req, Principal principal) {
        // Ya no recibimos el ID, usamos la identidad segura del Token
        return ResponseEntity.ok(service.crearPeticion(principal.getName(), req));
    }

    @GetMapping("/mis-peticiones")
    @PreAuthorize("hasRole('NUTRICIONISTA')")
    public ResponseEntity<List<PeticionResponse>> obtenerMisPeticiones(Principal principal) {
        return ResponseEntity.ok(service.obtenerMisPeticiones(principal.getName()));
    }

    // --- ENDPOINTS PARA EL ADMIN ---

    @GetMapping("/peticiones-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PeticionResponse>> listarTodasPeticionesAdmin() {
        return ResponseEntity.ok(service.listarTodasPeticionesAdmin());
    }

    @PutMapping("/peticiones/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PeticionResponse> cambiarEstadoPeticion(
            @PathVariable Long id,
            @RequestParam String estado) {
        return ResponseEntity.ok(service.actualizarEstadoPeticion(id, estado));
    }
}