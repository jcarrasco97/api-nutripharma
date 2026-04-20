package com.nutripharma.api_nutripharma.sales.pedidos.repository;

import com.nutripharma.api_nutripharma.sales.pedidos.domain.Pedido;
import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

       List<Pedido> findByFarmaciaUsuarioEmail(String email);

       // Consulta antigua (basada en comisiones ya repartidas)
       @Query("SELECT p FROM Pedido p JOIN p.repartos r WHERE r.nutricionista.usuario.email = :email")
       List<Pedido> findByRepartosNutricionistaEmail(@Param("email") String email);

       // 👇 CONSULTA CORREGIDA: Usamos una subconsulta desde el Nutricionista
       @Query("SELECT p FROM Pedido p WHERE p.farmacia IN (SELECT a.farmacia FROM Nutricionista n JOIN n.asignaciones a WHERE n.usuario.email = :email)")
       List<Pedido> findByNutricionistaAsignadaEmail(@Param("email") String email);

       List<Pedido> findByFechaPedidoBetween(LocalDate start, LocalDate end);

       @Query("SELECT p FROM Pedido p JOIN p.repartos r WHERE r.nutricionista.id = :nutricionistaId")
       List<Pedido> findByRepartosNutricionistaId(@Param("nutricionistaId") Long nutricionistaId);

       @Query("SELECT p FROM Pedido p JOIN p.repartos r WHERE r.nutricionista.id = :nutricionistaId AND p.fechaPedido BETWEEN :inicio AND :fin")
       List<Pedido> findByRepartosNutricionistaIdAndFechaPedidoBetween(
                     @Param("nutricionistaId") Long nutricionistaId,
                     @Param("inicio") LocalDate inicio,
                     @Param("fin") LocalDate fin);

       @Query("SELECT COUNT(l) FROM LineaPedido l WHERE l.producto.id = :productoId AND l.pedido.estado = 'PENDIENTE_ENVIO'")
       long countPedidosPendientesConProducto(
                     @org.springframework.data.repository.query.Param("productoId") Long productoId);

       @Query("SELECT new com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO$RendimientoProductoDTO(" +
               "lp.producto.nombreProducto, " +
               "SUM(lp.cantidad), " +
               "MAX(lp.precioAplicado), " +
               "MAX(lp.producto.pvp), " +
               "SUM(lp.precioAplicado * lp.cantidad), " +
               "SUM(lp.producto.pvp * lp.cantidad)) " +
               "FROM Pedido p JOIN p.lineas lp " +
               "WHERE p.estado <> com.nutripharma.api_nutripharma.sales.pedidos.domain.EstadoPedido.CANCELADO " +
               "AND YEAR(p.fechaPedido) = :anio " +
               "AND (:mes IS NULL OR MONTH(p.fechaPedido) = :mes) " +
               "AND (:farmaciaId IS NULL OR p.farmacia.id = :farmaciaId) " +
               "AND (:nutricionistaId IS NULL OR EXISTS (SELECT 1 FROM RepartoPedido rp WHERE rp.pedido = p AND rp.nutricionista.id = :nutricionistaId)) " +
               "GROUP BY lp.producto.nombreProducto " +
               "ORDER BY SUM(lp.cantidad) DESC")
       List<DashboardDTO.RendimientoProductoDTO> findRendimientoProductos(
               @Param("anio") int anio,
               @Param("mes") Integer mes,
               @Param("farmaciaId") Long farmaciaId,
               @Param("nutricionistaId") Long nutricionistaId,
               org.springframework.data.domain.Pageable pageable);
}