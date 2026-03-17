package com.nutripharma.api_nutripharma.sales.pedidos.service;

import com.nutripharma.api_nutripharma.organization.farmacias.domain.Farmacia;
import com.nutripharma.api_nutripharma.organization.farmacias.repository.FarmaciaRepository;
import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import com.nutripharma.api_nutripharma.organization.nutricionistas.repository.NutricionistaRepository;
import com.nutripharma.api_nutripharma.sales.catalogo.domain.Producto;
import com.nutripharma.api_nutripharma.sales.catalogo.repository.ProductoRepository;
import com.nutripharma.api_nutripharma.sales.pedidos.controller.dto.PedidoDTO.*;
import com.nutripharma.api_nutripharma.sales.pedidos.domain.EstadoPedido;
import com.nutripharma.api_nutripharma.sales.pedidos.domain.LineaPedido;
import com.nutripharma.api_nutripharma.sales.pedidos.domain.Pedido;
import com.nutripharma.api_nutripharma.sales.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final FarmaciaRepository farmaciaRepository;
    private final NutricionistaRepository nutricionistaRepository;
    private final ProductoRepository productoRepository;

    // Umbral mínimo de liquidación fijado por negocio (En el futuro podría venir de la BD)
    private static final BigDecimal UMBRAL_LIQUIDACION = new BigDecimal("80.00");

    @Transactional
    public PedidoResponse crearPedido(PedidoRequest request) {
        Farmacia farmacia = farmaciaRepository.findById(request.farmaciaId())
                .orElseThrow(() -> new IllegalArgumentException("Farmacia no encontrada"));

        Nutricionista nutricionista = null;
        if (request.nutricionistaId() != null) {
            nutricionista = nutricionistaRepository.findById(request.nutricionistaId())
                    .orElseThrow(() -> new IllegalArgumentException("Nutricionista no encontrado"));
        }

        // 1. Crear la cabecera del pedido
        Pedido nuevoPedido = Pedido.builder()
                .farmacia(farmacia)
                .nutricionista(nutricionista)
                .fechaPedido(request.fechaPedido())
                .estado(EstadoPedido.PENDIENTE_LIQUIDAR)
                .lineas(new ArrayList<>())
                .build();

        // 2. Procesar las líneas de pedido
        for (LineaPedidoRequest lineaReq : request.lineas()) {
            Producto producto = productoRepository.findById(lineaReq.productoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + lineaReq.productoId()));

            LineaPedido linea = LineaPedido.builder()
                    .pedido(nuevoPedido)
                    .producto(producto)
                    .cantidad(lineaReq.cantidad())
                    .bonificados(lineaReq.bonificados())
                    .precioAplicado(producto.getPvf()) // Congelamos el precio de Venta a Farmacia
                    .build();

            nuevoPedido.getLineas().add(linea);
        }

        return mapToResponse(pedidoRepository.save(nuevoPedido));
    }

    @Transactional
    public PedidoResponse liquidarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (pedido.getEstado() == EstadoPedido.LIQUIDADO) {
            throw new IllegalStateException("El pedido ya se encuentra liquidado.");
        }

        // Calcular el total para validar la regla de negocio
        BigDecimal total = calcularTotalPedido(pedido);

        // Validar umbral de 80€
        if (total.compareTo(UMBRAL_LIQUIDACION) < 0) {
            throw new IllegalStateException("No se puede liquidar. El pedido no alcanza el mínimo de " + UMBRAL_LIQUIDACION + "€. Total actual: " + total + "€.");
        }

        pedido.setEstado(EstadoPedido.LIQUIDADO);
        return mapToResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // --- MÉTODOS AUXILIARES ---

    private BigDecimal calcularTotalPedido(Pedido pedido) {
        return pedido.getLineas().stream()
                .map(linea -> linea.getPrecioAplicado().multiply(new BigDecimal(linea.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PedidoResponse mapToResponse(Pedido p) {
        List<LineaPedidoResponse> lineasResponse = p.getLineas().stream()
                .map(l -> new LineaPedidoResponse(
                        l.getId(),
                        l.getProducto().getNombreProducto(),
                        l.getCantidad(),
                        l.getBonificados(),
                        l.getPrecioAplicado(),
                        l.getPrecioAplicado().multiply(new BigDecimal(l.getCantidad())) // subtotal
                )).collect(Collectors.toList());

        return new PedidoResponse(
                p.getId(),
                p.getFarmacia().getNombre(),
                p.getNutricionista() != null ? p.getNutricionista().getNombre() : null,
                p.getFechaPedido(),
                p.getEstado(),
                calcularTotalPedido(p), // Total global
                lineasResponse
        );
    }
}