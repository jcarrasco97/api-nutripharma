package com.nutripharma.api_nutripharma.operations.dashboard.controller;

import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO.ResumenMensualNutricionista;
import com.nutripharma.api_nutripharma.operations.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // 👇 NUEVAS RUTAS PARA EL ADMIN 👇

    @GetMapping("/admin/facturacion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DashboardDTO.FacturacionMensualDTO>> obtenerFacturacionGlobal(
            @RequestParam int anio,
            @RequestParam(required = false) Long farmaciaId,
            @RequestParam(required = false) Long nutricionistaId) {
        return ResponseEntity.ok(dashboardService.obtenerFacturacionGlobalAnual(anio, farmaciaId, nutricionistaId));
    }

    @GetMapping("/admin/calendario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DashboardDTO.EventoCalendarioDTO>> obtenerEventosCalendario(
            @RequestParam int anio,
            @RequestParam int mes) {
        return ResponseEntity.ok(dashboardService.obtenerEventosCalendario(anio, mes));
    }

    @GetMapping("/admin/auditoria/{nutricionistaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardDTO.AuditoriaNutriDTO> obtenerAuditoriaNutricionista(
            @PathVariable Long nutricionistaId,
            @RequestParam int anio,
            @RequestParam int mes
    ) {
        return ResponseEntity.ok(dashboardService.obtenerAuditoriaNutricionista(nutricionistaId, anio, mes));
    }

    @GetMapping("/admin/auditoria/{nutricionistaId}/meses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> obtenerMesesAuditoria(@PathVariable Long nutricionistaId) {
        return ResponseEntity.ok(dashboardService.obtenerMesesActividadNutricionista(nutricionistaId));
    }

}