package com.nutripharma.api_nutripharma.documents.documentacion.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "documentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 255)
    private String descripcion;

    // En el MVP guardaremos la URL directa al archivo PDF (ej. un link de AWS S3 o una ruta local)
    @Column(name = "url_descarga", nullable = false, length = 500)
    private String urlDescarga;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDate fechaSubida;
}