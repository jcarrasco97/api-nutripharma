package com.nutripharma.api_nutripharma.sales.pedidos.repository;

import com.nutripharma.api_nutripharma.sales.pedidos.domain.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Muy útil para que la Farmacia vea solo sus pedidos en el frontend
    List<Pedido> findByFarmaciaId(Long farmaciaId);
    // Busca los pedidos de un nutricionista en un rango de fechas
    List<Pedido> findByNutricionistaIdAndFechaPedidoBetween(
            Long nutricionistaId,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );
    List<Pedido> findByNutricionistaUsuarioEmail(String email);
    List<Pedido> findByFarmaciaUsuarioEmail(String email);
    // Para el calendario y facturación global del Admin
    List<Pedido> findByFechaPedidoBetween(LocalDate fechaInicio, LocalDate fechaFin);
}