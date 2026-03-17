package com.nutripharma.api_nutripharma.sales.pedidos.controller.dto;

import com.nutripharma.api_nutripharma.sales.pedidos.domain.EstadoPedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PedidoDTO {

    // --- REQUESTS (Lo que entra desde React/Swagger) ---
    public record LineaPedidoRequest(
            Long productoId,
            Integer cantidad,
            Integer bonificados,
            Boolean pagadoConSaldo // <-- NUEVO CAMPO
    ) {}

    public record PedidoRequest(
            Long farmaciaId,
            Long nutricionistaId, // Puede ser null si la farmacia pide por sí misma
            LocalDate fechaPedido,
            List<LineaPedidoRequest> lineas
    ) {}

    // --- RESPONSES (Lo que devolvemos al frontend) ---
    public record LineaPedidoResponse(
            Long id,
            String nombreProducto,
            Integer cantidad,
            Integer bonificados,
            BigDecimal precioAplicado,
            BigDecimal subtotal, // cantidad * precioAplicado
            Boolean pagadoConSaldo // <-- NUEVO CAMPO
    ) {}

    public record PedidoResponse(
            Long id,
            String farmaciaNombre,
            String nutricionistaNombre, // Nullable
            LocalDate fechaPedido,
            EstadoPedido estado,
            BigDecimal totalPedido, // La suma de todos los subtotales
            List<LineaPedidoResponse> lineas
    ) {}
}