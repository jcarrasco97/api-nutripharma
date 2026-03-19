package com.nutripharma.api_nutripharma.documents.documentacion.service;

import com.nutripharma.api_nutripharma.documents.documentacion.controller.dto.DocumentoDTO.*;
import com.nutripharma.api_nutripharma.documents.documentacion.domain.AlcanceDocumento;
import com.nutripharma.api_nutripharma.documents.documentacion.domain.Documento;
import com.nutripharma.api_nutripharma.documents.documentacion.repository.DocumentoRepository;
import com.nutripharma.api_nutripharma.security.domain.Rol;
import com.nutripharma.api_nutripharma.security.domain.Usuario;
import com.nutripharma.api_nutripharma.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final GoogleDriveService googleDriveService;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public DocumentoResponse subirDocumento(String alcanceStr, String propietarioEmail, MultipartFile archivo) throws IOException, GeneralSecurityException {
        // 1. Subir a Google Drive
        String driveFileId = googleDriveService.subirArchivo(archivo);

        AlcanceDocumento alcance = AlcanceDocumento.valueOf(alcanceStr);
        Usuario propietario = null;

        // 2. Buscar dueño si es INDIVIDUAL
        if (alcance == AlcanceDocumento.INDIVIDUAL && propietarioEmail != null && !propietarioEmail.isEmpty()) {
            propietario = usuarioRepository.findByEmail(propietarioEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        }

        // 3. Guardar en MySQL (Solo con el nombre original)
        Documento d = Documento.builder()
                .nombreOriginal(archivo.getOriginalFilename())
                .driveFileId(driveFileId)
                .mimeType(archivo.getContentType())
                .alcance(alcance)
                .propietario(propietario)
                .fechaSubida(LocalDate.now())
                .build();

        documentoRepository.save(d);
        return mapToDTO(d);
    }

    @Transactional(readOnly = true)
    public List<DocumentoResponse> listarMisDocumentos(String email) {
        Usuario u = usuarioRepository.findByEmail(email).orElseThrow();

        boolean isAdmin = u.getRoles().stream().map(Rol::getNombre).anyMatch(r -> r.equals("ROLE_ADMIN"));
        boolean isFarmacia = u.getRoles().stream().map(Rol::getNombre).anyMatch(r -> r.equals("ROLE_FARMACIA"));

        if (isAdmin) {
            return documentoRepository.findAll().stream().map(this::mapToDTO).toList();
        }

        AlcanceDocumento alcanceGlobal = isFarmacia ? AlcanceDocumento.GLOBAL_FARMACIAS : AlcanceDocumento.GLOBAL_NUTRICIONISTAS;

        // BUG DE VISIBILIDAD RESUELTO:
        return documentoRepository.findDocumentosPermitidos(
                email, alcanceGlobal, AlcanceDocumento.GLOBAL_TODOS, AlcanceDocumento.INDIVIDUAL
        ).stream().map(this::mapToDTO).toList();
    }

    // 👇 MÉTODO NUEVO: Eliminar de Drive y MySQL 👇
    @Transactional
    public void eliminarDocumento(Long id) throws IOException, GeneralSecurityException {
        Documento doc = documentoRepository.findById(id).orElseThrow();
        googleDriveService.eliminarArchivo(doc.getDriveFileId());
        documentoRepository.delete(doc);
    }

    // 👇 MÉTODO NUEVO: Para el desplegable de React 👇
    @Transactional(readOnly = true)
    public List<UsuarioDestinatarioProjection> obtenerUsuariosParaDesplegable() {
        return usuarioRepository.findUsuariosParaDesplegable();
    }

    public byte[] descargarDocumento(Long id) throws IOException, GeneralSecurityException {
        Documento doc = documentoRepository.findById(id).orElseThrow();
        return googleDriveService.descargarArchivo(doc.getDriveFileId());
    }

    public Documento obtenerMetadatos(Long id) {
        return documentoRepository.findById(id).orElseThrow();
    }

    private DocumentoResponse mapToDTO(Documento d) {
        return new DocumentoResponse(
                d.getId(),
                d.getNombreOriginal(),
                d.getAlcance(),
                d.getPropietario() != null ? d.getPropietario().getEmail() : null,
                null, // Lo rellenaremos desde React para no hacer consultas pesadas aquí
                d.getFechaSubida()
        );
    }
}