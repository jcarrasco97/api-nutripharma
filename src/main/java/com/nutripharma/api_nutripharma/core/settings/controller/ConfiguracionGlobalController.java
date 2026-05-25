package com.nutripharma.api_nutripharma.core.settings.controller;

import com.nutripharma.api_nutripharma.core.settings.domain.ConfiguracionGlobal;
import com.nutripharma.api_nutripharma.core.settings.service.ConfiguracionGlobalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionGlobalController {

    private final ConfiguracionGlobalService service;

    public ConfiguracionGlobalController(ConfiguracionGlobalService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ConfiguracionGlobal> obtenerConfiguracion() {
        return ResponseEntity.ok(service.obtenerConfiguracion());
    }

    @PutMapping("/limite-monedero")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<ConfiguracionGlobal> actualizarLimiteMonedero(@RequestBody Map<String, Double> payload) {
        Double nuevoLimite = payload.get("limiteMonedero");
        return ResponseEntity.ok(service.actualizarLimiteMonedero(nuevoLimite));
    }
}
