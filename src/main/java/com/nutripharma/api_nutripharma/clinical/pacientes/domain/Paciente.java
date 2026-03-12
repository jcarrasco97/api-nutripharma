package com.nutripharma.api_nutripharma.clinical.pacientes.domain;

import com.nutripharma.api_nutripharma.clinical.consultas.domain.Consulta;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa a un paciente en el sistema clínico.
 * Basada en la especificación OpenAPI y compatible con el histórico de Nutripharma.
 */
@Entity
@Table(name = "pacientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPaciente;
    private String dni;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    private String telefono;

    private LocalDate fechaNacimiento;

    /** Sexo: false = Mujer, true = Hombre (Mapeo legacy) */
    private Boolean sexo;

    @Column(name = "altura")
    private Double altura;

    @Column(columnDefinition = "TEXT")
    private String historial;

    // RELACIÓN INVERSA
    // Un Paciente tiene Muchas Consultas
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consulta> consultas = new ArrayList<>();
}