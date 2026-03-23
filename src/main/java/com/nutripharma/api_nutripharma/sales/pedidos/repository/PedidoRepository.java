package com.nutripharma.api_nutripharma.sales.pedidos.repository;

import com.nutripharma.api_nutripharma.sales.pedidos.domain.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByFarmaciaUsuarioEmail(String email);

    // NUEVO: Busca los pedidos navegando a través de la tabla intermedia de repartos
    @Query("SELECT p FROM Pedido p JOIN p.repartos r WHERE r.nutricionista.usuario.email = :email")
    List<Pedido> findByRepartosNutricionistaEmail(@Param("email") String email);

    List<Pedido> findByFechaPedidoBetween(LocalDate start, LocalDate end);

    // NUEVO: Buscar pedidos de una nutricionista en un rango de fechas (atravesando los repartos)
    @Query("SELECT p FROM Pedido p JOIN p.repartos r WHERE r.nutricionista.id = :nutricionistaId AND p.fechaPedido BETWEEN :inicio AND :fin")
    List<Pedido> findByRepartosNutricionistaIdAndFechaPedidoBetween(
            @Param("nutricionistaId") Long nutricionistaId,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin
    );
}