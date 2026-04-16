package com.nutripharma.api_nutripharma.documents.facturas.domain;

import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@Entity
@Table(name = "facturas_gastos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class FacturaGasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutricionista_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "asignaciones", "usuario"})
    private Nutricionista nutricionista;

    @Column(nullable = false)
    private String nombreArchivo;

    @Column(nullable = false)
    private String driveFileId;

    @Column(nullable = false)
    private LocalDate fechaSubida;

    @Column(nullable = false)
    private String mesCorresponde;
}
