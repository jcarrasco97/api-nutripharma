package com.nutripharma.api_nutripharma.operations.dashboard.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO;
import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO.RendimientoProductoDTO;
import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO.FacturacionMensualDTO;
import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO.InformePdfRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InformePdfService {

    private final DashboardService dashboardService;
    private final InformeGraficaService informeGraficaService;

    public byte[] generarInformeRendimientoProductosPdf(InformePdfRequestDTO request) {
        DashboardDTO.ReporteJerarquicoDTO<List<RendimientoProductoDTO>> reporte = dashboardService.obtenerRendimientoProductosRango(
                request.getAnioInicio(), request.getAnioFin(), request.getMes(), request.getFarmaciaId(), request.getNutricionistaId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, out);
        document.open();

        try {
            agregarCabecera(document, "INFORME DE RENDIMIENTO DE PRODUCTOS", request);
            agregarGrafica(document, request.getGraficaBase64());

            Font fontSubtitulo = new Font(Font.HELVETICA, 12, Font.BOLD);
            String tituloTotales = request.getAnioInicio() == request.getAnioFin() 
                ? "TOTALES DEL AÑO " + request.getAnioInicio() 
                : "TOTALES DEL PERIODO " + request.getAnioInicio() + " - " + request.getAnioFin();
            
            document.add(new Paragraph(tituloTotales, fontSubtitulo));
            document.add(new Paragraph(" "));

            renderizarTablaProductos(document, reporte.totalesRango());

            if (request.getAnioInicio() != request.getAnioFin() && reporte.desglosesPorAnio() != null) {
                for (DashboardDTO.DesgloseAnualDTO<List<RendimientoProductoDTO>> desglose : reporte.desglosesPorAnio()) {
                    document.newPage();
                    document.add(new Paragraph("DESGLOSE AÑO " + desglose.anio(), fontSubtitulo));
                    document.add(new Paragraph(" "));
                    renderizarTablaProductos(document, desglose.datos());
                }
            }
        } catch (Exception e) {
            System.err.println("Error generando informe de productos: " + e.getMessage());
        }

        document.close();
        return out.toByteArray();
    }

    private void renderizarTablaProductos(Document document, List<RendimientoProductoDTO> datos) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        String[] cabeceras = {"Producto", "Uds. Vendidas", "PVF Unitario", "PVP Unitario", "Total Generado (PVF)", "Total Proyectado (PVP)"};

        Font fontCabecera = new Font(Font.HELVETICA, 10, Font.BOLD);
        for (String cabecera : cabeceras) {
            table.addCell(new PdfPCell(new Phrase(cabecera, fontCabecera)));
        }

        Font fontFila = new Font(Font.HELVETICA, 10, Font.NORMAL);
        for (RendimientoProductoDTO row : datos) {
            table.addCell(new Phrase(row.productoNombre(), fontFila));
            table.addCell(new Phrase(String.valueOf(row.cantidadVendida()), fontFila));
            table.addCell(new Phrase(row.precioVentaFarmacia() != null ? row.precioVentaFarmacia() + " €" : "-", fontFila));
            table.addCell(new Phrase(row.precioVentaPublico() != null ? row.precioVentaPublico() + " €" : "-", fontFila));
            table.addCell(new Phrase(row.ingresosGeneradosPvf() != null ? row.ingresosGeneradosPvf() + " €" : "0.00 €", fontFila));
            table.addCell(new Phrase(row.ingresosPotencialesPvp() != null ? row.ingresosPotencialesPvp() + " €" : "0.00 €", fontFila));
        }

        document.add(table);
    }

    public byte[] generarInformeFacturacionPdf(InformePdfRequestDTO request) {
        DashboardDTO.ReporteJerarquicoDTO<List<FacturacionMensualDTO>> reporte = dashboardService.obtenerFacturacionRango(
                request.getAnioInicio(), request.getAnioFin(), request.getFarmaciaId(), request.getNutricionistaId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        try {
            agregarCabecera(document, "RESUMEN DE FACTURACIÓN Y CONVERSIÓN", request);

            // Gráfica: si hay rango de 2 años usamos server-side (YoY).
            // Si el cliente no envía graficaBase64, también caemos a server-side cuando sea posible.
            boolean rangoDosAnios = request.getAnioInicio() != request.getAnioFin()
                    && reporte.desglosesPorAnio() != null
                    && reporte.desglosesPorAnio().size() == 2;
            boolean sinGraficaCliente = request.getGraficaBase64() == null || request.getGraficaBase64().isBlank();

            if (rangoDosAnios) {
                byte[] pngServerSide = informeGraficaService.generarGraficaFacturacionYoY(reporte);
                agregarGraficaDesdeBytes(document, pngServerSide);
            } else if (sinGraficaCliente) {
                // Sin gráfica disponible: omitimos.
            } else {
                agregarGrafica(document, request.getGraficaBase64());
            }

            Font fontSubtitulo = new Font(Font.HELVETICA, 12, Font.BOLD);
            
            if (reporte.desglosesPorAnio() != null) {
                boolean primera = true;
                for (DashboardDTO.DesgloseAnualDTO<List<FacturacionMensualDTO>> desglose : reporte.desglosesPorAnio()) {
                    if (!primera) {
                        document.newPage();
                    }
                    primera = false;
                    document.add(new Paragraph("DESGLOSE AÑO " + desglose.anio(), fontSubtitulo));
                    document.add(new Paragraph(" "));
                    renderizarTablaFacturacion(document, desglose.datos());
                }
            }
        } catch (Exception e) {
            System.err.println("Error generando informe de facturacion: " + e.getMessage());
        }

        document.close();
        return out.toByteArray();
    }

    private void renderizarTablaFacturacion(Document document, List<FacturacionMensualDTO> datos) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        String[] cabeceras = {"Mes", "Ingresos Consultas", "Ingresos Productos", "Total Facturado"};

        Font fontCabecera = new Font(Font.HELVETICA, 10, Font.BOLD);
        for (String cabecera : cabeceras) {
            table.addCell(new PdfPCell(new Phrase(cabecera, fontCabecera)));
        }

        Font fontFila = new Font(Font.HELVETICA, 10, Font.NORMAL);
        for (FacturacionMensualDTO row : datos) {
            table.addCell(new Phrase(row.mesTexto(), fontFila));
            table.addCell(new Phrase(row.ingresosConsultas() + " €", fontFila));
            table.addCell(new Phrase(row.ingresosPedidos() + " €", fontFila));
            table.addCell(new Phrase(row.totalBruto() + " €", fontFila));
        }

        document.add(table);
    }

    public byte[] generarInformeClinicoPdf(DashboardDTO.InformePdfRequestDTO request) {
        DashboardDTO.ReporteJerarquicoDTO<List<DashboardDTO.RendimientoClinicoDTO>> reporte = dashboardService.obtenerRendimientoClinicoRango(
                request.getAnioInicio(), request.getAnioFin(), request.getMes(), request.getNutricionistaId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, out);
        document.open();

        try {
            agregarCabecera(document, "ANÁLISIS CLÍNICO POR FARMACIA", request);
            agregarGrafica(document, request.getGraficaBase64());

            Font fontSubtitulo = new Font(Font.HELVETICA, 12, Font.BOLD);
            String tituloTotales = request.getAnioInicio() == request.getAnioFin() 
                ? "TOTALES DEL AÑO " + request.getAnioInicio() 
                : "TOTALES DEL PERIODO " + request.getAnioInicio() + " - " + request.getAnioFin();
            
            document.add(new Paragraph(tituloTotales, fontSubtitulo));
            document.add(new Paragraph(" "));

            renderizarTablaClinico(document, reporte.totalesRango());

            if (request.getAnioInicio() != request.getAnioFin() && reporte.desglosesPorAnio() != null) {
                for (DashboardDTO.DesgloseAnualDTO<List<DashboardDTO.RendimientoClinicoDTO>> desglose : reporte.desglosesPorAnio()) {
                    document.newPage();
                    document.add(new Paragraph("DESGLOSE AÑO " + desglose.anio(), fontSubtitulo));
                    document.add(new Paragraph(" "));
                    renderizarTablaClinico(document, desglose.datos());
                }
            }
        } catch (Exception e) {
            System.err.println("Error generando informe clinico: " + e.getMessage());
        }

        document.close();
        return out.toByteArray();
    }

    private void renderizarTablaClinico(Document document, List<DashboardDTO.RendimientoClinicoDTO> datos) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        String[] cabeceras = {"Farmacia", "Nuevas", "Revisiones", "Promocionales", "Personal", "Ingresos Est."};

        Font fontCabecera = new Font(Font.HELVETICA, 10, Font.BOLD);
        for (String cabecera : cabeceras) {
            table.addCell(new PdfPCell(new Phrase(cabecera, fontCabecera)));
        }

        Font fontFila = new Font(Font.HELVETICA, 10, Font.NORMAL);
        for (DashboardDTO.RendimientoClinicoDTO row : datos) {
            table.addCell(new Phrase(row.farmaciaNombre(), fontFila));
            table.addCell(new Phrase(String.valueOf(row.nuevas()), fontFila));
            table.addCell(new Phrase(String.valueOf(row.revisiones()), fontFila));
            table.addCell(new Phrase(String.valueOf(row.promocionales()), fontFila));
            table.addCell(new Phrase(String.valueOf(row.personal()), fontFila));
            table.addCell(new Phrase(row.ingresosGenerados() + " €", fontFila));
        }

        document.add(table);
    }

    private void agregarCabecera(Document document, String titulo, InformePdfRequestDTO req) throws DocumentException {
        Font fontTitulo = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font fontNormal = new Font(Font.HELVETICA, 10, Font.NORMAL);

        // Lógica para mostrar un año o un rango de años
        String textoAnio = (req.getAnioFin() > 0 && req.getAnioInicio() != req.getAnioFin())
                ? req.getAnioInicio() + " - " + req.getAnioFin()
                : String.valueOf(req.getAnioInicio());

        document.add(new Paragraph(titulo, fontTitulo));
        document.add(new Paragraph("Fecha de generación: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fontNormal));
        document.add(new Paragraph("Filtros aplicados - Año: " + textoAnio + (req.getMes() != null ? " | Mes: " + req.getMes() : ""), fontNormal));
        document.add(new Paragraph(" "));
    }

    private void agregarGrafica(Document document, String graficaBase64) {
        try {
            if (graficaBase64 != null && graficaBase64.contains(",")) {
                byte[] imageBytes = Base64.getDecoder().decode(graficaBase64.split(",")[1]);
                agregarGraficaDesdeBytes(document, imageBytes);
            }
        } catch (Exception e) {
            System.err.println("Error al incrustar gráfica: " + e.getMessage());
        }
    }

    private void agregarGraficaDesdeBytes(Document document, byte[] imageBytes) {
        try {
            if (imageBytes == null || imageBytes.length == 0) return;
            Image chartImage = Image.getInstance(imageBytes);

            float usableWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
            chartImage.scaleToFit(usableWidth, 500f);
            chartImage.setAlignment(Element.ALIGN_CENTER);

            document.add(chartImage);
            document.add(new Paragraph(" "));
        } catch (Exception e) {
            System.err.println("Error al incrustar gráfica desde bytes: " + e.getMessage());
        }
    }
}