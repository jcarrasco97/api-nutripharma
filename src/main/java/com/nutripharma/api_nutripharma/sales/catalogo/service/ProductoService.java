package com.nutripharma.api_nutripharma.sales.catalogo.service;

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
        if (productoRepository.existsByReferencia(request.referencia())) {
            throw new IllegalArgumentException("Ya existe un producto con esta referencia.");
        }

        Producto nuevoProducto = Producto.builder()
                .nombreProducto(request.nombreProducto())
                .acronimo(request.acronimo())
                .categoria(request.categoria())
                .referencia(request.referencia())
                .pvf(request.pvf())
                .pvp(request.pvp())
                // El IVA (10%) y hayExistencias (true) se ponen solos gracias al @Builder.Default de la Entidad
                .build();

        return mapToResponse(productoRepository.save(nuevoProducto));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTodos() {
        return productoRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
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