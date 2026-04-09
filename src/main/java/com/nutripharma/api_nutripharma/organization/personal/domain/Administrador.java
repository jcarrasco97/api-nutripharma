package com.nutripharma.api_nutripharma.organization.personal.domain;

import com.nutripharma.api_nutripharma.security.domain.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "administradores")
// --- MAGIA DEL BORRADO LÓGICO ---
@SQLDelete(sql = "UPDATE administradores SET activo = false WHERE id=?")
@SQLRestriction("activo = true")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación 1 a 1 con las credenciales
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String apellidos;

    // --- Soft Delete ---
    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}