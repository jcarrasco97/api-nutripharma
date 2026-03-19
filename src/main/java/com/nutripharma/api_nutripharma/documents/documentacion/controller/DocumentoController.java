package com.nutripharma.api_nutripharma.documents.documentacion.controller;

import com.nutripharma.api_nutripharma.documents.documentacion.controller.dto.DocumentoDTO.*;
import com.nutripharma.api_nutripharma.documents.documentacion.domain.Documento;
import com.nutripharma.api_nutripharma.documents.documentacion.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentoController {

    private final DocumentoService service;

    // Subida simplificada (Sin título ni descripción)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentoResponse> subirDocumento(
            @RequestParam("alcance") String alcance,
            @RequestParam(value = "propietarioEmail", required = false) String propietarioEmail,
            @RequestParam("archivo") MultipartFile archivo) throws IOException, GeneralSecurityException {

        return ResponseEntity.ok(service.subirDocumento(alcance, propietarioEmail, archivo));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('NUTRICIONISTA') or hasRole('FARMACIA')")
    public ResponseEntity<List<DocumentoResponse>> listarMisDocumentos(Principal principal) {
        return ResponseEntity.ok(service.listarMisDocumentos(principal.getName()));
    }

    @GetMapping("/{id}/descargar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('NUTRICIONISTA') or hasRole('FARMACIA')")
    public ResponseEntity<byte[]> descargarDocumento(@PathVariable Long id) throws IOException, GeneralSecurityException {
        Documento meta = service.obtenerMetadatos(id);
        byte[] archivoFisico = service.descargarDocumento(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + meta.getNombreOriginal() + "\"")
                .contentType(MediaType.parseMediaType(meta.getMimeType()))
                .body(archivoFisico);
    }

    // 👇 RUTA NUEVA: Borrado definitivo 👇
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarDocumento(@PathVariable Long id) throws IOException, GeneralSecurityException {
        service.eliminarDocumento(id);
        return ResponseEntity.noContent().build();
    }

    // 👇 RUTA NUEVA: Para el desplegable de usuarios 👇
    @GetMapping("/destinatarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioDestinatarioProjection>> obtenerDestinatarios() {
        return ResponseEntity.ok(service.obtenerUsuariosParaDesplegable());
    }
}