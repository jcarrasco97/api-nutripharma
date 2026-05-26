package com.nutripharma.api_nutripharma.operations.consultas.domain;

import com.nutripharma.api_nutripharma.organization.farmacias.domain.Farmacia;
import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "consultas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
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

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    // --- AUDITORÍA (Sello de tiempo del servidor) ---
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private java.time.LocalDateTime fechaCreacion = java.time.LocalDateTime.now();

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

    @Column(name = "mensaje_incidencia", columnDefinition = "TEXT")
    private String mensajeIncidencia;

    // Saldo generado para la farmacia al validar esta consulta. Se graba en el momento
    // de validación para garantizar trazabilidad exacta aunque cambie el % de comisión.
    @Column(name = "comision_generada", precision = 10, scale = 2)
    private java.math.BigDecimal comisionGenerada;

    // Porcentaje de comisión de la farmacia en el momento de crear la consulta.
    @Column(name = "porcentaje_comision_aplicado")
    private Double porcentajeComisionAplicado;

    // --- EVIDENCIAS (Prueba de Vida) ---
    @Column(name = "evidencia_url", length = 500)
    private String evidenciaUrl;

    @Column(name = "evidencia_fecha")
    private java.time.LocalDateTime evidenciaFecha;
}