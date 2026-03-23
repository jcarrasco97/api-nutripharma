package com.nutripharma.api_nutripharma.organization.farmacias.domain;

import com.nutripharma.api_nutripharma.security.domain.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "farmacias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(length = 255)
    private String direccion;

    @Column(name = "saldo_virtual", nullable = false)
    @Builder.Default
    private Double saldoVirtual = 0.0;
    @Column(name = "es_provincia_local", nullable = false)
    @Builder.Default
    private Boolean esProvinciaLocal = true; // Por defecto Almería (PVF)
}