package com.nutripharma.api_nutripharma.operations.dashboard.service;

import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO;
import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO.FacturacionMensualDTO;
import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO.InformePdfRequestDTO;
import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO.RendimientoClinicoDTO;
import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO.RendimientoProductoDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Servicio de generación de informes Excel (.xlsx) con Apache POI.
 * Reutiliza la lógica de cálculo ya consolidada en {@link DashboardService}.
 *
 * Convenciones de estilo:
 *  - Cabecera: fondo verde corporativo (#062e3a) con texto blanco en negrita.
 *  - Primera fila congelada (freeze pane) y todas las columnas auto-ajustadas.
 *  - Nombres de hojas truncados a 31 caracteres (límite de Excel).
 */
@Service
@RequiredArgsConstructor
public class InformeExcelService {

    private final DashboardService dashboardService;

    // ─────────────────────────────────────────────────────────────────────────
    // 1. EXCEL: RENDIMIENTO DE PRODUCTOS
    // ─────────────────────────────────────────────────────────────────────────

    public byte[] generarInformeRendimientoProductosExcel(InformePdfRequestDTO request) {
        DashboardDTO.ReporteJerarquicoDTO<List<RendimientoProductoDTO>> reporte =
                dashboardService.obtenerRendimientoProductosRango(
                        request.getAnioInicio(), request.getAnioFin(),
                        request.getMes(), request.getFarmaciaId(), request.getNutricionistaId());

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle estiloCabecera = crearEstiloCabecera(workbook);
            CellStyle estiloTotal = crearEstiloTotal(workbook);

            // Hoja por cada año (solo si hay desglose y es un rango)
            if (request.getAnioInicio() != request.getAnioFin() && reporte.desglosesPorAnio() != null) {
                for (DashboardDTO.DesgloseAnualDTO<List<RendimientoProductoDTO>> desglose : reporte.desglosesPorAnio()) {
                    Sheet hoja = workbook.createSheet(safeSheetName("Productos " + desglose.anio()));
                    renderizarHojaProductos(hoja, desglose.datos(), estiloCabecera, estiloTotal);
                }
            }

            // Hoja "Totales" (siempre presente)
            String nombreTotales = request.getAnioInicio() == request.getAnioFin()
                    ? "Totales " + request.getAnioInicio()
                    : "Totales " + request.getAnioInicio() + "-" + request.getAnioFin();
            Sheet hojaTotales = workbook.createSheet(safeSheetName(nombreTotales));
            renderizarHojaProductos(hojaTotales, reporte.totalesRango(), estiloCabecera, estiloTotal);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generando Excel de productos: " + e.getMessage(), e);
        }
    }

    private void renderizarHojaProductos(Sheet hoja, List<RendimientoProductoDTO> datos,
                                          CellStyle estiloCabecera, CellStyle estiloTotal) {
        String[] cabeceras = {"Producto", "Uds. Vendidas", "PVF Unitario", "PVP Unitario", "Total PVF", "Total PVP"};
        Row filaCabecera = hoja.createRow(0);
        for (int i = 0; i < cabeceras.length; i++) {
            Cell celda = filaCabecera.createCell(i);
            celda.setCellValue(cabeceras[i]);
            celda.setCellStyle(estiloCabecera);
        }

        long totalUds = 0;
        BigDecimal totalPvf = BigDecimal.ZERO;
        BigDecimal totalPvp = BigDecimal.ZERO;

        int filaIdx = 1;
        if (datos != null) {
            for (RendimientoProductoDTO row : datos) {
                Row fila = hoja.createRow(filaIdx++);
                fila.createCell(0).setCellValue(row.productoNombre() != null ? row.productoNombre() : "");
                fila.createCell(1).setCellValue(row.cantidadVendida() != null ? row.cantidadVendida() : 0);
                fila.createCell(2).setCellValue(row.precioVentaFarmacia() != null ? row.precioVentaFarmacia().doubleValue() : 0.0);
                fila.createCell(3).setCellValue(row.precioVentaPublico() != null ? row.precioVentaPublico().doubleValue() : 0.0);
                fila.createCell(4).setCellValue(row.ingresosGeneradosPvf() != null ? row.ingresosGeneradosPvf().doubleValue() : 0.0);
                fila.createCell(5).setCellValue(row.ingresosPotencialesPvp() != null ? row.ingresosPotencialesPvp().doubleValue() : 0.0);

                totalUds += row.cantidadVendida() != null ? row.cantidadVendida() : 0L;
                if (row.ingresosGeneradosPvf() != null) totalPvf = totalPvf.add(row.ingresosGeneradosPvf());
                if (row.ingresosPotencialesPvp() != null) totalPvp = totalPvp.add(row.ingresosPotencialesPvp());
            }
        }

        // Fila TOTAL
        Row filaTotal = hoja.createRow(filaIdx);
        Cell cTotal = filaTotal.createCell(0);
        cTotal.setCellValue("TOTAL");
        cTotal.setCellStyle(estiloTotal);
        Cell cUds = filaTotal.createCell(1);
        cUds.setCellValue(totalUds);
        cUds.setCellStyle(estiloTotal);
        for (int i = 2; i <= 3; i++) {
            Cell c = filaTotal.createCell(i);
            c.setCellValue("");
            c.setCellStyle(estiloTotal);
        }
        Cell cPvf = filaTotal.createCell(4);
        cPvf.setCellValue(totalPvf.setScale(2, RoundingMode.HALF_UP).doubleValue());
        cPvf.setCellStyle(estiloTotal);
        Cell cPvp = filaTotal.createCell(5);
        cPvp.setCellValue(totalPvp.setScale(2, RoundingMode.HALF_UP).doubleValue());
        cPvp.setCellStyle(estiloTotal);

        autoAjustarYCongelar(hoja, cabeceras.length);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. EXCEL: FACTURACIÓN (el más importante — incluye comparativa YoY)
    // ─────────────────────────────────────────────────────────────────────────

    public byte[] generarInformeFacturacionExcel(InformePdfRequestDTO request) {
        DashboardDTO.ReporteJerarquicoDTO<List<FacturacionMensualDTO>> reporte =
                dashboardService.obtenerFacturacionRango(
                        request.getAnioInicio(), request.getAnioFin(),
                        request.getFarmaciaId(), request.getNutricionistaId());

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle estiloCabecera = crearEstiloCabecera(workbook);
            CellStyle estiloTotal = crearEstiloTotal(workbook);
            CellStyle estiloVerde = crearEstiloPorcentajeColor(workbook, new Color(46, 125, 50));   // verde
            CellStyle estiloRojo = crearEstiloPorcentajeColor(workbook, new Color(198, 40, 40));    // rojo
            CellStyle estiloNeutro = crearEstiloPorcentaje(workbook);

            List<DashboardDTO.DesgloseAnualDTO<List<FacturacionMensualDTO>>> desgloses = reporte.desglosesPorAnio();

            // Hoja "Comparativa YoY" si hay 2+ años
            if (desgloses != null && desgloses.size() >= 2) {
                DashboardDTO.DesgloseAnualDTO<List<FacturacionMensualDTO>> anio1 = desgloses.get(0);
                DashboardDTO.DesgloseAnualDTO<List<FacturacionMensualDTO>> anio2 = desgloses.get(desgloses.size() - 1);
                Sheet hojaYoY = workbook.createSheet(safeSheetName("Comparativa YoY"));
                renderizarHojaComparativaYoY(hojaYoY, anio1, anio2, estiloCabecera, estiloTotal,
                        estiloVerde, estiloRojo, estiloNeutro);
            }

            // Hoja por cada año
            if (desgloses != null) {
                for (DashboardDTO.DesgloseAnualDTO<List<FacturacionMensualDTO>> desglose : desgloses) {
                    Sheet hoja = workbook.createSheet(safeSheetName("Facturacion " + desglose.anio()));
                    renderizarHojaFacturacion(hoja, desglose.datos(), estiloCabecera, estiloTotal);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generando Excel de facturación: " + e.getMessage(), e);
        }
    }

    private void renderizarHojaFacturacion(Sheet hoja, List<FacturacionMensualDTO> datos,
                                            CellStyle estiloCabecera, CellStyle estiloTotal) {
        String[] cabeceras = {"Mes", "Ingresos Consultas", "Ingresos Productos", "Total Facturado"};
        Row filaCabecera = hoja.createRow(0);
        for (int i = 0; i < cabeceras.length; i++) {
            Cell celda = filaCabecera.createCell(i);
            celda.setCellValue(cabeceras[i]);
            celda.setCellStyle(estiloCabecera);
        }

        BigDecimal totalConsultas = BigDecimal.ZERO;
        BigDecimal totalProductos = BigDecimal.ZERO;
        BigDecimal totalBruto = BigDecimal.ZERO;

        int filaIdx = 1;
        if (datos != null) {
            for (FacturacionMensualDTO row : datos) {
                Row fila = hoja.createRow(filaIdx++);
                fila.createCell(0).setCellValue(row.mesTexto() != null ? row.mesTexto() : "");
                fila.createCell(1).setCellValue(row.ingresosConsultas() != null ? row.ingresosConsultas().doubleValue() : 0.0);
                fila.createCell(2).setCellValue(row.ingresosPedidos() != null ? row.ingresosPedidos().doubleValue() : 0.0);
                fila.createCell(3).setCellValue(row.totalBruto() != null ? row.totalBruto().doubleValue() : 0.0);

                if (row.ingresosConsultas() != null) totalConsultas = totalConsultas.add(row.ingresosConsultas());
                if (row.ingresosPedidos() != null) totalProductos = totalProductos.add(row.ingresosPedidos());
                if (row.totalBruto() != null) totalBruto = totalBruto.add(row.totalBruto());
            }
        }

        Row filaTotal = hoja.createRow(filaIdx);
        Cell c0 = filaTotal.createCell(0);
        c0.setCellValue("TOTAL");
        c0.setCellStyle(estiloTotal);
        Cell c1 = filaTotal.createCell(1);
        c1.setCellValue(totalConsultas.setScale(2, RoundingMode.HALF_UP).doubleValue());
        c1.setCellStyle(estiloTotal);
        Cell c2 = filaTotal.createCell(2);
        c2.setCellValue(totalProductos.setScale(2, RoundingMode.HALF_UP).doubleValue());
        c2.setCellStyle(estiloTotal);
        Cell c3 = filaTotal.createCell(3);
        c3.setCellValue(totalBruto.setScale(2, RoundingMode.HALF_UP).doubleValue());
        c3.setCellStyle(estiloTotal);

        autoAjustarYCongelar(hoja, cabeceras.length);
    }

    private void renderizarHojaComparativaYoY(Sheet hoja,
                                               DashboardDTO.DesgloseAnualDTO<List<FacturacionMensualDTO>> anio1,
                                               DashboardDTO.DesgloseAnualDTO<List<FacturacionMensualDTO>> anio2,
                                               CellStyle estiloCabecera, CellStyle estiloTotal,
                                               CellStyle estiloVerde, CellStyle estiloRojo, CellStyle estiloNeutro) {
        // Fila 0: super-cabecera agrupada
        Row superCab = hoja.createRow(0);
        Cell celdaMes = superCab.createCell(0);
        celdaMes.setCellValue("");
        celdaMes.setCellStyle(estiloCabecera);
        Cell celdaA1 = superCab.createCell(1);
        celdaA1.setCellValue("Año " + anio1.anio());
        celdaA1.setCellStyle(estiloCabecera);
        Cell celdaA2 = superCab.createCell(4);
        celdaA2.setCellValue("Año " + anio2.anio());
        celdaA2.setCellStyle(estiloCabecera);
        Cell celdaDelta = superCab.createCell(7);
        celdaDelta.setCellValue("");
        celdaDelta.setCellStyle(estiloCabecera);
        hoja.addMergedRegion(new CellRangeAddress(0, 0, 1, 3));
        hoja.addMergedRegion(new CellRangeAddress(0, 0, 4, 6));

        // Fila 1: cabecera detallada
        String[] cabeceras = {"Mes",
                "Consultas", "Ventas", "Total",
                "Consultas", "Ventas", "Total",
                "Δ% Total"};
        Row filaCab = hoja.createRow(1);
        for (int i = 0; i < cabeceras.length; i++) {
            Cell c = filaCab.createCell(i);
            c.setCellValue(cabeceras[i]);
            c.setCellStyle(estiloCabecera);
        }

        // Indexar por mes (1-12) para ambos años
        FacturacionMensualDTO[] datosA1 = indexarPorMes(anio1.datos());
        FacturacionMensualDTO[] datosA2 = indexarPorMes(anio2.datos());

        BigDecimal totA1Consultas = BigDecimal.ZERO, totA1Ventas = BigDecimal.ZERO, totA1Total = BigDecimal.ZERO;
        BigDecimal totA2Consultas = BigDecimal.ZERO, totA2Ventas = BigDecimal.ZERO, totA2Total = BigDecimal.ZERO;

        for (int m = 1; m <= 12; m++) {
            Row fila = hoja.createRow(1 + m);
            FacturacionMensualDTO d1 = datosA1[m];
            FacturacionMensualDTO d2 = datosA2[m];

            String mesTexto = d1 != null && d1.mesTexto() != null ? d1.mesTexto()
                    : (d2 != null && d2.mesTexto() != null ? d2.mesTexto() : String.valueOf(m));
            fila.createCell(0).setCellValue(mesTexto);

            BigDecimal a1Cons = bd(d1 != null ? d1.ingresosConsultas() : null);
            BigDecimal a1Vent = bd(d1 != null ? d1.ingresosPedidos() : null);
            BigDecimal a1Tot  = bd(d1 != null ? d1.totalBruto() : null);
            BigDecimal a2Cons = bd(d2 != null ? d2.ingresosConsultas() : null);
            BigDecimal a2Vent = bd(d2 != null ? d2.ingresosPedidos() : null);
            BigDecimal a2Tot  = bd(d2 != null ? d2.totalBruto() : null);

            fila.createCell(1).setCellValue(a1Cons.doubleValue());
            fila.createCell(2).setCellValue(a1Vent.doubleValue());
            fila.createCell(3).setCellValue(a1Tot.doubleValue());
            fila.createCell(4).setCellValue(a2Cons.doubleValue());
            fila.createCell(5).setCellValue(a2Vent.doubleValue());
            fila.createCell(6).setCellValue(a2Tot.doubleValue());

            double delta = calcularDeltaPorcentual(a1Tot, a2Tot);
            Cell celdaDeltaFila = fila.createCell(7);
            celdaDeltaFila.setCellValue(delta / 100.0); // formato porcentaje en Excel
            if (delta > 0)      celdaDeltaFila.setCellStyle(estiloVerde);
            else if (delta < 0) celdaDeltaFila.setCellStyle(estiloRojo);
            else                celdaDeltaFila.setCellStyle(estiloNeutro);

            totA1Consultas = totA1Consultas.add(a1Cons);
            totA1Ventas    = totA1Ventas.add(a1Vent);
            totA1Total     = totA1Total.add(a1Tot);
            totA2Consultas = totA2Consultas.add(a2Cons);
            totA2Ventas    = totA2Ventas.add(a2Vent);
            totA2Total     = totA2Total.add(a2Tot);
        }

        // Fila TOTAL
        Row filaTotal = hoja.createRow(14);
        Cell t0 = filaTotal.createCell(0); t0.setCellValue("TOTAL"); t0.setCellStyle(estiloTotal);
        aplicarTotal(filaTotal, 1, totA1Consultas, estiloTotal);
        aplicarTotal(filaTotal, 2, totA1Ventas, estiloTotal);
        aplicarTotal(filaTotal, 3, totA1Total, estiloTotal);
        aplicarTotal(filaTotal, 4, totA2Consultas, estiloTotal);
        aplicarTotal(filaTotal, 5, totA2Ventas, estiloTotal);
        aplicarTotal(filaTotal, 6, totA2Total, estiloTotal);

        double deltaTotal = calcularDeltaPorcentual(totA1Total, totA2Total);
        Cell celdaDeltaTotal = filaTotal.createCell(7);
        celdaDeltaTotal.setCellValue(deltaTotal / 100.0);
        if (deltaTotal > 0)      celdaDeltaTotal.setCellStyle(estiloVerde);
        else if (deltaTotal < 0) celdaDeltaTotal.setCellStyle(estiloRojo);
        else                     celdaDeltaTotal.setCellStyle(estiloNeutro);

        autoAjustarYCongelar(hoja, cabeceras.length);
        // Congelamos las DOS primeras filas (super-cabecera + cabecera) en lugar de solo 1
        hoja.createFreezePane(0, 2);
    }

    private FacturacionMensualDTO[] indexarPorMes(List<FacturacionMensualDTO> lista) {
        FacturacionMensualDTO[] arr = new FacturacionMensualDTO[13]; // índice 1..12
        if (lista == null) return arr;
        for (FacturacionMensualDTO d : lista) {
            if (d.mesNumero() >= 1 && d.mesNumero() <= 12) arr[d.mesNumero()] = d;
        }
        return arr;
    }

    private double calcularDeltaPorcentual(BigDecimal base, BigDecimal actual) {
        if (base == null || base.signum() == 0) {
            return actual != null && actual.signum() != 0 ? 100.0 : 0.0;
        }
        BigDecimal delta = actual.subtract(base)
                .divide(base, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return delta.doubleValue();
    }

    private void aplicarTotal(Row fila, int col, BigDecimal valor, CellStyle estilo) {
        Cell c = fila.createCell(col);
        c.setCellValue(valor.setScale(2, RoundingMode.HALF_UP).doubleValue());
        c.setCellStyle(estilo);
    }

    private BigDecimal bd(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. EXCEL: RENDIMIENTO CLÍNICO
    // ─────────────────────────────────────────────────────────────────────────

    public byte[] generarInformeClinicoExcel(InformePdfRequestDTO request) {
        DashboardDTO.ReporteJerarquicoDTO<List<RendimientoClinicoDTO>> reporte =
                dashboardService.obtenerRendimientoClinicoRango(
                        request.getAnioInicio(), request.getAnioFin(),
                        request.getMes(), request.getNutricionistaId());

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle estiloCabecera = crearEstiloCabecera(workbook);
            CellStyle estiloTotal = crearEstiloTotal(workbook);

            String nombre = request.getAnioInicio() == request.getAnioFin()
                    ? "Clinico " + request.getAnioInicio()
                    : "Clinico " + request.getAnioInicio() + "-" + request.getAnioFin();
            Sheet hoja = workbook.createSheet(safeSheetName(nombre));
            renderizarHojaClinico(hoja, reporte.totalesRango(), estiloCabecera, estiloTotal);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generando Excel clínico: " + e.getMessage(), e);
        }
    }

    private void renderizarHojaClinico(Sheet hoja, List<RendimientoClinicoDTO> datos,
                                        CellStyle estiloCabecera, CellStyle estiloTotal) {
        String[] cabeceras = {"Farmacia", "Nuevas", "Revisiones", "Promocionales", "Personal", "Ingresos Est."};
        Row filaCabecera = hoja.createRow(0);
        for (int i = 0; i < cabeceras.length; i++) {
            Cell celda = filaCabecera.createCell(i);
            celda.setCellValue(cabeceras[i]);
            celda.setCellStyle(estiloCabecera);
        }

        long totNuevas = 0, totRev = 0, totProm = 0, totPers = 0;
        BigDecimal totIngresos = BigDecimal.ZERO;

        int filaIdx = 1;
        if (datos != null) {
            for (RendimientoClinicoDTO row : datos) {
                Row fila = hoja.createRow(filaIdx++);
                fila.createCell(0).setCellValue(row.farmaciaNombre() != null ? row.farmaciaNombre() : "");
                fila.createCell(1).setCellValue(row.nuevas());
                fila.createCell(2).setCellValue(row.revisiones());
                fila.createCell(3).setCellValue(row.promocionales());
                fila.createCell(4).setCellValue(row.personal());
                fila.createCell(5).setCellValue(row.ingresosGenerados() != null ? row.ingresosGenerados().doubleValue() : 0.0);

                totNuevas += row.nuevas();
                totRev    += row.revisiones();
                totProm   += row.promocionales();
                totPers   += row.personal();
                if (row.ingresosGenerados() != null) totIngresos = totIngresos.add(row.ingresosGenerados());
            }
        }

        Row filaTotal = hoja.createRow(filaIdx);
        Cell c0 = filaTotal.createCell(0); c0.setCellValue("TOTAL");      c0.setCellStyle(estiloTotal);
        Cell c1 = filaTotal.createCell(1); c1.setCellValue(totNuevas);    c1.setCellStyle(estiloTotal);
        Cell c2 = filaTotal.createCell(2); c2.setCellValue(totRev);       c2.setCellStyle(estiloTotal);
        Cell c3 = filaTotal.createCell(3); c3.setCellValue(totProm);      c3.setCellStyle(estiloTotal);
        Cell c4 = filaTotal.createCell(4); c4.setCellValue(totPers);      c4.setCellStyle(estiloTotal);
        Cell c5 = filaTotal.createCell(5);
        c5.setCellValue(totIngresos.setScale(2, RoundingMode.HALF_UP).doubleValue());
        c5.setCellStyle(estiloTotal);

        autoAjustarYCongelar(hoja, cabeceras.length);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILIDADES DE ESTILO Y FORMATO
    // ─────────────────────────────────────────────────────────────────────────

    private CellStyle crearEstiloCabecera(XSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        // Color corporativo #062e3a
        estilo.setFillForegroundColor(new XSSFColor(new Color(6, 46, 58), null));
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font fuente = workbook.createFont();
        fuente.setColor(IndexedColors.WHITE.getIndex());
        fuente.setBold(true);
        estilo.setFont(fuente);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        aplicarBordes(estilo);
        return estilo;
    }

    private CellStyle crearEstiloTotal(XSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setFillForegroundColor(new XSSFColor(new Color(232, 240, 234), null));
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        estilo.setFont(fuente);
        aplicarBordes(estilo);
        return estilo;
    }

    private CellStyle crearEstiloPorcentaje(XSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        DataFormat formato = workbook.createDataFormat();
        estilo.setDataFormat(formato.getFormat("0.00%"));
        aplicarBordes(estilo);
        return estilo;
    }

    private CellStyle crearEstiloPorcentajeColor(XSSFWorkbook workbook, Color color) {
        CellStyle estilo = crearEstiloPorcentaje(workbook);
        org.apache.poi.xssf.usermodel.XSSFFont fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setColor(new XSSFColor(color, null));
        estilo.setFont(fuente);
        return estilo;
    }

    private void aplicarBordes(CellStyle estilo) {
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
    }

    private void autoAjustarYCongelar(Sheet hoja, int numColumnas) {
        for (int i = 0; i < numColumnas; i++) {
            hoja.autoSizeColumn(i);
        }
        hoja.createFreezePane(0, 1);
    }

    /**
     * Trunca y sanea el nombre de la hoja para cumplir la limitación de Excel:
     * máximo 31 caracteres y sin los caracteres prohibidos \ / ? * [ ]
     */
    private String safeSheetName(String nombre) {
        if (nombre == null) return "Hoja";
        String limpio = nombre.replaceAll("[\\\\/\\?\\*\\[\\]:]", "_");
        return limpio.length() > 31 ? limpio.substring(0, 31) : limpio;
    }
}
