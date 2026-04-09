package com.nutripharma.api_nutripharma.core.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@Table(name = "auditoria_revisiones")
@RevisionEntity(AuditoriaRevisionListener.class)
@Getter
@Setter
public class AuditoriaRevisionEntity {

    // 1. El ID de la revisión (Obligatorio para Envers)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    private int id;

    // 2. La fecha y hora exacta del cambio (Obligatorio para Envers)
    @RevisionTimestamp
    private long timestamp;

    // 3. NUESTRO CAMPO PERSONALIZADO: ¿Quién hizo el cambio?
    @Column(name = "usuario_email")
    private String usuarioEmail;

}