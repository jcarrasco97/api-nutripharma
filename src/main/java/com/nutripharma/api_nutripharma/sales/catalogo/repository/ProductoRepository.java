package com.nutripharma.api_nutripharma.sales.catalogo.repository;

import com.nutripharma.api_nutripharma.sales.catalogo.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // --- INTERFAZ DE PROYECCIÓN ---
    interface ProductoInactivoProjection {
        Long getId();
        String getNombreProducto();
        String getReferencia();
        Double getPvf();
        Double getPvp();
    }

    // Para evitar que el Admin cree dos productos con la misma referencia (código de barras/SKU)
    boolean existsByReferencia(String referencia);

    // --- CONSULTA NATIVA PARA EL CEMENTERIO ---
    @Query(value = "SELECT p.id as id, p.nombre_producto as nombreProducto, p.referencia as referencia, p.pvf as pvf, p.pvp as pvp " +
            "FROM productos p WHERE p.activo = false", nativeQuery = true)
    List<ProductoInactivoProjection> findHistorialBajas();
}