package com.nutripharma.api_nutripharma.sales.catalogo.controller;

import com.nutripharma.api_nutripharma.sales.catalogo.controller.dto.ProductoDTO.ProductoRequest;
import com.nutripharma.api_nutripharma.sales.catalogo.controller.dto.ProductoDTO.ProductoResponse;
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
}