package com.nutripharma.api_nutripharma.sales.pedidos.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@Service
public class FacturaPdfService {

    public byte[] generarPdfFactura(Long pedidoId, String nombreFarmacia, double total) {
        // Tubería en memoria RAM para guardar el PDF sin tocar el disco duro
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Inicializamos el documento OpenPDF
        Document document = new Document();
        PdfWriter.getInstance(document, out);

        document.open();

        // --- AQUÍ DIBUJAREMOS EL PDF EN EL FUTURO ---
        Font fontTitulo = new Font(Font.HELVETICA, 24, Font.BOLD);
        Font fontNormal = new Font(Font.HELVETICA, 12, Font.NORMAL);

        document.add(new Paragraph("FACTURA COMERCIAL", fontTitulo));
        document.add(new Paragraph(" ")); // Salto de línea
        document.add(new Paragraph("Pedido #: " + pedidoId, fontNormal));
        document.add(new Paragraph("Fecha: " + LocalDate.now().toString(), fontNormal));
        document.add(new Paragraph("Cliente: " + nombreFarmacia, fontNormal));
        document.add(new Paragraph("Total a Pagar: " + total + " €", fontNormal));
        // ---------------------------------------------

        document.close();

        return out.toByteArray(); // Devolvemos el archivo binario final
    }
}