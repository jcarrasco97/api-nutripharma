package com.nutripharma.api_nutripharma.sales.catalogo.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@SQLRestriction("activo = true")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_producto", nullable = false, length = 150)
    private String nombreProducto;

    @Column(nullable = false, length = 20)
    private String acronimo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaProducto categoria;

    @Column(nullable = false, unique = true, length = 50)
    private String referencia;

    // BigDecimal para precisión financiera exacta. Precision 10, 2 decimales.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pvf;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pvp;

    // El IVA por defecto al 10% (0.10)
    @Builder.Default
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal iva = new BigDecimal("0.10");

    // Para diferenciar los que tienen stock en el frontend
    @Builder.Default
    @Column(name = "hay_existencias", nullable = false)
    private Boolean hayExistencias = true;

    // --- NUEVO: Para el Soft Delete (Descatalogado) ---
    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    // --- CAMPOS DE AUDITORÍA (TRAZABILIDAD) ---
    @Column(name = "fecha_baja")
    private java.time.LocalDateTime fechaBaja;

    @Column(name = "borrado_por")
    private String borradoPor;
}