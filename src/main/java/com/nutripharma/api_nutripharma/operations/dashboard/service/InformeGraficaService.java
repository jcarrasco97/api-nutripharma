package com.nutripharma.api_nutripharma.operations.dashboard.service;

import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO;
import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO.FacturacionMensualDTO;
import lombok.RequiredArgsConstructor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;

import org.springframework.stereotype.Service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Generación server-side de gráficas para embeber en informes PDF.
 * Usa JFreeChart para renderizar barras agrupadas comparando dos años (YoY).
 */
@Service
@RequiredArgsConstructor
public class InformeGraficaService {

    private static final String[] MESES_LABELS = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    private static final Color COLOR_ANIO_ANTERIOR = new Color(0xCD, 0x5C, 0x5C); // rojo
    private static final Color COLOR_ANIO_ACTUAL = new Color(0x6A, 0x82, 0xB6);   // azul

    /**
     * Genera una gráfica de barras agrupadas mes-a-mes comparando dos años.
     *
     * @param reporte reporte jerárquico cuyo {@code desglosesPorAnio} debe contener exactamente 2 entradas.
     * @return PNG en bytes (1200x400)
     * @throws IllegalArgumentException si el reporte no tiene exactamente 2 desgloses anuales.
     */
    public byte[] generarGraficaFacturacionYoY(
            DashboardDTO.ReporteJerarquicoDTO<List<FacturacionMensualDTO>> reporte) {

        if (reporte == null || reporte.desglosesPorAnio() == null || reporte.desglosesPorAnio().size() != 2) {
            throw new IllegalArgumentException("Se requieren exactamente 2 años para la gráfica YoY");
        }

        DashboardDTO.DesgloseAnualDTO<List<FacturacionMensualDTO>> desgloseAnterior = reporte.desglosesPorAnio().get(0);
        DashboardDTO.DesgloseAnualDTO<List<FacturacionMensualDTO>> desgloseActual = reporte.desglosesPorAnio().get(1);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String serieAnterior = String.valueOf(desgloseAnterior.anio());
        String serieActual = String.valueOf(desgloseActual.anio());

        double[] valoresAnterior = extraerTotalesMensuales(desgloseAnterior.datos());
        double[] valoresActual = extraerTotalesMensuales(desgloseActual.datos());
        for (int m = 0; m < 12; m++) {
            dataset.addValue(valoresAnterior[m], serieAnterior, MESES_LABELS[m]);
            dataset.addValue(valoresActual[m], serieActual, MESES_LABELS[m]);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Comparativa de Facturación " + serieAnterior + " vs " + serieActual,
                "Mes",
                "Euros (€)",
                dataset,
                PlotOrientation.VERTICAL,
                true,   // legend
                false,  // tooltips
                false   // urls
        );

        aplicarEstilos(chart);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ChartUtils.writeChartAsPNG(baos, chart, 1200, 400);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generando PNG de la gráfica YoY: " + e.getMessage(), e);
        }
    }

    private double[] extraerTotalesMensuales(List<FacturacionMensualDTO> datos) {
        double[] arr = new double[12];
        if (datos == null) return arr;
        for (FacturacionMensualDTO d : datos) {
            int mes = d.mesNumero();
            if (mes >= 1 && mes <= 12) {
                BigDecimal v = d.totalBruto();
                arr[mes - 1] = v != null ? v.doubleValue() : 0.0;
            }
        }
        return arr;
    }

    private void aplicarEstilos(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 16));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(250, 250, 250));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setOutlineVisible(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, COLOR_ANIO_ANTERIOR);
        renderer.setSeriesPaint(1, COLOR_ANIO_ACTUAL);
        renderer.setShadowVisible(false);
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setItemMargin(0.05);
        renderer.setDefaultOutlineStroke(new BasicStroke(0f));

        CategoryAxis xAxis = plot.getDomainAxis();
        xAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        xAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        yAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));

        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(new Font("SansSerif", Font.PLAIN, 12));
            legend.setBorder(0, 0, 0, 0);
        }
    }
}
