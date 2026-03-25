package com.nutripharma.api_nutripharma.sales.pedidos.domain;

import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pedido_reparto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepartoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutricionista_id", nullable = false)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE) // <-- AÑADIR ESTO
    private Nutricionista nutricionista;

    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;
}