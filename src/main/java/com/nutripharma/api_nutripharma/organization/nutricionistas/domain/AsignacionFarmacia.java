package com.nutripharma.api_nutripharma.organization.nutricionistas.domain;

import com.nutripharma.api_nutripharma.organization.farmacias.domain.Farmacia;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "nutricionista_farmacia") // Mantenemos el nombre de la tabla puente
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class AsignacionFarmacia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutricionista_id", nullable = false)
    private Nutricionista nutricionista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmacia_id", nullable = false)
    private Farmacia farmacia;

    @Column(name = "kilometros", nullable = false)
    @Builder.Default
    private Integer kilometros = 0;
}