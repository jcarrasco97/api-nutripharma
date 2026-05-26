package com.nutripharma.api_nutripharma.organization.farmacias.saldo.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nutripharma.api_nutripharma.organization.farmacias.saldo.domain.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SaldoMovimientoDTO {

    public record SaldoMovimientoResponse(
            Long id,
            TipoMovimiento tipo,
            BigDecimal importe,
            BigDecimal saldoResultante,
            String referenciaTipo,
            Long referenciaId,
            String nota,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime fecha,
            String creadoPor
    ) {}

    public record AjusteSaldoRequest(
            Double nuevoSaldo,
            String nota
    ) {}
}
