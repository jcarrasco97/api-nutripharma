package com.nutripharma.api_nutripharma.documents.documentacion.repository;

import com.nutripharma.api_nutripharma.documents.documentacion.domain.AlcanceDocumento;
import com.nutripharma.api_nutripharma.documents.documentacion.domain.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    // Usamos LEFT JOIN para que no descarte los documentos que tienen propietario NULL (los globales)
    @Query("SELECT d FROM Documento d LEFT JOIN d.propietario p " +
            "WHERE d.alcance = :alcanceTodos " +
            "OR d.alcance = :alcanceGlobal " +
            "OR (d.alcance = :alcanceIndividual AND p.email = :email) " +
            "ORDER BY d.fechaSubida DESC")
    List<Documento> findDocumentosPermitidos(
            @Param("email") String email,
            @Param("alcanceGlobal") AlcanceDocumento alcanceGlobal,
            @Param("alcanceTodos") AlcanceDocumento alcanceTodos,
            @Param("alcanceIndividual") AlcanceDocumento alcanceIndividual
    );
}