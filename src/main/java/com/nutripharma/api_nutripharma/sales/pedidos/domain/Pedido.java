package com.nutripharma.api_nutripharma.sales.pedidos.domain;

import com.nutripharma.api_nutripharma.organization.farmacias.domain.Farmacia;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

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
@Audited
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmacia_id", nullable = false)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE) // <-- AÑADIR ESTO
    private Farmacia farmacia;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDate fechaPedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPedido estado;

    // --- AUDITORÍA DE CREACIÓN ---
    // Guardará el email de la Farmacia, Nutricionista o Admin (Proxy) que creó el pedido
    @Column(name = "creado_por", nullable = false)
    private String creadoPor;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LineaPedido> lineas = new ArrayList<>();

    // --- NUEVO: Relación 1:N hacia los repartos de las nutricionistas ---
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RepartoPedido> repartos = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}