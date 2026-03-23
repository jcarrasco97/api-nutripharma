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
import com.nutripharma.api_nutripharma.sales.pedidos.domain.RepartoPedido;
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

    private static final BigDecimal UMBRAL_LIQUIDACION = new BigDecimal("80.00");

    @Transactional
    public PedidoResponse crearPedido(PedidoRequest request) {
        Farmacia farmacia = farmaciaRepository.findById(request.farmaciaId())
                .orElseThrow(() -> new IllegalArgumentException("Farmacia no encontrada"));

        Pedido nuevoPedido = Pedido.builder()
                .farmacia(farmacia)
                .fechaPedido(request.fechaPedido())
                .estado(EstadoPedido.PENDIENTE_ENVIO)
                .lineas(new ArrayList<>())
                .repartos(new ArrayList<>())
                .creadoPorAdmin(request.creadoPorAdmin() != null ? request.creadoPorAdmin() : false)
                .build();

        // 1. LÓGICA DE REPARTO MULTICAPA
        if (request.repartos() != null && !request.repartos().isEmpty()) {
            BigDecimal sumaPorcentajes = BigDecimal.ZERO;
            for (RepartoRequest repReq : request.repartos()) {
                Nutricionista n = nutricionistaRepository.findById(repReq.nutricionistaId())
                        .orElseThrow(() -> new IllegalArgumentException("Nutricionista no encontrado para el reparto"));

                RepartoPedido reparto = RepartoPedido.builder()
                        .pedido(nuevoPedido)
                        .nutricionista(n)
                        .porcentaje(repReq.porcentaje())
                        .build();
                nuevoPedido.getRepartos().add(reparto);
                sumaPorcentajes = sumaPorcentajes.add(repReq.porcentaje());
            }

            // Validación de seguridad: El reparto debe sumar 100%
            if (sumaPorcentajes.compareTo(new BigDecimal("100.00")) != 0 && sumaPorcentajes.compareTo(new BigDecimal("100.0")) != 0) {
                throw new IllegalArgumentException("Los porcentajes de reparto deben sumar exactamente 100%. Suma actual: " + sumaPorcentajes);
            }
        }

        BigDecimal totalReal = BigDecimal.ZERO;
        BigDecimal totalSaldo = BigDecimal.ZERO;

        // 2. LÍNEAS DE PEDIDO
        for (LineaPedidoRequest lineaReq : request.lineas()) {
            Producto producto = productoRepository.findById(lineaReq.productoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + lineaReq.productoId()));

            boolean pagadoConSaldo = lineaReq.pagadoConSaldo() != null && lineaReq.pagadoConSaldo();
            BigDecimal precioAplicable = farmacia.getEsProvinciaLocal() ? producto.getPvf() : producto.getPvp();
            BigDecimal subtotal = precioAplicable.multiply(new BigDecimal(lineaReq.cantidad()));

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
                    .precioAplicado(precioAplicable)
                    .pagadoConSaldo(pagadoConSaldo)
                    .build();

            nuevoPedido.getLineas().add(linea);
        }

        // 3. REGLAS MONEDERO VIRTUAL
        if (totalSaldo.compareTo(BigDecimal.ZERO) > 0) {
            if (totalReal.compareTo(UMBRAL_LIQUIDACION) < 0) {
                throw new IllegalStateException("El importe en dinero real debe ser igual o superior a " + UMBRAL_LIQUIDACION + "€. (Actual: " + totalReal + "€)");
            }
            BigDecimal saldoDisponible = BigDecimal.valueOf(farmacia.getSaldoVirtual() != null ? farmacia.getSaldoVirtual() : 0.0);
            if (saldoDisponible.compareTo(totalSaldo) < 0) {
                throw new IllegalStateException("Saldo virtual insuficiente.");
            }
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

        BigDecimal totalReal = calcularTotalRealPedido(pedido);
        if (totalReal.compareTo(UMBRAL_LIQUIDACION) < 0) {
            throw new IllegalStateException("No se puede liquidar. El importe real no alcanza los " + UMBRAL_LIQUIDACION + "€.");
        }

        pedido.setEstado(EstadoPedido.LIQUIDADO);
        return mapToResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> obtenerMisPedidos(String email) {
        List<Pedido> pedidos = pedidoRepository.findByFarmaciaUsuarioEmail(email);

        if (pedidos.isEmpty()) {
            pedidos = pedidoRepository.findByRepartosNutricionistaEmail(email);
        }

        return pedidos.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public PedidoResponse marcarComoEnviado(Long id, List<RepartoRequest> repartos) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (pedido.getEstado() != EstadoPedido.PENDIENTE_ENVIO) {
            throw new IllegalStateException("El pedido no está pendiente de envío.");
        }

        // --- NUEVA LÓGICA: EL ADMIN INYECTA EL REPARTO AL VALIDAR EL PEDIDO ---
        if (repartos != null && !repartos.isEmpty()) {
            pedido.getRepartos().clear(); // Limpiamos por seguridad
            BigDecimal sumaPorcentajes = BigDecimal.ZERO;

            for (RepartoRequest repReq : repartos) {
                Nutricionista n = nutricionistaRepository.findById(repReq.nutricionistaId())
                        .orElseThrow(() -> new IllegalArgumentException("Nutricionista no encontrado para el reparto"));

                RepartoPedido reparto = RepartoPedido.builder()
                        .pedido(pedido)
                        .nutricionista(n)
                        .porcentaje(repReq.porcentaje())
                        .build();
                pedido.getRepartos().add(reparto);
                sumaPorcentajes = sumaPorcentajes.add(repReq.porcentaje());
            }

            if (sumaPorcentajes.compareTo(new BigDecimal("100.00")) != 0 && sumaPorcentajes.compareTo(new BigDecimal("100.0")) != 0) {
                throw new IllegalArgumentException("Los porcentajes de reparto deben sumar exactamente 100%.");
            }
        }

        pedido.setEstado(EstadoPedido.ENVIADO);
        return mapToResponse(pedidoRepository.save(pedido));
    }

    private BigDecimal calcularTotalRealPedido(Pedido pedido) {
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
                        l.getPrecioAplicado().multiply(new BigDecimal(l.getCantidad())),
                        l.getPagadoConSaldo() != null && l.getPagadoConSaldo()
                )).collect(Collectors.toList());

        List<RepartoResponse> repartosResponse = p.getRepartos().stream()
                .map(r -> new RepartoResponse(
                        r.getNutricionista().getId(),
                        r.getNutricionista().getNombre() + " " + r.getNutricionista().getApellidos(),
                        r.getPorcentaje()
                )).collect(Collectors.toList());

        return new PedidoResponse(
                p.getId(),
                p.getFarmacia().getNombre(),
                p.getFechaPedido(),
                p.getEstado(),
                calcularTotalRealPedido(p),
                lineasResponse,
                p.getCreadoPorAdmin(),
                repartosResponse
        );
    }
}