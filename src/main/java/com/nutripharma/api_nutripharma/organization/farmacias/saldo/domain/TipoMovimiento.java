package com.nutripharma.api_nutripharma.organization.farmacias.saldo.domain;

public enum TipoMovimiento {
    INGRESO,       // consulta validada → saldo sube
    GASTO,         // pedido creado con saldo → saldo baja
    DEVOLUCION,    // pedido cancelado → saldo vuelve a la farmacia
    REVERSION,     // consulta cancelada/editada → ingreso previo anulado
    AJUSTE_MANUAL  // corrección manual por administrador
}
