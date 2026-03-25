package com.nutripharma.api_nutripharma.sales.catalogo.service;

import com.nutripharma.api_nutripharma.sales.catalogo.controller.dto.ProductoDTO;
import com.nutripharma.api_nutripharma.sales.catalogo.controller.dto.ProductoDTO.ProductoRequest;
import com.nutripharma.api_nutripharma.sales.catalogo.controller.dto.ProductoDTO.ProductoResponse;
import com.nutripharma.api_nutripharma.sales.catalogo.domain.Producto;
import com.nutripharma.api_nutripharma.sales.catalogo.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

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
        p.setPvf(request.pvf());
        p.setPvp(request.pvp());

        return mapToResponse(productoRepository.save(p));
    }

    @Transactional
    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<com.nutripharma.api_nutripharma.sales.catalogo.repository.ProductoRepository.ProductoInactivoProjection> obtenerBajas() {
        return productoRepository.findHistorialBajas();
    }

    @Transactional
    public void restaurarProducto(Long id) {
        productoRepository.reactivarProducto(id);
    }

    private ProductoResponse mapToResponse(Producto p) {
        return new ProductoResponse(
                p.getId(),
                p.getNombreProducto(),
                p.getAcronimo(),
                p.getCategoria(),
                p.getReferencia(),
                p.getPvf(),
                p.getPvp(),
                p.getIva(),
                p.getHayExistencias()
        );
    }
}