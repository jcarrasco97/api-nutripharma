// --- controller/DocumentoController.java ---
package com.nutripharma.api_nutripharma.documents.documentacion.controller;
import com.nutripharma.api_nutripharma.documents.documentacion.controller.dto.DocumentoDTO.*;
import com.nutripharma.api_nutripharma.documents.documentacion.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {
    private final DocumentoService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentoResponse> subirDocumento(@RequestBody DocumentoRequest req) { return ResponseEntity.ok(service.subirDocumento(req)); }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('NUTRICIONISTA') or hasRole('FARMACIA')")
    public ResponseEntity<List<DocumentoResponse>> listarTodos() { return ResponseEntity.ok(service.listarTodos()); }
}