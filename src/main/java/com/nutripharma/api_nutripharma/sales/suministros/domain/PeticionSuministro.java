package com.nutripharma.api_nutripharma.sales.suministros.domain;

import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "peticiones_suministros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeticionSuministro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutricionista_id", nullable = false)
    private Nutricionista nutricionista;

    @Column(nullable = false)
    private LocalDate fechaPeticion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoPeticion estado = EstadoPeticion.PENDIENTE;

    // Aquí está la magia del "Checklist": Una petición tiene MUCHOS materiales seleccionados.
    @ManyToMany
    @JoinTable(
            name = "peticion_material",
            joinColumns = @JoinColumn(name = "peticion_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id")
    )
    @Builder.Default
    private List<Material> materialesSolicitados = new ArrayList<>();
}