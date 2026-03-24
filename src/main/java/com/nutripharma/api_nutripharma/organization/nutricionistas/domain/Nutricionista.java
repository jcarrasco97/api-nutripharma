package com.nutripharma.api_nutripharma.organization.nutricionistas.domain;

import com.nutripharma.api_nutripharma.security.domain.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nutricionistas")
// --- MAGIA DEL BORRADO LÓGICO ---
@SQLDelete(sql = "UPDATE nutricionistas SET activo = false WHERE id=?")
@SQLRestriction("activo = true")
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

    @OneToMany(mappedBy = "nutricionista", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AsignacionFarmacia> asignaciones = new ArrayList<>();

    // --- SOLUCIÓN: LA VARIABLE QUE FALTABA PARA EL BORRADO LÓGICO ---
    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}