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

    // Umbral mínimo de liquidación fijado por negocio
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

        Pedido nuevoPedido = Pedido.builder()
                .farmacia(farmacia)
                .nutricionista(nutricionista)
                .fechaPedido(request.fechaPedido())
                .estado(EstadoPedido.PENDIENTE_LIQUIDAR)
                .lineas(new ArrayList<>())
                .build();

        // Contadores para la regla de la "Doble Cesta"
        BigDecimal totalReal = BigDecimal.ZERO;
        BigDecimal totalSaldo = BigDecimal.ZERO;

        // 1. Procesar las líneas de pedido y separar los totales
        for (LineaPedidoRequest lineaReq : request.lineas()) {
            Producto producto = productoRepository.findById(lineaReq.productoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + lineaReq.productoId()));

            boolean pagadoConSaldo = lineaReq.pagadoConSaldo() != null && lineaReq.pagadoConSaldo();
            BigDecimal subtotal = producto.getPvf().multiply(new BigDecimal(lineaReq.cantidad()));

            if (pagadoConSaldo) {
                totalSaldo = totalSaldo.add(subtotal);
            } else {
                totalReal = totalReal.add(subtotal);
            }

            LineaPedido linea = LineaPedido.builder()
                    .pedido(nuevoPedido)
                    .producto(producto)
                    .cantidad(lineaReq.cantidad())
                    .bonificados(lineaReq.bonificados())
                    .precioAplicado(producto.getPvf())
                    .pagadoConSaldo(pagadoConSaldo) // <- Guardamos la marca de cómo se pagó
                    .build();

            nuevoPedido.getLineas().add(linea);
        }

        // 2. Aplicar las Reglas de Negocio del Monedero Virtual
        if (totalSaldo.compareTo(BigDecimal.ZERO) > 0) {
            // Regla A: El pedido real debe llegar a 80€ para desbloquear el saldo
            if (totalReal.compareTo(UMBRAL_LIQUIDACION) < 0) {
                throw new IllegalStateException("Para poder usar el saldo virtual, el importe en dinero real debe ser igual o superior a " + UMBRAL_LIQUIDACION + "€. (Actual: " + totalReal + "€)");
            }

            // Regla B: La farmacia debe tener saldo suficiente
            BigDecimal saldoDisponible = BigDecimal.valueOf(farmacia.getSaldoVirtual() != null ? farmacia.getSaldoVirtual() : 0.0);
            if (saldoDisponible.compareTo(totalSaldo) < 0) {
                throw new IllegalStateException("La farmacia no tiene saldo virtual suficiente. Requerido: " + totalSaldo + "€, Disponible: " + saldoDisponible + "€.");
            }

            // Regla C: Descontar el dinero del monedero de la farmacia
            BigDecimal nuevoSaldo = saldoDisponible.subtract(totalSaldo);
            farmacia.setSaldoVirtual(nuevoSaldo.doubleValue());
            farmaciaRepository.save(farmacia);
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

        // Validamos que el dinero real pagado llegue al mínimo para poder liquidar el pedido principal
        BigDecimal totalReal = calcularTotalRealPedido(pedido);

        if (totalReal.compareTo(UMBRAL_LIQUIDACION) < 0) {
            throw new IllegalStateException("No se puede liquidar. El importe real del pedido no alcanza el mínimo de " + UMBRAL_LIQUIDACION + "€. Total actual: " + totalReal + "€.");
        }

        pedido.setEstado(EstadoPedido.LIQUIDADO);
        return mapToResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // --- MÉTODOS AUXILIARES ---

    private BigDecimal calcularTotalRealPedido(Pedido pedido) {
        // Ahora solo sumamos los productos que NO se pagaron con saldo virtual
        return pedido.getLineas().stream()
                .filter(linea -> linea.getPagadoConSaldo() == null || !linea.getPagadoConSaldo())
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
                        l.getPrecioAplicado().multiply(new BigDecimal(l.getCantidad())), // subtotal
                        l.getPagadoConSaldo() != null && l.getPagadoConSaldo() // <- Lo enviamos al Frontend
                )).collect(Collectors.toList());

        return new PedidoResponse(
                p.getId(),
                p.getFarmacia().getNombre(),
                p.getNutricionista() != null ? p.getNutricionista().getNombre() : null,
                p.getFechaPedido(),
                p.getEstado(),
                calcularTotalRealPedido(p), // Total global (EXCLUSIVO dinero real)
                lineasResponse
        );
    }
}