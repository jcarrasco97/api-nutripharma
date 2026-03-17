package com.nutripharma.api_nutripharma.documents.documentacion.repository;
import com.nutripharma.api_nutripharma.documents.documentacion.domain.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DocumentoRepository extends JpaRepository<Documento, Long> {}