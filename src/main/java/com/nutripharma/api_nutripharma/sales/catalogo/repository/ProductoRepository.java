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

        // ESTOS DOS SON LOS NUEVOS:
        java.time.LocalDateTime getFechaBaja();

        String getBorradoPor();
    }

    // Para evitar que el Admin cree dos productos con la misma referencia (código de barras/SKU)
    boolean existsByReferencia(String referencia);

    // --- CONSULTA NATIVA PARA EL CEMENTERIO ---
    @Query(value = "SELECT p.id as id, p.nombre_producto as nombreProducto, p.referencia as referencia, p.pvf as pvf, p.pvp as pvp, p.fecha_baja as fechaBaja, p.borrado_por as borradoPor " +
            "FROM productos p WHERE p.activo = false", nativeQuery = true)
    List<ProductoInactivoProjection> findHistorialBajas();

    // --- RESURRECCIÓN Y LIMPIEZA DE EXPEDIENTE ---
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(
            value = "UPDATE productos SET activo = true, fecha_baja = NULL, borrado_por = NULL WHERE id = ?1",
            nativeQuery = true)
    void reactivarProducto(Long id);

    // Radar para detectar productos descatalogados por referencia
    @Query(value = "SELECT * FROM productos WHERE referencia = :referencia", nativeQuery = true)
    java.util.Optional<com.nutripharma.api_nutripharma.sales.catalogo.domain.Producto> findByReferenciaIgnorandoBajas(@org.springframework.data.repository.query.Param("referencia") String referencia);
}