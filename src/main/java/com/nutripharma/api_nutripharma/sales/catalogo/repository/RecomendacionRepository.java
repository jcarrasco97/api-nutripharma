package com.nutripharma.api_nutripharma.sales.catalogo.repository;

import com.nutripharma.api_nutripharma.sales.catalogo.domain.RecomendacionProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecomendacionRepository extends JpaRepository<RecomendacionProducto, Long> {
    Optional<RecomendacionProducto> findByProductoId(Long productoId);
}