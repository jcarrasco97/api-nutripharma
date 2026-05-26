package com.nutripharma.api_nutripharma.organization.farmacias.saldo.service;

import com.nutripharma.api_nutripharma.organization.farmacias.domain.Farmacia;
import com.nutripharma.api_nutripharma.organization.farmacias.saldo.controller.dto.SaldoMovimientoDTO.SaldoMovimientoResponse;
import com.nutripharma.api_nutripharma.organization.farmacias.saldo.domain.SaldoMovimiento;
import com.nutripharma.api_nutripharma.organization.farmacias.saldo.domain.TipoMovimiento;
import com.nutripharma.api_nutripharma.organization.farmacias.saldo.repository.SaldoMovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaldoMovimientoService {

    private final SaldoMovimientoRepository movimientoRepository;

    // Llamar DESPUÉS de actualizar farmacia.saldoVirtual
    public void registrarIngreso(Farmacia farmacia, BigDecimal importe, Long consultaId, String creadoPor) {
        guardar(farmacia, importe.abs(), saldoActual(farmacia),
                TipoMovimiento.INGRESO, "CONSULTA", consultaId, null, creadoPor);
    }

    // Llamar DESPUÉS de actualizar farmacia.saldoVirtual
    public void registrarGasto(Farmacia farmacia, BigDecimal importe, Long pedidoId, String creadoPor) {
        guardar(farmacia, importe.abs().negate(), saldoActual(farmacia),
                TipoMovimiento.GASTO, "PEDIDO", pedidoId, null, creadoPor);
    }

    // Llamar DESPUÉS de actualizar farmacia.saldoVirtual (pedido cancelado)
    public void registrarDevolucion(Farmacia farmacia, BigDecimal importe, Long pedidoId, String creadoPor) {
        guardar(farmacia, importe.abs(), saldoActual(farmacia),
                TipoMovimiento.DEVOLUCION, "PEDIDO", pedidoId, null, creadoPor);
    }

    // Llamar DESPUÉS de actualizar farmacia.saldoVirtual (consulta cancelada/editada)
    public void registrarReversion(Farmacia farmacia, BigDecimal importe, Long consultaId, String creadoPor) {
        guardar(farmacia, importe.abs().negate(), saldoActual(farmacia),
                TipoMovimiento.REVERSION, "CONSULTA", consultaId, null, creadoPor);
    }

    // Llamar DESPUÉS de actualizar farmacia.saldoVirtual
    public void registrarAjusteManual(Farmacia farmacia, BigDecimal diferencia,
                                      BigDecimal saldoResultante, String nota, String creadoPor) {
        guardar(farmacia, diferencia, saldoResultante,
                TipoMovimiento.AJUSTE_MANUAL, null, null, nota, creadoPor);
    }

    public List<SaldoMovimientoResponse> obtenerMovimientos(Long farmaciaId, boolean esFarmacia) {
        return movimientoRepository.findByFarmaciaIdOrderByFechaDesc(farmaciaId)
                .stream()
                .map(m -> toResponse(m, esFarmacia))
                .collect(Collectors.toList());
    }

    private void guardar(Farmacia farmacia, BigDecimal importe, BigDecimal saldoResultante,
                         TipoMovimiento tipo, String referenciaTipo, Long referenciaId,
                         String nota, String creadoPor) {
        movimientoRepository.save(SaldoMovimiento.builder()
                .farmacia(farmacia)
                .tipo(tipo)
                .importe(importe.setScale(2, RoundingMode.HALF_UP))
                .saldoResultante(saldoResultante.setScale(2, RoundingMode.HALF_UP))
                .referenciaTipo(referenciaTipo)
                .referenciaId(referenciaId)
                .nota(nota)
                .creadoPor(creadoPor)
                .build());
    }

    private BigDecimal saldoActual(Farmacia farmacia) {
        return BigDecimal.valueOf(farmacia.getSaldoVirtual() != null ? farmacia.getSaldoVirtual() : 0.0);
    }

    private SaldoMovimientoResponse toResponse(SaldoMovimiento m, boolean esFarmacia) {
        String nota = (esFarmacia && m.getTipo() == TipoMovimiento.AJUSTE_MANUAL)
                ? "Saldo ajustado por Nutripharma"
                : m.getNota();
        return new SaldoMovimientoResponse(
                m.getId(),
                m.getTipo(),
                m.getImporte(),
                m.getSaldoResultante(),
                m.getReferenciaTipo(),
                m.getReferenciaId(),
                nota,
                m.getFecha(),
                m.getCreadoPor()
        );
    }
}
