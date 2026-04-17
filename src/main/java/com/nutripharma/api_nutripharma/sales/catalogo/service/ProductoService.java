package com.nutripharma.api_nutripharma.sales.catalogo.service;

import com.nutripharma.api_nutripharma.sales.catalogo.controller.dto.ProductoDTO;
import com.nutripharma.api_nutripharma.sales.catalogo.controller.dto.ProductoDTO.ProductoRequest;
import com.nutripharma.api_nutripharma.sales.catalogo.controller.dto.ProductoDTO.ProductoResponse;
import com.nutripharma.api_nutripharma.sales.catalogo.domain.Producto;
import com.nutripharma.api_nutripharma.sales.catalogo.domain.OrdenPorDefectoProducto;
import com.nutripharma.api_nutripharma.sales.catalogo.repository.ProductoRepository;
import com.nutripharma.api_nutripharma.sales.catalogo.repository.OrdenPorDefectoRepository;
import com.nutripharma.api_nutripharma.sales.pedidos.repository.LineaPedidoRepository;
import com.nutripharma.api_nutripharma.sales.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final OrdenPorDefectoRepository ordenPorDefectoRepository;
    private final LineaPedidoRepository lineaPedidoRepository;

    @Transactional
    public ProductoResponse crearProducto(ProductoRequest request) {

        var prodExistente = productoRepository.findByReferenciaIgnorandoBajas(request.referencia());
        if (prodExistente.isPresent()) {
            if (prodExistente.get().getActivo()) {
                throw new IllegalArgumentException("Ya existe un producto activo con esta referencia en el catálogo.");
            } else {
                throw new IllegalArgumentException("Esta referencia pertenece a un producto descatalogado. Ve al Archivo Histórico para restaurarlo.");
            }
        }

        Producto nuevoProducto = Producto.builder()
                .nombreProducto(request.nombreProducto())
                .acronimo(request.acronimo())
                .categoria(request.categoria())
                .referencia(request.referencia())
                .pvf(request.pvf())
                .pvp(request.pvp())
                .build();

        return mapToResponse(productoRepository.save(nuevoProducto));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTodos() {
        return productoRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public ProductoResponse actualizarProducto(Long id, ProductoDTO.ProductoUpdateRequest request) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        p.setNombreProducto(request.nombreProducto());
        if (request.acronimo() != null) p.setAcronimo(request.acronimo());
        if (request.categoria() != null) p.setCategoria(request.categoria());
        if (request.referencia() != null) p.setReferencia(request.referencia());
        p.setPvf(request.pvf());
        p.setPvp(request.pvp());

        return mapToResponse(productoRepository.save(p));
    }

    @Transactional
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        // REGLA DE NEGOCIO: No borrar si está en un carrito/pedido pendiente
        long pedidosPendientes = pedidoRepository.countPedidosPendientesConProducto(id);
        if (pedidosPendientes > 0) {
            throw new IllegalArgumentException(
                    "No se puede descatalogar: Hay " + pedidosPendientes + " pedidos PENDIENTES que contienen este producto. " +
                            "Por favor, ve al Centro de Validaciones y gestiona/cancela esos pedidos primero."
            );
        }

        // Si pasa la validación, procedemos al Soft Delete
        String usuarioActual = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        producto.setBorradoPor(usuarioActual);
        producto.setFechaBaja(java.time.LocalDateTime.now());
        producto.setActivo(false);

        productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public List<com.nutripharma.api_nutripharma.sales.catalogo.repository.ProductoRepository.ProductoInactivoProjection> obtenerBajas() {
        return productoRepository.findHistorialBajas();
    }

    @Transactional
    public void restaurarProducto(Long id) {
        // Ejecuta la consulta nativa que resucita el producto y limpia los datos de auditoría
        productoRepository.reactivarProducto(id);
    }

    @Transactional
    public ProductoResponse toggleStock(Long id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Producto no encontrado"));

        // Invertimos el valor actual (si era true pasa a false, y viceversa)
        p.setHayExistencias(!p.getHayExistencias());

        return mapToResponse(productoRepository.save(p));
    }
    @Transactional
    public void guardarOrdenRecomendado(List<Long> productIds) {
        // 1. Limpiamos el orden anterior
        ordenPorDefectoRepository.deleteAll();

        // 2. Insertamos el nuevo orden
        List<OrdenPorDefectoProducto> nuevasRecoms = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i++) {
            Producto p = productoRepository.findById(productIds.get(i)).orElseThrow();
            nuevasRecoms.add(new OrdenPorDefectoProducto(null, p, i));
        }
        ordenPorDefectoRepository.saveAll(nuevasRecoms);
    }

    @Transactional(readOnly = true)
    public List<Long> obtenerRecomendadosPorFarmacia(Long farmaciaId) {
        return lineaPedidoRepository.findTopProductosIdsByFarmacia(farmaciaId);
    }

    @Transactional(readOnly = true)
    public List<Long> obtenerTopVentasGlobal() {
        return lineaPedidoRepository.findTopProductosIdsGlobal();
    }

    private ProductoResponse mapToResponse(Producto p) {
        // Buscamos si este producto tiene una posición guardada. Si no, le damos un 999 para que vaya al final.
        Integer posicionOrden = ordenPorDefectoRepository.findByProductoId(p.getId())
                .map(OrdenPorDefectoProducto::getPosicion)
                .orElse(999);

        return new ProductoResponse(
                p.getId(),
                p.getNombreProducto(),
                p.getAcronimo(),
                p.getCategoria(),
                p.getReferencia(),
                p.getPvf(),
                p.getPvp(),
                p.getIva(),
                p.getHayExistencias(),
                posicionOrden // <-- LO AÑADIMOS AQUÍ
        );
    }
}