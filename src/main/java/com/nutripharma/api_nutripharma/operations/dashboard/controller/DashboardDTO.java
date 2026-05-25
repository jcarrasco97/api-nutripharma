package com.nutripharma.api_nutripharma.operations.dashboard.controller;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * DTO Maestro para el Dashboard Multidimensional.
 * Incluye estructuras jerárquicas para informes de rango y desgloses anuales.
 */
public class DashboardDTO {

        // --- ESTRUCTURAS JERÁRQUICAS (Para informes Desde-Hasta) ---

        public record ReporteJerarquicoDTO<T>(
                        T totalesRango,
                        List<DesgloseAnualDTO<T>> desglosesPorAnio) {
        }

        public record DesgloseAnualDTO<T>(
                        int anio,
                        T datos) {
        }

        // --- MODELOS DE DATOS ESPECÍFICOS ---

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
                        BigDecimal bonusEstimadoEuros) {
        }

        public record FacturacionMensualDTO(
                        String mesTexto,
                        int mesNumero,
                        BigDecimal ingresosConsultas,
                        BigDecimal ingresosPedidos,
                        BigDecimal totalBruto) {
        }

        public record EventoCalendarioDTO(
                        String idUnico,
                        String tipo,
                        String titulo,
                        LocalDate fecha,
                        String estado,
                        String detalles) {
        }

        public record RendimientoProductoDTO(
                        String productoNombre,
                        Long cantidadVendida,
                        BigDecimal precioVentaFarmacia,
                        BigDecimal precioVentaPublico,
                        BigDecimal ingresosGeneradosPvf,
                        BigDecimal ingresosPotencialesPvp) {
        }

        public record RendimientoClinicoDTO(
                        String farmaciaNombre,
                        int nuevas,
                        int revisiones,
                        int promocionales,
                        int personal,
                        BigDecimal ingresosGenerados) {
        }

        public record FacturacionPorFarmaciaDTO(
                        Long farmaciaId,
                        String farmaciaNombre,
                        ReporteJerarquicoDTO<List<FacturacionMensualDTO>> reporte) {
        }

        // --- CONFIGURACIÓN DE PETICIÓN (Request) ---

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class InformePdfRequestDTO {
                @JsonAlias("anio") // Compatibilidad con el front antiguo
                private int anioInicio;
                private int anioFin;
                private Integer mes;
                private Long farmaciaId;
                private Long nutricionistaId;
                private String tipoInforme;
                private String graficaBase64;
                private boolean incluirPromocionales;
                private boolean incluirPersonal;
        }
}