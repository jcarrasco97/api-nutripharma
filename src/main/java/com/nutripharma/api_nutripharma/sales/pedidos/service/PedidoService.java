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
import com.nutripharma.api_nutripharma.core.events.PedidoConfirmadoEvent; // <-- IMPORTA EL EVENTO
import org.springframework.context.ApplicationEventPublisher; // <-- IMPORTA EL PUBLICADOR
import com.nutripharma.api_nutripharma.organization.personal.repository.AdministradorRepository; // <-- AÑADIR ESTE

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
    private final ApplicationEventPublisher eventPublisher; // <-- INYECTA EL EVENT PUBLISHER
    private final AdministradorRepository administradorRepository;

    private static final BigDecimal UMBRAL_LIQUIDACION = new BigDecimal("80.00");

    @Transactional
    public PedidoResponse crearPedido(PedidoRequest request) {
        Farmacia farmacia = farmaciaRepository.findById(request.farmaciaId())
                .orElseThrow(() -> new IllegalArgumentException("Farmacia no encontrada"));

        // 🛡️ SEGURIDAD: Extraemos la identidad inviolable del token JWT
        String usuarioActual = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        Pedido nuevoPedido = Pedido.builder()
                .farmacia(farmacia)
                .fechaPedido(request.fechaPedido())
                .estado(EstadoPedido.PENDIENTE_ENVIO)
                .lineas(new ArrayList<>())
                .repartos(new ArrayList<>())
                .creadoPor(usuarioActual) // <-- INYECCIÓN DE AUDITORÍA
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
                    .bonificados(recalcularBonificadosSeguros(lineaReq.cantidad(), pagadoConSaldo))                    .precioAplicado(precioAplicable)
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
    public PedidoResponse cancelarPedidoAdmin(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (pedido.getEstado() != EstadoPedido.PENDIENTE_ENVIO) {
            throw new IllegalStateException("Solo se pueden cancelar pedidos pendientes de envío.");
        }

        // 1. Calcular cuánto Saldo Virtual se gastó en este pedido
        BigDecimal totalSaldoGastado = BigDecimal.ZERO;
        for (LineaPedido linea : pedido.getLineas()) {
            if (linea.getPagadoConSaldo() != null && linea.getPagadoConSaldo()) {
                BigDecimal subtotalLinea = linea.getPrecioAplicado().multiply(new BigDecimal(linea.getCantidad()));
                totalSaldoGastado = totalSaldoGastado.add(subtotalLinea);
            }
        }

        // 2. Si gastó saldo virtual, se lo devolvemos a la farmacia
        if (totalSaldoGastado.compareTo(BigDecimal.ZERO) > 0) {
            Farmacia farmacia = pedido.getFarmacia();
            BigDecimal saldoActual = BigDecimal.valueOf(farmacia.getSaldoVirtual() != null ? farmacia.getSaldoVirtual() : 0.0);
            BigDecimal saldoRestaurado = saldoActual.add(totalSaldoGastado);

            farmacia.setSaldoVirtual(saldoRestaurado.doubleValue());
            farmaciaRepository.save(farmacia);
        }

        // 3. Cambiar estado a CANCELADO
        pedido.setEstado(EstadoPedido.CANCELADO);

        return mapToResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> obtenerMisPedidos(String email) {
        // 1. Intentamos buscar como si fuera una Farmacia
        List<Pedido> pedidos = pedidoRepository.findByFarmaciaUsuarioEmail(email);

        // 2. Si la lista está vacía, asumimos que es una Nutricionista.
        if (pedidos.isEmpty()) {
            // Usamos la nueva consulta que ignora si hay repartos o no,
            // simplemente busca pedidos hechos por las farmacias de la Nutri.
            pedidos = pedidoRepository.findByNutricionistaAsignadaEmail(email);
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

        // Guardamos explícitamente para tener el objeto final
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // 👇 DISPARAMOS EL EVENTO AL AIRE PARA QUE LO COJA EL EMAIL 👇
        // Usamos tu método privado para calcular el importe real de forma segura
        double totalReal = calcularTotalRealPedido(pedidoGuardado).doubleValue();

        eventPublisher.publishEvent(new PedidoConfirmadoEvent(
                pedidoGuardado.getId(),
                pedidoGuardado.getFarmacia().getUsuario().getEmail(),
                pedidoGuardado.getFarmacia().getNombre(),
                totalReal
        ));

        return mapToResponse(pedidoGuardado);
    }

    private BigDecimal calcularTotalRealPedido(Pedido pedido) {
        return pedido.getLineas().stream()
                .filter(linea -> linea.getPagadoConSaldo() == null || !linea.getPagadoConSaldo())
                .map(linea -> linea.getPrecioAplicado().multiply(new BigDecimal(linea.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PedidoResponse mapToResponse(Pedido p) {
        List<LineaPedidoResponse> lineasResponse = p.getLineas().stream()
                .map(l -> {
                    String nombreProd = (l.getProducto() != null)
                            ? l.getProducto().getNombreProducto()
                            : "[PRODUCTO DESCATALOGADO]";

                    return new LineaPedidoResponse(
                            l.getId(),
                            nombreProd,
                            l.getCantidad(),
                            l.getBonificados(),
                            l.getPrecioAplicado(),
                            l.getPrecioAplicado().multiply(new BigDecimal(l.getCantidad())),
                            l.getPagadoConSaldo() != null && l.getPagadoConSaldo()
                    );
                }).collect(Collectors.toList());

        List<RepartoResponse> repartosResponse = p.getRepartos().stream()
                .map(r -> {
                    String nombreNutri = (r.getNutricionista() != null)
                            ? r.getNutricionista().getNombre() + " " + r.getNutricionista().getApellidos()
                            : "[NUTRICIONISTA DE BAJA]";
                    Long idNutri = (r.getNutricionista() != null) ? r.getNutricionista().getId() : 0L;

                    return new RepartoResponse(idNutri, nombreNutri, r.getPorcentaje());
                }).collect(Collectors.toList());

        String nombreFarmacia = (p.getFarmacia() != null)
                ? p.getFarmacia().getNombre()
                : "[FARMACIA DADA DE BAJA]";

        // 🛡️ TRADUCCIÓN DE EMAIL A NOMBRE REAL (Buscando en los perfiles)
        String emailCreador = p.getCreadoPor();
        String nombreRealAutor = "Sistema";

        if (emailCreador != null) {
            // 1. ¿Fue un Administrador (Proxy)?
            var adminOpt = administradorRepository.findByUsuarioEmail(emailCreador);
            if (adminOpt.isPresent()) {
                nombreRealAutor = adminOpt.get().getNombre() + " " + adminOpt.get().getApellidos() + " (Proxy)";
            } else {
                // 2. ¿Fue una Nutricionista?
                var nutriOpt = nutricionistaRepository.findByUsuarioEmail(emailCreador);
                if (nutriOpt.isPresent()) {
                    nombreRealAutor = nutriOpt.get().getNombre() + " " + nutriOpt.get().getApellidos();
                } else {
                    // 3. ¿Fue la propia Farmacia?
                    var farmaciaOpt = farmaciaRepository.findByUsuarioEmail(emailCreador);
                    if (farmaciaOpt.isPresent()) {
                        nombreRealAutor = farmaciaOpt.get().getNombre();
                    } else {
                        // Fallback por si acaso
                        nombreRealAutor = emailCreador;
                    }
                }
            }
        }

        return new PedidoResponse(
                p.getId(),
                nombreFarmacia,
                p.getFechaPedido(),
                p.getEstado(),
                calcularTotalRealPedido(p),
                lineasResponse,
                emailCreador,
                nombreRealAutor, // <-- AHORA SÍ PASAMOS EL NOMBRE REAL TRADUCIDO
                repartosResponse
        );
    }

    private Integer recalcularBonificadosSeguros(Integer cantidadComprada, Boolean pagadoConSaldo) {
        if (pagadoConSaldo != null && pagadoConSaldo) return 0; // El saldo no genera regalos
        if (cantidadComprada == null || cantidadComprada <= 0) return 0;

        int q = cantidadComprada;
        int totalRegalos = 0;

        totalRegalos += (q / 100) * 25;
        q = q % 100;

        totalRegalos += (q / 20) * 5;
        q = q % 20;

        totalRegalos += (q / 10) * 2;
        q = q % 10;

        totalRegalos += (q / 6) * 1;

        return totalRegalos;
    }
}