package com.nutripharma.api_nutripharma.sales.pedidos.repository;

import com.nutripharma.api_nutripharma.sales.pedidos.domain.LineaPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LineaPedidoRepository extends JpaRepository<LineaPedido, Long> {

    /**
     * Devuelve los IDs de productos ordenados por volumen total comprado
     * (cantidad + bonificados) para una farmacia concreta, excluyendo pedidos cancelados.
     */
    @Query("SELECT lp.producto.id FROM LineaPedido lp " +
           "JOIN lp.pedido p " +
           "WHERE p.farmacia.id = :farmaciaId " +
           "AND p.estado NOT IN ('CANCELADA', 'CANCELADO') " +
           "GROUP BY lp.producto.id " +
           "ORDER BY SUM(lp.cantidad + lp.bonificados) DESC")
    List<Long> findTopProductosIdsByFarmacia(@Param("farmaciaId") Long farmaciaId);

    @Query("SELECT lp.producto.id FROM LineaPedido lp " +
           "JOIN lp.pedido p " +
           "WHERE p.estado NOT IN ('CANCELADA', 'CANCELADO') " +
           "GROUP BY lp.producto.id " +
           "ORDER BY SUM(lp.cantidad + lp.bonificados) DESC")
    List<Long> findTopProductosIdsGlobal();
}
