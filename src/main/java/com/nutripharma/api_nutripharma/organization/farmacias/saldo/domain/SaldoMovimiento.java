package com.nutripharma.api_nutripharma.organization.farmacias.saldo.domain;

import com.nutripharma.api_nutripharma.organization.farmacias.domain.Farmacia;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "saldo_movimientos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaldoMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmacia_id", nullable = false)
    private Farmacia farmacia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    // Importe con signo: positivo = saldo sube, negativo = saldo baja.
    // INGRESO y DEVOLUCION son siempre positivos.
    // GASTO y REVERSION son siempre negativos.
    // AJUSTE_MANUAL puede ser ambos.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    @Column(name = "saldo_resultante", nullable = false, precision = 10, scale = 2)
    private BigDecimal saldoResultante;

    @Column(name = "referencia_tipo", length = 50)
    private String referenciaTipo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    // Nota interna del admin; nunca se expone al rol FARMACIA.
    @Column(length = 500)
    private String nota;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "creado_por", length = 255)
    private String creadoPor;
}
