package com.nutripharma.api_nutripharma.operations.dashboard.controller;

import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO.ResumenMensualNutricionista;
import com.nutripharma.api_nutripharma.operations.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/nutricionistas/{id}/resumen")
    @PreAuthorize("hasRole('ADMIN') or hasRole('NUTRICIONISTA')")
    public ResponseEntity<ResumenMensualNutricionista> obtenerResumen(
            @PathVariable Long id,
            @RequestParam int anio,
            @RequestParam int mes
    ) {
        return ResponseEntity.ok(dashboardService.calcularResumenNutricionista(id, anio, mes));
    }
}