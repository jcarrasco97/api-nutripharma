package com.nutripharma.api_nutripharma.organization.farmacias.domain;

import com.nutripharma.api_nutripharma.security.domain.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "farmacias")
@SQLRestriction("activo = true")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Farmacia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación 1 a 1: Una farmacia tiene unas credenciales de acceso únicas
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, unique = true, length = 20)
    private String cif;

    @Column(length = 20)
    private String telefono;

    @Column(length = 255)
    private String direccion;

    @Column(name = "saldo_virtual", nullable = false)
    @Builder.Default
    private Double saldoVirtual = 0.0;
    @Column(name = "es_provincia_local", nullable = false)
    @Builder.Default
    private Boolean esProvinciaLocal = true; // Por defecto Almería (PVF)
    // --- NUEVO: Comisión Variable ---
    @Column(name = "porcentaje_comision", nullable = false)
    @Builder.Default
    private Double porcentajeComision = 30.0; // 30%
    // --- NUEVO: Para el Soft Delete ---
    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_baja")
    private java.time.LocalDateTime fechaBaja;

    @Column(name = "borrado_por")
    private String borradoPor;
}