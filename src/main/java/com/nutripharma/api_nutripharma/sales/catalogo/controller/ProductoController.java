package com.nutripharma.api_nutripharma.sales.catalogo.controller;

import com.nutripharma.api_nutripharma.sales.catalogo.controller.dto.ProductoDTO;
import com.nutripharma.api_nutripharma.sales.catalogo.controller.dto.ProductoDTO.ProductoRequest;
import com.nutripharma.api_nutripharma.sales.catalogo.controller.dto.ProductoDTO.ProductoResponse;
import com.nutripharma.api_nutripharma.sales.catalogo.repository.ProductoRepository;
import com.nutripharma.api_nutripharma.sales.catalogo.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Solo el Admin da de alta productos nuevos
    public ResponseEntity<ProductoResponse> crearProducto(@RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crearProducto(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('NUTRICIONISTA') or hasRole('FARMACIA')") // Todos pueden ver el catálogo
    public ResponseEntity<List<ProductoResponse>> listarTodos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> actualizarProducto(@PathVariable Long id, @RequestBody ProductoDTO.ProductoUpdateRequest request) {
        return ResponseEntity.ok(productoService.actualizarProducto(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> toggleStock(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.toggleStock(id));
    }

    @GetMapping("/bajas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductoRepository.ProductoInactivoProjection>> listarBajas() {
        return ResponseEntity.ok(productoService.obtenerBajas());
    }

    @PutMapping("/{id}/restaurar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restaurarProducto(@PathVariable Long id) {
        productoService.restaurarProducto(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/orden-recomendado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> guardarOrdenRecomendado(@RequestBody List<Long> productIds) {
        productoService.guardarOrdenRecomendado(productIds);
        return ResponseEntity.ok().build();
    }
}