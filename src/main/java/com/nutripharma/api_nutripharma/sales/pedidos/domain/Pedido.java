package com.nutripharma.api_nutripharma.sales.pedidos.domain;

import com.nutripharma.api_nutripharma.organization.farmacias.domain.Farmacia;
import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El pedido SIEMPRE va a una farmacia
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmacia_id", nullable = false)
    private Farmacia farmacia;

    // Puede ser NULO si la propia farmacia hace el pedido (sin nutricionista)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutricionista_id")
    private Nutricionista nutricionista;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDate fechaPedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.PENDIENTE_ENVIO;

    // MAGIA DE HIBERNATE: Un pedido tiene MUCHAS líneas.
    // Si borro el pedido, se borran sus líneas (CascadeType.ALL)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LineaPedido> lineas = new ArrayList<>();
}