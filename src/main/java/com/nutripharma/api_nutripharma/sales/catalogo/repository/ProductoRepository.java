package com.nutripharma.api_nutripharma.sales.catalogo.repository;

import com.nutripharma.api_nutripharma.sales.catalogo.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Para evitar que el Admin cree dos productos con la misma referencia (código de barras/SKU)
    boolean existsByReferencia(String referencia);
}