package com.nutripharma.api_nutripharma.sales.pedidos.domain;

import com.nutripharma.api_nutripharma.sales.catalogo.domain.Producto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

@Entity
@Table(name = "lineas_pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class LineaPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A qué pedido pertenece esta línea
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    // Qué producto se está comprando
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE) // <-- AÑADIR ESTO
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Builder.Default
    @Column(nullable = false)
    private Integer bonificados = 0;

    // Guardamos el precio en el momento exacto de la compra (por si el PVF cambia en el futuro)
    @Column(name = "precio_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioAplicado;

    @Column(name = "pagado_con_saldo", nullable = false)
    @Builder.Default
    private Boolean pagadoConSaldo = false; // true si va en la 2ª cesta
}