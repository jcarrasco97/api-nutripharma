package com.nutripharma.api_nutripharma.organization.nutricionistas.domain;

import com.nutripharma.api_nutripharma.security.domain.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nutricionistas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nutricionista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación 1 a 1: Un nutricionista tiene unas credenciales de acceso únicas
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    // Dato crucial para calcular si debe horas o tiene saldo a favor en el Resumen
    @Column(name = "horas_contrato_mensual", nullable = false)
    private Integer horasContratoMensual;
}