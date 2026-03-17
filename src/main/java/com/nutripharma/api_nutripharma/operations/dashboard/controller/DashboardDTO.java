package com.nutripharma.api_nutripharma.operations.dashboard.controller;

import java.math.BigDecimal;

public class DashboardDTO {

    public record ResumenMensualNutricionista(
            String mes,
            int anio,
            // Bloque de Horas
            double horasTrabajadas,
            int horasContrato,
            double balanceHoras, // Positivo (a favor) o Negativo (debe)
            // Bloque Clínico
            int totalNuevas,
            int totalRevisiones,
            int totalPromocionales,
            int totalPersonalFarmacia,
            // Bloque Financiero
            BigDecimal volumenVentasEuros,
            BigDecimal bonusEstimadoEuros
    ) {}
}