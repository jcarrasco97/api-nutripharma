package com.nutripharma.api_nutripharma.operations.dashboard.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DashboardDTO {

    // (Tu DTO actual para Nutricionistas)
    public record ResumenMensualNutricionista(
            String mes,
            int anio,
            double horasTrabajadas,
            int horasContrato,
            double balanceHoras,
            int totalNuevas,
            int totalRevisiones,
            int totalPromocionales,
            int totalPersonalFarmacia,
            BigDecimal volumenVentasEuros,
            BigDecimal bonusEstimadoEuros
    ) {}

    // 👇 NUEVO: DTO para la Gráfica del Admin 👇
    public record FacturacionMensualDTO(
            String mesTexto,
            int mesNumero,
            BigDecimal ingresosConsultas,
            BigDecimal ingresosPedidos,
            BigDecimal totalBruto
    ) {}

    // 👇 NUEVO: DTO para el Calendario del Admin 👇
    public record EventoCalendarioDTO(
            String idUnico,   // Ej: "PED-12" o "CON-45"
            String tipo,      // "PEDIDO" o "CONSULTA"
            String titulo,    // Ej: "Pedido de Farmacia Centro"
            LocalDate fecha,
            String estado,    // Ej: "PENDIENTE_LIQUIDAR", "BORRADOR"
            String detalles   // Ej: "140.50€" o "Turno: MAÑANA"
    ) {}

    // 👇 NUEVO: DTO para el Modo Auditoría del Admin 👇
    public record AuditoriaNutriDTO(
            int totalKilometros,
            int totalConsultas,
            int cantidadPedidos,
            BigDecimal facturacionConsultas,
            BigDecimal facturacionProductos
    ) {}
}