package com.nutripharma.api_nutripharma.sales.catalogo.controller.dto;

import com.nutripharma.api_nutripharma.sales.catalogo.domain.CategoriaProducto;
import java.math.BigDecimal;

public class ProductoDTO {

    public record ProductoRequest(
            String nombreProducto,
            String acronimo,
            CategoriaProducto categoria,
            String referencia,
            BigDecimal pvf,
            BigDecimal pvp
    ) {}

    public record ProductoResponse(
            Long id,
            String nombreProducto,
            String acronimo,
            CategoriaProducto categoria,
            String referencia,
            BigDecimal pvf,
            BigDecimal pvp,
            BigDecimal iva,
            Boolean hayExistencias
    ) {}
}