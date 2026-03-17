package com.nutripharma.api_nutripharma.sales.suministros.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "materiales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre; // Ej. "Caja de Bolígrafos corporativos"

    @Column(name = "cantidad_estandar", nullable = false)
    private Integer cantidadEstandar; // Ej. 50 (unidades que se envían por defecto)
}