package com.nutripharma.api_nutripharma.operations.consultas.domain;

import com.nutripharma.api_nutripharma.organization.farmacias.domain.Farmacia;
import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

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

    // --- RELACIONES (Dónde y Quién) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutricionista_id", nullable = false)
    private Nutricionista nutricionista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmacia_id", nullable = false)
    private Farmacia farmacia;

    // --- TEMPORALIDAD (El Turno) ---
    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_turno", nullable = false, length = 15)
    private TipoTurno tipoTurno;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    // --- MÉTRICAS (Contadores por defecto a 0) ---
    @Builder.Default
    @Column(nullable = false)
    private Integer nuevas = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer revisiones = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer promociones = 0;

    @Builder.Default
    @Column(name = "personal_farmacia", nullable = false)
    private Integer personalFarmacia = 0;

    // --- CUALITATIVO Y ESTADOS (La máquina de anti-WhatsApp) ---
    @Column(name = "observaciones_jornada", columnDefinition = "TEXT")
    private String observacionesJornada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoConsulta estado = EstadoConsulta.BORRADOR;

    // Si abren incidencia, guardamos aquí el motivo ("Me equivoqué, puse 2 y eran 3")
    @Column(name = "mensaje_incidencia", columnDefinition = "TEXT")
    private String mensajeIncidencia;

    // --- EVIDENCIAS (Prueba de Vida) ---
    @Column(name = "evidencia_url", length = 500)
    private String evidenciaUrl; // Guardaremos el ID del archivo en Drive

    @Column(name = "evidencia_fecha")
    private java.time.LocalDateTime evidenciaFecha;
}