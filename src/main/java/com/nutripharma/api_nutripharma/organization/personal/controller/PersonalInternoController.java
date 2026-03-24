package com.nutripharma.api_nutripharma.organization.personal.controller;

import com.nutripharma.api_nutripharma.organization.personal.service.PersonalInternoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personal-interno")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PersonalInternoController {

    private final PersonalInternoService personalInternoService;

    // DTOs
    public record AdminRequest(String email, String password, String nombre, String apellidos) {}
    public record AdminResponse(Long id, String email, String nombre, String apellidos) {}

    @PostMapping("/admin")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<String> crearAdmin(@RequestBody AdminRequest request) {
        personalInternoService.crearAdmin(request.email(), request.password(), request.nombre(), request.apellidos());
        return ResponseEntity.ok("Administrador creado con identidad completa.");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<AdminResponse>> listarAdmins() {
        return ResponseEntity.ok(personalInternoService.obtenerTodos());
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Void> eliminarAdmin(@PathVariable Long id) {
        personalInternoService.eliminarAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/bajas")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<com.nutripharma.api_nutripharma.organization.personal.repository.AdministradorRepository.AdminInactivoProjection>> listarBajas() {
        return ResponseEntity.ok(personalInternoService.obtenerBajas());
    }
}