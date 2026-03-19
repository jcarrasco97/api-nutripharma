package com.nutripharma.api_nutripharma.documents.documentacion.controller.dto;

import com.nutripharma.api_nutripharma.documents.documentacion.domain.AlcanceDocumento;
import java.time.LocalDate;

public class DocumentoDTO {

    // Respuesta para la tabla (sin titulo ni descripcion, ahora usa nombreOriginal)
    public record DocumentoResponse(
            Long id,
            String nombreOriginal,
            AlcanceDocumento alcance,
            String propietarioEmail,
            String propietarioNombre, // Añadido para mostrar "Juan Pérez" en lugar de su email
            LocalDate fechaSubida
    ) {}

    // Esta interfaz captura los datos nativos de la base de datos para el desplegable
    public interface UsuarioDestinatarioProjection {
        String getEmail();
        String getNombreCompleto();
    }
}