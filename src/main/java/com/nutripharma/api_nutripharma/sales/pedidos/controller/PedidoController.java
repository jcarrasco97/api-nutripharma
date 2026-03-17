package com.nutripharma.api_nutripharma.sales.pedidos.controller;

import com.nutripharma.api_nutripharma.sales.pedidos.controller.dto.PedidoDTO.PedidoRequest;
import com.nutripharma.api_nutripharma.sales.pedidos.controller.dto.PedidoDTO.PedidoResponse;
import com.nutripharma.api_nutripharma.sales.pedidos.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('NUTRICIONISTA') or hasRole('FARMACIA')")
    public ResponseEntity<PedidoResponse> crearPedido(@RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(request));
    }

    @PutMapping("/{id}/liquidar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('NUTRICIONISTA') or hasRole('FARMACIA')") // Solo el Admin aprueba las liquidaciones
    public ResponseEntity<PedidoResponse> liquidarPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.liquidarPedido(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('NUTRICIONISTA') or hasRole('FARMACIA')")
    public ResponseEntity<List<PedidoResponse>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }
}