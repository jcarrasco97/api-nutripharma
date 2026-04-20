package com.nutripharma.api_nutripharma.documents.facturas.controller;

import com.nutripharma.api_nutripharma.documents.facturas.domain.FacturaGasto;
import com.nutripharma.api_nutripharma.documents.facturas.service.FacturaGastoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaGastoController {

    private final FacturaGastoService facturaGastoService;

    @PostMapping
    @PreAuthorize("hasRole('NUTRICIONISTA')")
    public ResponseEntity<FacturaGasto> subirFactura(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("mesCorresponde") String mesCorresponde) throws GeneralSecurityException, IOException {

        FacturaGasto factura = facturaGastoService.subirFactura(archivo, mesCorresponde);
        return ResponseEntity.ok(factura);
    }

    @GetMapping("/mis-facturas")
    @PreAuthorize("hasRole('NUTRICIONISTA')")
    public ResponseEntity<List<FacturaGasto>> obtenerMisFacturas() {
        return ResponseEntity.ok(facturaGastoService.obtenerMisFacturas());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<FacturaGasto>> obtenerTodas() {
        return ResponseEntity.ok(facturaGastoService.obtenerTodas());
    }

    @GetMapping("/{id}/descargar")
    @PreAuthorize("hasAnyRole('NUTRICIONISTA', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> descargarFactura(@PathVariable Long id) throws GeneralSecurityException, IOException {
        byte[] data = facturaGastoService.descargarFactura(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"factura_gasto.pdf\"")
                .body(data);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> eliminarFactura(@PathVariable Long id) throws GeneralSecurityException, IOException {
        facturaGastoService.eliminarFactura(id);
        return ResponseEntity.noContent().build();
    }
}
