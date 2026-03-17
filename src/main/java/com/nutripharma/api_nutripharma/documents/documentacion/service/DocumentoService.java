// --- service/DocumentoService.java ---
package com.nutripharma.api_nutripharma.documents.documentacion.service;
import com.nutripharma.api_nutripharma.documents.documentacion.controller.dto.DocumentoDTO.*;
import com.nutripharma.api_nutripharma.documents.documentacion.domain.Documento;
import com.nutripharma.api_nutripharma.documents.documentacion.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentoService {
    private final DocumentoRepository repository;

    public DocumentoResponse subirDocumento(DocumentoRequest req) {
        Documento d = repository.save(Documento.builder().titulo(req.titulo()).descripcion(req.descripcion()).urlDescarga(req.urlDescarga()).fechaSubida(LocalDate.now()).build());
        return new DocumentoResponse(d.getId(), d.getTitulo(), d.getDescripcion(), d.getUrlDescarga(), d.getFechaSubida());
    }

    public List<DocumentoResponse> listarTodos() {
        return repository.findAll().stream().map(d -> new DocumentoResponse(d.getId(), d.getTitulo(), d.getDescripcion(), d.getUrlDescarga(), d.getFechaSubida())).toList();
    }
}