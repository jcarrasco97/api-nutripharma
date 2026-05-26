package com.nutripharma.api_nutripharma.organization.farmacias.saldo.controller;

import com.nutripharma.api_nutripharma.organization.farmacias.controller.dto.FarmaciaDTO.FarmaciaResponse;
import com.nutripharma.api_nutripharma.organization.farmacias.domain.Farmacia;
import com.nutripharma.api_nutripharma.organization.farmacias.repository.FarmaciaRepository;
import com.nutripharma.api_nutripharma.organization.farmacias.saldo.controller.dto.SaldoMovimientoDTO.AjusteSaldoRequest;
import com.nutripharma.api_nutripharma.organization.farmacias.saldo.controller.dto.SaldoMovimientoDTO.SaldoMovimientoResponse;
import com.nutripharma.api_nutripharma.organization.farmacias.saldo.service.SaldoMovimientoService;
import com.nutripharma.api_nutripharma.organization.farmacias.service.FarmaciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/farmacias")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SaldoMovimientoController {

    private final SaldoMovimientoService saldoMovimientoService;
    private final FarmaciaRepository farmaciaRepository;
    private final FarmaciaService farmaciaService;

    @GetMapping("/{id}/movimientos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SaldoMovimientoResponse>> listarMovimientosAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(saldoMovimientoService.obtenerMovimientos(id, false));
    }

    @GetMapping("/movimientos/me")
    @PreAuthorize("hasRole('FARMACIA')")
    public ResponseEntity<List<SaldoMovimientoResponse>> listarMisMovimientos(Principal principal) {
        Farmacia farmacia = farmaciaRepository.findByUsuarioEmail(principal.getName())
                .orElseThrow(() -> new NoSuchElementException("Farmacia no encontrada"));
        return ResponseEntity.ok(saldoMovimientoService.obtenerMovimientos(farmacia.getId(), true));
    }

    @PutMapping("/{id}/ajustar-saldo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FarmaciaResponse> ajustarSaldo(
            @PathVariable Long id,
            @RequestBody AjusteSaldoRequest request,
            Principal principal) {
        return ResponseEntity.ok(farmaciaService.ajustarSaldoAdmin(id, request, principal.getName()));
    }
}
