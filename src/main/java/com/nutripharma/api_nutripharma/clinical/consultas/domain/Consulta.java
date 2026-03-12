package com.nutripharma.api_nutripharma.clinical.consultas.domain;

import com.nutripharma.api_nutripharma.clinical.pacientes.domain.Paciente;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_consulta", nullable = false)
    private LocalDateTime fechaConsulta;

    @Column(nullable = false)
    private Double peso;

    @Column(name = "porcentaje_grasa")
    private Double porcentajeGrasa;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    // EL NÚCLEO DE LA RELACIÓN (Foreign Key)
    // Muchas Consultas pertenecen a Un Paciente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
}