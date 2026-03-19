package com.nutripharma.api_nutripharma.documents.documentacion.domain;

import com.nutripharma.api_nutripharma.security.domain.Usuario;
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

    // Solo nos quedamos con el nombre real del archivo (Ej: "dieta_verano.pdf")
    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(nullable = false, unique = true)
    private String driveFileId;

    @Column(nullable = false)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlcanceDocumento alcance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario propietario;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDate fechaSubida;
}