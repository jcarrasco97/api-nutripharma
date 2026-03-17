// --- controller/DocumentoDTO.java ---
package com.nutripharma.api_nutripharma.documents.documentacion.controller.dto;
import java.time.LocalDate;
public class DocumentoDTO {
    public record DocumentoRequest(String titulo, String descripcion, String urlDescarga) {}
    public record DocumentoResponse(Long id, String titulo, String descripcion, String urlDescarga, LocalDate fechaSubida) {}
}