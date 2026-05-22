package com.nutripharma.api_nutripharma.sales.pedidos.controller.dto;

import com.nutripharma.api_nutripharma.sales.pedidos.domain.EstadoPedido;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PedidoDTO {

    public record LineaPedidoRequest(
            Long productoId,
            Integer cantidad,
            Integer bonificados,
            Boolean pagadoConSaldo
    ) {}

    public record LineaPedidoResponse(
            Long id,
            String productoNombre,
            Integer cantidad,
            Integer bonificados,
            BigDecimal precioUnitario,
            BigDecimal subtotal,
            Boolean pagadoConSaldo
    ) {}

    // --- NUEVO: DTOs para el reparto multicapa ---
    public record RepartoRequest(
            Long nutricionistaId,
            BigDecimal porcentaje
    ) {}

    public record RepartoResponse(
            Long nutricionistaId,
            String nutricionistaNombre,
            BigDecimal porcentaje
    ) {}

    public record PedidoRequest(
            Long farmaciaId,
            LocalDate fechaPedido,
            List<LineaPedidoRequest> lineas,
            List<RepartoRequest> repartos,
            String observaciones
    ) {}

    public record PedidoResponse(
            Long id,
            String farmaciaNombre,
            LocalDate fechaPedido,
            EstadoPedido estado,
            BigDecimal totalPedido,
            List<LineaPedidoResponse> lineas,
            String creadoPor,
            String creadoPorNombre,
            List<RepartoResponse> repartos,
            String observaciones
    ) {}
}