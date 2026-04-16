package com.nutripharma.api_nutripharma.sales.catalogo.domain;

import com.nutripharma.api_nutripharma.sales.catalogo.domain.Producto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "productos_recomendados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecomendacionProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private Producto producto;

    @Column(nullable = false)
    private Integer posicion; // 1, 2, 3...
}