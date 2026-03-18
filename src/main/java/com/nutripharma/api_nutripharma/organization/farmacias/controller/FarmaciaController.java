package com.nutripharma.api_nutripharma.organization.farmacias.controller;

import com.nutripharma.api_nutripharma.organization.farmacias.controller.dto.FarmaciaDTO.FarmaciaRequest;
import com.nutripharma.api_nutripharma.organization.farmacias.controller.dto.FarmaciaDTO.FarmaciaResponse;
import com.nutripharma.api_nutripharma.organization.farmacias.service.FarmaciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farmacias")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // <--- ¡AÑADE ESTO!
public class FarmaciaController {

    private final FarmaciaService farmaciaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FarmaciaResponse> registrarFarmacia(@RequestBody FarmaciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmaciaService.crearFarmacia(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('NUTRICIONISTA')")
    // Los Nutris necesitan ver la lista para elegir farmacia en su turno
    public ResponseEntity<List<FarmaciaResponse>> listarTodas() {
        return ResponseEntity.ok(farmaciaService.obtenerTodas());
    }

    // Añade este endpoint debajo de los que ya tienes:
    @GetMapping("/perfil/me")
    @PreAuthorize("hasRole('FARMACIA')")
    public ResponseEntity<FarmaciaResponse> obtenerMiPerfil(java.security.Principal principal) {
        return ResponseEntity.ok(farmaciaService.obtenerMiPerfil(principal.getName()));
    }
}